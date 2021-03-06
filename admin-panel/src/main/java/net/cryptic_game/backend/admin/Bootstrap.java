package net.cryptic_game.backend.admin;

import net.getnova.framework.core.GlobalErrorWebExceptionHandler;
import net.getnova.framework.core.NovaBanner;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.WebSession;

import java.security.Principal;
import java.util.stream.Collectors;

@SpringBootApplication
@RestController
@EnableConfigurationProperties(Config.class)
public class Bootstrap {

    private static final String JAVASCRIPT_CLOSE = "<script>close()</script>";

    public static void main(final String[] args) {
        new SpringApplicationBuilder(Bootstrap.class)
                .banner(new NovaBanner())
                .run(args);
    }

    @GetMapping("/user")
    public Principal user(@AuthenticationPrincipal final Principal principal) {
        return principal;
    }

    @GetMapping(value = "/auth/success", produces = MediaType.TEXT_HTML_VALUE)
    public String auth(@AuthenticationPrincipal final Authentication authentication, final WebSession session) {
        return JAVASCRIPT_CLOSE;
    }

    @Bean("server")
    WebClient client(final Config config) {
        return WebClient.builder()
                .baseUrl(config.getServerUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, config.getApiToken())
                .build();
    }

    @Bean
    GroupedOpenApi websiteApi() {
        return GroupedOpenApi.builder()
                .group("cryptic-website")
                .pathsToMatch("/website/**")
                .build();
    }

    @Bean
    GroupedOpenApi serverApi() {
        return GroupedOpenApi.builder()
                .group("cryptic-server")
                .pathsToMatch("/server_management/**")
                .build();
    }

    @Bean
    @Order(-2)
    public ErrorWebExceptionHandler errorWebExceptionHandler(
            final ErrorAttributes errorAttributes,
            //TODO
            final ResourceProperties resourceProperties,
            final WebProperties webProperties,
            final ApplicationContext applicationContext,
            final ServerProperties serverProperties,
            final ObjectProvider<ViewResolver> viewResolvers,
            final ServerCodecConfigurer serverCodecConfigurer
    ) {
        final AbstractErrorWebExceptionHandler exceptionHandler = new GlobalErrorWebExceptionHandler(
                errorAttributes,
                resourceProperties.hasBeenCustomized() ? resourceProperties : webProperties.getResources(),
                applicationContext,
                serverProperties.getError()
        );
        exceptionHandler.setViewResolvers(viewResolvers.orderedStream().collect(Collectors.toList()));
        exceptionHandler.setMessageWriters(serverCodecConfigurer.getWriters());
        exceptionHandler.setMessageReaders(serverCodecConfigurer.getReaders());
        return exceptionHandler;
    }
}
