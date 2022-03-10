package net.cryptic_game.auth;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
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
}
