package net.cryptic_game.auth.oauth.impl.discord;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cryptic.auth.oauth.discord")
public record DiscordOAuthConfig(String clientId, String clientSecret) {

}
