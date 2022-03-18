package net.cryptic_game.auth.oauth.impl.discord;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cryptic_game.auth.oauth.OAuthProvider;
import net.cryptic_game.auth.oauth.OAuthService;
import net.cryptic_game.auth.oauth.exception.InvalidOAuthCodeException;
import net.cryptic_game.common.CrypticConfig;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class DiscordOAuthProvider implements OAuthProvider {

  private static final Metadata METADATA = new Metadata("discord", "Discord");
  private static final String AUTH_URL_TEMPLATE = "https://discord.com/api/oauth2/authorize?client_id=%s&redirect_uri=%s&response_type=code&scope=identify&state=%s";
  private final DiscordOAuthConfig config;
  private final WebClient client;
  private final String callbackUri;

  public DiscordOAuthProvider(final CrypticConfig crypticConfig, final DiscordOAuthConfig config) {
    this.config = config;
    this.client = WebClient.builder()
        .baseUrl("https://discord.com/api/oauth2/")
        .defaultHeaders(headers -> headers.setContentType(MediaType.APPLICATION_JSON))
        .build();

    this.callbackUri = OAuthService.buildCallbackUri(crypticConfig.publicUrl(), "discord");
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
        .flatMap(tokenResponse ->
            this.requestUserInfo(tokenResponse.accessToken())
                .map(userInfoResponse -> userInfoResponse.user().id())
                .doOnNext(ignored -> this.revokeToken(tokenResponse))
        );
  }

  private void revokeToken(final TokenResponse response) {
    final Mono<Void> accessToken = this.revokeToken(response.accessToken(), TokenType.ACCESS_TOKEN);
    final Mono<Void> refreshToken = this.revokeToken(response.refreshToken(), TokenType.REFRESH_TOKEN);

    Mono.zip(accessToken, refreshToken)
        .doOnError(throwable -> log.error("Unable to revoke discord tokens.", throwable))
        .subscribe();
  }

  private Mono<TokenResponse> requestToken(final String callbackUrl, final String code) {
    final MultiValueMap<String, String> request = new LinkedMultiValueMap<>(5);
    request.add("client_id", this.config.clientId());
    request.add("client_secret", this.config.clientSecret());
    request.add("grant_type", "authorization_code");
    request.add("code", code);
    request.add("redirect_uri", callbackUrl);

    return this.client.post()
        .uri("token")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(BodyInserters.fromFormData(request))
        .exchangeToMono(response ->
            response.statusCode().is2xxSuccessful()
                ? response.bodyToMono(TokenResponse.class)
                : this.handleTokenError(response)
        );
  }

  private Mono<Void> revokeToken(final String token, final TokenType tokenType) {
    final MultiValueMap<String, String> request = new LinkedMultiValueMap<>(4);
    request.add("client_id", this.config.clientId());
    request.add("client_secret", this.config.clientSecret());
    request.add("token", token);
    request.add("token_type_hint", tokenType.toString());

    return this.client.post()
        .uri("token/revoke")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(BodyInserters.fromValue(request))
        .exchangeToMono(response ->
            response.statusCode().is2xxSuccessful()
                ? Mono.empty()
                : response.createException().flatMap(Mono::error)
        );
  }

  private Mono<AuthInformationResponse> requestUserInfo(final String token) {
    return this.client.get()
        .uri("@me")
        .headers(headers -> headers.setBearerAuth(token))
        .exchangeToMono(response ->
            response.statusCode().is2xxSuccessful()
                ? response.bodyToMono(AuthInformationResponse.class)
                : response.createException().flatMap(Mono::error)
        );
  }

  private <T> Mono<T> handleTokenError(final ClientResponse response) {
    if (response.statusCode().equals(HttpStatus.BAD_REQUEST)) {
      return response.bodyToMono(DiscordApiError.class)
          .flatMap(error ->
              error.description().equals("Invalid \"code\" in request.")
                  ? Mono.error(new InvalidOAuthCodeException())
                  : response.createException().flatMap(Mono::error)
          );
    }

    return response.createException()
        .flatMap(Mono::error);
  }

  @RequiredArgsConstructor
  private enum TokenType {
    REFRESH_TOKEN("refresh_token"),
    ACCESS_TOKEN("access_token");

    private final String value;

    @Override
    public String toString() {
      return this.value;
    }
  }

  private record TokenResponse(@JsonProperty("access_token") String accessToken,
                               @JsonProperty("refresh_token") String refreshToken) {
  }

  private record AuthInformationResponse(@JsonProperty("user") User user) {

    private record User(@JsonProperty("id") String id) {
    }
  }

  private record DiscordApiError(@JsonProperty("error_description") String description) {
  }
}
