package net.cryptic_game.auth.oauth.github;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "cryptic.oauth.github")
public class GitHubOAuthConfig {

  private String clientId;
  private String clientSecret;
}
