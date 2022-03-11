package net.cryptic_game.auth.oauth.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import net.cryptic_game.auth.oauth.InvalidOAuthCodeException;
import net.cryptic_game.auth.oauth.OAuthCallbackResponse;
import net.cryptic_game.auth.oauth.OAuthConfig;
import net.cryptic_game.auth.oauth.OAuthProvider;
import net.cryptic_game.auth.oauth.OAuthService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class GitHubOAuthProvider implements OAuthProvider {

  private static final String AUTH_URL_TEMPLATE = "https://github.com/login/oauth/authorize?client_id=%s&redirect_uri=%s&scope=&state=%s";
  private final GitHubOAuthConfig config;
  private final WebClient client;
  private final String callbackUri;

  public GitHubOAuthProvider(final OAuthConfig oAuthConfig, final GitHubOAuthConfig config) {
    this.config = config;
    this.client = WebClient.builder()
        .defaultHeaders(headers -> headers.setAccept(List.of(MediaType.APPLICATION_JSON)))
        .build();

    this.callbackUri = OAuthService.buildCallbackUri(oAuthConfig.getPublicUrl(), "discord");
  }

  @Override
  public URI buildAuthorizeUrl(final String state) {
    return URI.create(AUTH_URL_TEMPLATE.formatted(
        URLEncoder.encode(this.config.getClientId(), StandardCharsets.UTF_8),
        URLEncoder.encode(this.callbackUri, StandardCharsets.UTF_8),
        URLEncoder.encode(state, StandardCharsets.UTF_8)
    ));
  }

  @Override
  public Mono<OAuthCallbackResponse> handleCallback(final String code) {
    return this.requestToken(this.callbackUri, code)
        .flatMap(response ->
            response.accessToken() == null
                ? Mono.error(new InvalidOAuthCodeException())
                : this.requestUserInfo(response.accessToken())
        )
        .map(response -> new OAuthCallbackResponse(response.id()));
  }

  private Mono<TokenResponse> requestToken(final String callbackUrl, final String code) {
    final MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
    request.add("client_id", this.config.getClientId());
    request.add("client_secret", this.config.getClientSecret());
    request.add("code", code);
    request.add("redirect_uri", callbackUrl);

    return this.client.post()
        .uri("https://github.com/login/oauth/access_token")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(BodyInserters.fromFormData(request))
        .exchangeToMono(response -> response.statusCode().is2xxSuccessful()
            ? response.bodyToMono(TokenResponse.class)
            : response.createException().flatMap(Mono::error)
        );
  }

  private Mono<UserInfoResponse> requestUserInfo(final String token) {
    return this.client.get()
        .uri("https://api.github.com/user")
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

  private record UserInfoResponse(
      @JsonProperty("id") String id
  ) {

  }
}
