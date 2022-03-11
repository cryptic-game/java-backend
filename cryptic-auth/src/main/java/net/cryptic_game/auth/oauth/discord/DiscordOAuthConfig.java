package net.cryptic_game.auth.oauth.discord;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "cryptic.oauth.discord")
public class DiscordOAuthConfig {

  private String clientId;
  private String clientSecret;
}
