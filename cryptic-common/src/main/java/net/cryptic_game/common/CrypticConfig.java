package net.cryptic_game.common;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cryptic")
public record CrypticConfig(String publicUrl) {

}
