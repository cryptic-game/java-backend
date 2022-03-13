package net.cryptic_game.auth;

import de.m4rc3l.nova.core.NovaBanner;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import net.cryptic_game.auth.jwt.JwtConfig;
import net.cryptic_game.auth.oauth.impl.discord.DiscordOAuthConfig;
import net.cryptic_game.auth.oauth.impl.github.GitHubOAuthConfig;
import net.cryptic_game.common.CrypticConfig;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties({CrypticConfig.class, DiscordOAuthConfig.class, GitHubOAuthConfig.class, JwtConfig.class})
public class Bootstrap {

  public static void main(final String[] args) {
    new SpringApplicationBuilder(Bootstrap.class)
        .banner(new NovaBanner())
        .run(args);
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
