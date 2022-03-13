package net.cryptic_game.auth.oauth.impl;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import net.cryptic_game.auth.oauth.OAuthController;
import net.cryptic_game.auth.oauth.OAuthFlowService;
import net.cryptic_game.auth.oauth.OAuthProvider.Metadata;
import net.cryptic_game.auth.oauth.OAuthService;
import net.cryptic_game.auth.oauth.exception.InvalidOAuthCodeException;
import net.cryptic_game.auth.user.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Tag(name = "OAuth")
@RequestMapping("/auth/oauth")
public class OAuthControllerImpl implements OAuthController {

  private final OAuthService oAuthService;
  private final OAuthFlowService flowService;
  private final UserService userService;
  private final Resource successfulAuth;
  private final Resource stateExpired;
  private final Resource register;

  public OAuthControllerImpl(
      final OAuthService oAuthService,
      final OAuthFlowService flowService,
      final UserService userService,
      @Value("classpath:/pages/successful_auth.html") final Resource successfulAuth,
      @Value("classpath:/pages/state_expired.html") final Resource stateExpired,
      @Value("classpath:/pages/register.html") final Resource register
  ) {
    this.oAuthService = oAuthService;
    this.flowService = flowService;
    this.userService = userService;
    this.successfulAuth = successfulAuth;
    this.stateExpired = stateExpired;
    this.register = register;
  }

  @Override
  public Set<Metadata> getMetadata() {
    return this.oAuthService.getMetadata();
  }

  @Override
  public void auth(final String providerId, final String flowId, final ServerHttpResponse response) {
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

    final ResponseCookie challengeCookie = ResponseCookie.from("flow", flowId)
        .httpOnly(true)
        .maxAge(Duration.ofMinutes(5))
        .sameSite("Lax")
        .path("/")
        .build();

    response.addCookie(challengeCookie);

    final HttpHeaders headers = response.getHeaders();
    headers.setLocation(callbackUri);
  }

  @Override
  public Mono<Resource> handleCallback(
      final String providerId,
      final String code,
      final String actualState,
      final String state,
      final UUID flowId,
      final ServerHttpResponse response
  ) {
    if (flowId == null) {
      return Mono.just(this.stateExpired);
    }

    if (!actualState.equals(state)) {
      return this.oAuthService.cancelFlow(flowId)
          .thenReturn(this.stateExpired);
    }

    return this.oAuthService.handleCallback(flowId, providerId, code)
        .flatMap(providerUserId -> this.userService.login(providerId, providerUserId)
            .flatMap(user -> this.flowService.successfulCallback(flowId, user.id())
                .thenReturn(this.successfulAuth))
            .switchIfEmpty(this.userService.createRegisterToken(providerId, providerUserId).map(token -> {
              final ResponseCookie stateCookie = ResponseCookie.from("register_token", token.toString())
                  .httpOnly(true)
                  .maxAge(Duration.ofMinutes(5))
                  .sameSite("Lax")
                  .path("/")
                  .build();

              response.addCookie(stateCookie);

              return this.register;
            })))
        .onErrorResume(InvalidOAuthCodeException.class, ignored -> this.flowService.cancelFlow(flowId).thenReturn(this.stateExpired));
  }

  @Override
  public Flux<String> flow() {
    final UUID flowId = UUID.randomUUID();

    return Mono.just(flowId.toString())
        .concatWith(
            this.flowService.receiveFlowResponse(flowId)
//                .timeout(Duration.of(12))
        );
  }
}
