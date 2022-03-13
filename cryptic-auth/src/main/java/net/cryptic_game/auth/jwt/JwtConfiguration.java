package net.cryptic_game.auth.jwt;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class JwtConfiguration {

  @Bean
  Key jwtKey(final ApplicationContext context, final JwtConfig config) {
    final byte[] bytes = config.key().getBytes(StandardCharsets.UTF_8);

    if (bytes.length * 8 < SignatureAlgorithm.HS512.getMinKeyLength()) {
      log.error("A key length of {} bits is too weak! Minimum required is {} bits.",
          bytes.length * 8, SignatureAlgorithm.HS512.getMinKeyLength());
      SpringApplication.exit(context);
    }

    return Keys.hmacShaKeyFor(bytes);
  }
}
