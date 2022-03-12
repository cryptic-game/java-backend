package net.cryptic_game.auth.oauth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class OAuthConfiguration {

  @Bean
  ReactiveRedisTemplate<String, OAuthFlowService.OAuthFlowResponse> oauthRedisTemplate(
      final LettuceConnectionFactory connectionFactory
  ) {
    final RedisSerializer<OAuthFlowService.OAuthFlowResponse> serializer = new Jackson2JsonRedisSerializer<>(
        OAuthFlowService.OAuthFlowResponse.class);

    final RedisSerializationContext<String, OAuthFlowService.OAuthFlowResponse> context = RedisSerializationContext
        .<String, OAuthFlowService.OAuthFlowResponse>newSerializationContext(RedisSerializer.string())
        .value(serializer)
        .build();

    return new ReactiveRedisTemplate<>(connectionFactory, context);
  }
}
