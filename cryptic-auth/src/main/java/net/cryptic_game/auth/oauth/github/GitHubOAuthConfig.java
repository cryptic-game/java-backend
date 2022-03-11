package net.cryptic_game.auth.oauth.github;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cryptic.auth.oauth.github")
public record GitHubOAuthConfig(String clientId, String clientSecret) {

}
