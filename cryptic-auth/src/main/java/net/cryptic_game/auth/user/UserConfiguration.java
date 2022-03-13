package net.cryptic_game.auth.user;

import net.cryptic_game.auth.user.UserService.RegisterToken;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class UserConfiguration {

  @Bean
  ReactiveRedisTemplate<String, RegisterToken> registerTokenRedisTemplate(
      final LettuceConnectionFactory connectionFactory
  ) {
    final RedisSerializer<RegisterToken> serializer = new Jackson2JsonRedisSerializer<>(RegisterToken.class);

    final RedisSerializationContext<String, RegisterToken> context = RedisSerializationContext
        .<String, RegisterToken>newSerializationContext(RedisSerializer.string())
        .value(serializer)
        .build();

    return new ReactiveRedisTemplate<>(connectionFactory, context);
  }
}
