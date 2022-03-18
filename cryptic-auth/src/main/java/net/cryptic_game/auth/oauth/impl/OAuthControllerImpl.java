package net.cryptic_game.auth.oauth.impl;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import net.cryptic_game.auth.oauth.OAuthController;
import net.cryptic_game.auth.oauth.OAuthFlowService;
import net.cryptic_game.auth.oauth.OAuthProvider;
import net.cryptic_game.auth.oauth.OAuthProvider.Metadata;
import net.cryptic_game.auth.oauth.OAuthService;
import net.cryptic_game.auth.oauth.exception.InvalidOAuthCodeException;
import net.cryptic_game.auth.oauth.exception.UnknownOAuthProviderException;
import net.cryptic_game.auth.user.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
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

  private static ResponseCookie createCookie(final String name, final String value) {
    return ResponseCookie.from(name, value)
        .httpOnly(true)
        .maxAge(Duration.ofMinutes(5))
        .sameSite("Lax")
        .path("/")
        .build();
  }

  @Override
  public Set<Metadata> getMetadata() {
    return this.oAuthService.getMetadata();
  }

  @Override
  public void auth(final String providerId, final UUID flowId, final ServerHttpResponse response) {
    final OAuthProvider provider = this.oAuthService.getProvider(providerId);
    final String state = UUID.randomUUID().toString();

    response.addCookie(createCookie("state", state));
    response.addCookie(createCookie("flow", flowId.toString()));

    response.getHeaders().setLocation(provider.buildAuthorizeUrl(state));
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
      return this.flowService.cancelFlow(flowId)
          .thenReturn(this.stateExpired);
    }

    final OAuthProvider provider;

    try {
      provider = this.oAuthService.getProvider(providerId);
    } catch (UnknownOAuthProviderException e) {
      return this.flowService.cancelFlow(flowId)
          .then(Mono.error(e));
    }

    return provider.handleCallback(code)
        .flatMap(providerUserId -> this.userService.login(providerId, providerUserId)
            .flatMap(user -> this.flowService.successfulCallback(flowId, user.id())
                .thenReturn(this.successfulAuth))
            .switchIfEmpty(this.userService.createRegisterToken(providerId, providerUserId).map(token -> {
              response.addCookie(createCookie("register_token", token.toString()));
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
//            TODO: timeout + if connection is closed -> remove send cancel event through redis
//            TODO: handle FlowCanceledException
//                .timeout(Duration.of(12))
        );
  }
}
