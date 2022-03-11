package net.cryptic_game.auth.oauth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cryptic.auth.oauth")
public record OAuthConfig(String publicUrl) {

}
