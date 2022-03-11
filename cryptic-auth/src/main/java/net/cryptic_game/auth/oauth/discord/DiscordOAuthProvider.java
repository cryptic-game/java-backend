package net.cryptic_game.auth.oauth.discord;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cryptic_game.auth.oauth.InvalidOAuthCodeException;
import net.cryptic_game.auth.oauth.OAuthCallbackResponse;
import net.cryptic_game.auth.oauth.OAuthConfig;
import net.cryptic_game.auth.oauth.OAuthProvider;
import net.cryptic_game.auth.oauth.OAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
public class DiscordOAuthProvider implements OAuthProvider {

  private static final String AUTH_URL_TEMPLATE = "https://discord.com/api/oauth2/authorize?client_id=%s&redirect_uri=%s&response_type=code&scope=identify&state=%s";
  private final DiscordOAuthConfig config;
  private final WebClient client;
  private final String callbackUri;

  public DiscordOAuthProvider(final OAuthConfig oAuthConfig, final DiscordOAuthConfig config) {
    this.config = config;
    this.client = WebClient.builder()
        .baseUrl("https://discord.com/api/oauth2/")
        .defaultHeaders(headers -> headers.setContentType(MediaType.APPLICATION_JSON))
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
        .flatMap(tokenResponse ->
            this.requestUserInfo(tokenResponse.accessToken())
                .map(userInfoResponse -> new OAuthCallbackResponse(userInfoResponse.user().id()))
                .doOnNext(ignored ->
                    Mono.zip(
                            this.revokeToken(tokenResponse.accessToken(), TokenType.ACCESS_TOKEN),
                            this.revokeToken(tokenResponse.refreshToken(), TokenType.REFRESH_TOKEN)
                        )
                        .subscribeOn(Schedulers.boundedElastic())
                        .subscribe(objects -> {
                        }, throwable -> log.error("Unable to revoke discord tokens.", throwable))
                )
        );
  }

  private Mono<TokenResponse> requestToken(final String callbackUrl, final String code) {
    final MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
    request.add("client_id", this.config.getClientId());
    request.add("client_secret", this.config.getClientSecret());
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
    final MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
    request.add("client_id", this.config.getClientId());
    request.add("client_secret", this.config.getClientSecret());
    request.add("token", token);
    request.add("token_type_hint", tokenType.getValue());

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

  private record TokenResponse(
      @JsonProperty("access_token") String accessToken,
      @JsonProperty("token_type") String tokenType,
      @JsonProperty("expires_in") long expiresIn,
      @JsonProperty("refresh_token") String refreshToken,
      @JsonProperty("scope") String scope
  ) {

  }

  @Getter
  @RequiredArgsConstructor
  private enum TokenType {
    REFRESH_TOKEN("refresh_token"),
    ACCESS_TOKEN("access_token");

    private final String value;
  }

  private record AuthInformationResponse(
      // @JsonProperty("application") Application application,
      @JsonProperty("scopes") List<String> scopes,
      @JsonProperty("expires") OffsetDateTime expires,
      @JsonProperty("user") User user
  ) {

    private record User(
        @JsonProperty("id") String id
    ) {

    }
  }

  private record DiscordApiError(
      @JsonProperty("error") String type,
      @JsonProperty("error_description") String description
  ) {

  }
}
