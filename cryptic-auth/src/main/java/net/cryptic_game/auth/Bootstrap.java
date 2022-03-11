package net.cryptic_game.auth;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import net.cryptic_game.auth.oauth.OAuthConfig;
import net.cryptic_game.auth.oauth.discord.DiscordOAuthConfig;
import net.cryptic_game.auth.oauth.github.GitHubOAuthConfig;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties({OAuthConfig.class, DiscordOAuthConfig.class, GitHubOAuthConfig.class})
public class Bootstrap {

  public static void main(final String[] args) {
    SpringApplication.run(Bootstrap.class, args);
  }

  @Bean
  OpenAPI openApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Cryptic Auth Api Definition")
                .version(Bootstrap.class.getPackage().getImplementationVersion())
        );
  }

  @Bean
  GroupedOpenApi crypticAuth() {
    return GroupedOpenApi.builder()
        .group("cryptic-auth")
        .pathsToMatch("/auth/**")
        .build();
  }
}
