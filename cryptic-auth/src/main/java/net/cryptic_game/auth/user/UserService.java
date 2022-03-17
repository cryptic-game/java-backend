package net.cryptic_game.auth.user;

import java.util.UUID;
import net.cryptic_game.common.domain.auth.User;
import reactor.core.publisher.Mono;

public interface UserService {

  Mono<User> login(String providerId, String providerUserId);

  Mono<User> register(String registerTokenId, String name);

  Mono<Boolean> isUsernameAvailable(String name);

  Mono<UUID> createRegisterToken(String providerId, String providerUserId);

  Mono<Boolean> isUsernameAcceptable(String name);

  record RegisterToken(String providerId, String providerUserId) {

  }
}
