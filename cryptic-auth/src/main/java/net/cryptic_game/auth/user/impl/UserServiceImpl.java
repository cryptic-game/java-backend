package net.cryptic_game.auth.user.impl;

import de.m4rc3l.nova.core.utils.ValidationUtils;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import net.cryptic_game.auth.domain.converter.UserModelConverter;
import net.cryptic_game.auth.domain.model.UserModel;
import net.cryptic_game.auth.domain.model.UserOAuthModel;
import net.cryptic_game.auth.domain.model.UserOAuthModel.Id;
import net.cryptic_game.auth.domain.repository.UserOAuthRepository;
import net.cryptic_game.auth.domain.repository.UserRepository;
import net.cryptic_game.auth.user.UserService;
import net.cryptic_game.auth.user.exception.InvalidRegisterTokenException;
import net.cryptic_game.common.domain.auth.User;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private static final Duration REGISTER_TOKEN_LIFETIME = Duration.ofMinutes(10);
  private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\d_]{3,20}$");

  private final UserRepository userRepository;
  private final UserModelConverter userModelConverter;
  private final UserOAuthRepository userOAuthRepository;
  private final ReactiveRedisTemplate<String, RegisterToken> registerTokenRedisTemplate;

  @Override
  public Mono<User> login(final String providerId, final String providerUserId) {
    return Mono.fromCallable(() -> this.userRepository.findByProviderIdAndProviderUserId(providerId, providerUserId))
        .flatMap(Mono::justOrEmpty)
        .map(this.userModelConverter::toDto)
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Override
  public Mono<User> register(final String registerTokenId, final String name) {
    return this.registerTokenRedisTemplate.opsForValue().getAndDelete(registerTokenId)
        .switchIfEmpty(Mono.defer(() -> Mono.error(new InvalidRegisterTokenException())))
        .doOnNext(ignored -> ValidationUtils.pattern("name", name, NAME_PATTERN))
        .flatMap(token ->
            Mono.fromCallable(() -> {
              final OffsetDateTime now = OffsetDateTime.now();
              final UserModel userModel = new UserModel(name.strip(), now, now);
              final Id id = new Id(userModel, token.providerId());
              final UserOAuthModel userOAuthModel = new UserOAuthModel(id, token.providerUserId(), now, now);
              this.userRepository.save(userModel);
              this.userOAuthRepository.save(userOAuthModel);
              return this.userModelConverter.toDto(userModel);
            }).subscribeOn(Schedulers.boundedElastic())
        );
  }

  @Override
  public Mono<Boolean> isUsernameAvailable(final String name) {
    return Mono.fromCallable(() -> this.userRepository.isUsernameAvailable(name))
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Override
  public Mono<UUID> createRegisterToken(final String providerId, final String providerUserId) {
    final UUID id = UUID.randomUUID();
    final RegisterToken token = new RegisterToken(providerId, providerUserId);

    return this.registerTokenRedisTemplate.opsForValue()
        .set(id.toString(), token, REGISTER_TOKEN_LIFETIME)
        .thenReturn(id);
  }

  @Override
  public Mono<Boolean> isUsernameAcceptable(final String name) {
    return Mono.just(NAME_PATTERN.matcher(name).matches());
  }
}
