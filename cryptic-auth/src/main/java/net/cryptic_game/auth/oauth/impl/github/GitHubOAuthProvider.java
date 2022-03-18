package net.cryptic_game.auth.oauth.impl.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import net.cryptic_game.auth.oauth.OAuthProvider;
import net.cryptic_game.auth.oauth.OAuthService;
import net.cryptic_game.auth.oauth.exception.InvalidOAuthCodeException;
import net.cryptic_game.common.CrypticConfig;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class GitHubOAuthProvider implements OAuthProvider {

  private static final Metadata METADATA = new Metadata("github", "GitHub");
  private static final String AUTH_URL_TEMPLATE = "https://github.com/login/oauth/authorize?client_id=%s&redirect_uri=%s&scope=&state=%s";

  private static final String TOKEN_URL = "https://github.com/login/oauth/access_token";
  private static final String USERINFO_URL = "https://api.github.com/user";

  private final GitHubOAuthConfig config;
  private final WebClient client;
  private final String callbackUri;

  public GitHubOAuthProvider(final CrypticConfig crypticConfig, final GitHubOAuthConfig config) {
    this.config = config;
    this.client = WebClient.builder()
        .defaultHeaders(headers -> headers.setAccept(List.of(MediaType.APPLICATION_JSON)))
        .build();

    this.callbackUri = OAuthService.buildCallbackUri(crypticConfig.publicUrl(), "github");
  }

  @Override
  public Metadata getMetadata() {
    return METADATA;
  }

  @Override
  public URI buildAuthorizeUrl(final String state) {
    return URI.create(AUTH_URL_TEMPLATE.formatted(
        URLEncoder.encode(this.config.clientId(), StandardCharsets.UTF_8),
        URLEncoder.encode(this.callbackUri, StandardCharsets.UTF_8),
        URLEncoder.encode(state, StandardCharsets.UTF_8)
    ));
  }

  @Override
  public Mono<String> handleCallback(final String code) {
    return this.requestToken(this.callbackUri, code)
        .flatMap(response ->
            response.accessToken() == null
                ? Mono.error(new InvalidOAuthCodeException())
                : this.requestUserInfo(response.accessToken())
        )
        .map(UserInfoResponse::id);
  }

  private Mono<TokenResponse> requestToken(final String callbackUrl, final String code) {
    final MultiValueMap<String, String> request = new LinkedMultiValueMap<>(4);
    request.add("client_id", this.config.clientId());
    request.add("client_secret", this.config.clientSecret());
    request.add("code", code);
    request.add("redirect_uri", callbackUrl);

    return this.client.post()
        .uri(TOKEN_URL)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(BodyInserters.fromFormData(request))
        .exchangeToMono(response -> response.statusCode().is2xxSuccessful()
            ? response.bodyToMono(TokenResponse.class)
            : response.createException().flatMap(Mono::error)
        );
  }

  private Mono<UserInfoResponse> requestUserInfo(final String token) {
    return this.client.get()
        .uri(USERINFO_URL)
        .headers(headers -> headers.setBearerAuth(token))
        .exchangeToMono(response ->
            response.statusCode().is2xxSuccessful()
                ? response.bodyToMono(UserInfoResponse.class)
                : response.createException().flatMap(Mono::error)
        );
  }

  private record TokenResponse(
      @JsonProperty("access_token") String accessToken,
      @JsonProperty("scope") String scope,
      @JsonProperty("token_type") String tokenType
  ) {
  }

  private record UserInfoResponse(@JsonProperty("id") String id) {
  }
}
