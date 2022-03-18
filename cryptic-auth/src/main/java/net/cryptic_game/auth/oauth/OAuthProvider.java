package net.cryptic_game.auth.oauth;

import java.net.URI;
import reactor.core.publisher.Mono;

public interface OAuthProvider {

  Metadata getMetadata();

  URI buildAuthorizeUrl(String state);

  Mono<String> handleCallback(String code);

  record Metadata(String id, String name) {
  }
}
