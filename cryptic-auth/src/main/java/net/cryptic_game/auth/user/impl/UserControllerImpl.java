package net.cryptic_game.auth.user.impl;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import net.cryptic_game.auth.oauth.OAuthFlowService;
import net.cryptic_game.auth.user.UserController;
import net.cryptic_game.auth.user.UserService;
import net.cryptic_game.auth.user.exception.InvalidRegisterTokenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@Tag(name = "User")
@RequestMapping("/auth/user")
public class UserControllerImpl implements UserController {

  private final UserService userService;
  private final OAuthFlowService flowService;
  private final Resource successfulAuth;
  private final Resource stateExpired;

  public UserControllerImpl(
      final UserService userService,
      final OAuthFlowService flowService,
      @Value("classpath:/pages/successful_auth.html") final Resource successfulAuth,
      @Value("classpath:/pages/state_expired.html") final Resource stateExpired
  ) {
    this.userService = userService;
    this.flowService = flowService;
    this.successfulAuth = successfulAuth;
    this.stateExpired = stateExpired;
  }

  @Override
  public Mono<Boolean> checkUsername(final String name) {
    return this.userService.isUsernameAvailable(name);
  }

  @Override
  public Mono<Resource> register(final RegisterBody body, final String registerToken, final UUID flowId) {
    if (flowId == null) {
      return Mono.just(this.stateExpired);
    }

    if (registerToken == null) {
      return this.flowService.cancelFlow(flowId)
          .thenReturn(this.stateExpired);
    }

    if (body.username() == null || body.username().isBlank()) {
      return this.flowService.cancelFlow(flowId)
          .then(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST)));
    }

    return this.userService.register(registerToken, body.username())
        .flatMap(user -> this.flowService.successfulCallback(flowId, user.id()))
        .thenReturn(this.successfulAuth)
        .onErrorResume(InvalidRegisterTokenException.class, ignored ->
            this.flowService.cancelFlow(flowId).thenReturn(this.stateExpired));
  }
}
