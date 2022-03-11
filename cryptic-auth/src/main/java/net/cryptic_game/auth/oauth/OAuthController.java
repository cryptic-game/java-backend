package net.cryptic_game.auth.oauth;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import net.cryptic_game.CrypticConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Tag(name = "OAuth")
@RequestMapping("/auth/oauth")
public class OAuthController {

  private final OAuthService oAuthService;
  private final Resource successfulAuth;
  private final Resource stateExpired;

  public OAuthController(
      final CrypticConfig config,
      final OAuthService oAuthService,
      @Value("classpath:/pages/successful_auth.html") final Resource successfulAuth,
      @Value("classpath:/pages/state_expired.html") final Resource stateExpired
  ) {
    this.oAuthService = oAuthService;
    this.successfulAuth = successfulAuth;
    this.stateExpired = stateExpired;
  }

  @GetMapping("provider")
  public Set<String> findProvider() {
    return Set.of("discord", "github");
  }

  @ResponseStatus(HttpStatus.FOUND)
  @GetMapping("provider/{provider_id}/auth/{challenge_id}")
  public void auth(
      @PathVariable("provider_id") final String providerId,
      @PathVariable("challenge_id") final String challengeId,
      final ServerHttpResponse response
  ) {
    final String state = UUID.randomUUID().toString();
    final URI callbackUri = this.oAuthService.buildAuthorizeUri(providerId, state);

    if (callbackUri == null) {
      throw new IllegalStateException("TODO");
    }

    final ResponseCookie stateCookie = ResponseCookie.from("state", state)
        .httpOnly(true)
        .maxAge(Duration.ofMinutes(5))
        .sameSite("Lax")
        .path("/")
        .build();

    response.addCookie(stateCookie);

    final ResponseCookie challengeCookie = ResponseCookie.from("challenge", challengeId)
        .httpOnly(true)
        .maxAge(Duration.ofMinutes(5))
        .sameSite("Lax")
        .path("/")
        .build();

    response.addCookie(challengeCookie);

    final HttpHeaders headers = response.getHeaders();
    headers.setLocation(callbackUri);
  }

  @GetMapping(value = "provider/{provider_id}/callback", produces = MediaType.TEXT_HTML_VALUE)
  public Mono<Resource> handleCallback(
      @PathVariable("provider_id") final String providerId,
      @RequestParam("code") final String code,
      @RequestParam("state") final String actualState,
      @CookieValue(value = "state", required = false) final String state,
      @CookieValue(value = "challenge", required = false) final String challengeId
  ) {
    if (!actualState.equals(state) || challengeId == null) {
      return Mono.just(this.stateExpired);
    }

    return this.oAuthService.handleCallback(providerId, code, challengeId)
        .doOnNext(System.out::println)
        .map(ignored -> this.successfulAuth)
        .onErrorResume(InvalidOAuthCodeException.class, ignored -> Mono.just(this.stateExpired));
  }
}
