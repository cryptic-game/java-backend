package net.cryptic_game.auth.oauth;

import java.util.Set;
import java.util.UUID;
import net.cryptic_game.auth.oauth.OAuthProvider.Metadata;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OAuthController {

  @GetMapping
  Set<Metadata> getMetadata();

  @ResponseStatus(HttpStatus.FOUND)
  @GetMapping("{provider_id}/auth/{challenge_id}")
  void auth(
      @PathVariable("provider_id") String providerId,
      @PathVariable("challenge_id") String flowId,
      ServerHttpResponse response
  );

  @GetMapping(value = "{provider_id}/callback", produces = MediaType.TEXT_HTML_VALUE)
  Mono<Resource> handleCallback(
      @PathVariable("provider_id") String providerId,
      @RequestParam("code") String code,
      @RequestParam("state") String actualState,
      @CookieValue(value = "state", required = false) String state,
      @CookieValue(value = "flow", required = false) UUID flowId
  );

  @GetMapping(value = "flow", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  Flux<String> flow();
}
