package net.cryptic_game.auth.jwt;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cryptic.jwt")
public record JwtConfig(String key, Duration lifetime, String issuer, String audience) {
}
