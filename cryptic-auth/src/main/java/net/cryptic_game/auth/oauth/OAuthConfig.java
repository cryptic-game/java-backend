package net.cryptic_game.auth.oauth;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "cryptic.oauth")
public class OAuthConfig {

  private String publicUrl;
}
