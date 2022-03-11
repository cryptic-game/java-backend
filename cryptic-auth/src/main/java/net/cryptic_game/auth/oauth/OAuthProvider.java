package net.cryptic_game.auth.oauth;

import java.net.URI;
import reactor.core.publisher.Mono;

public interface OAuthProvider {

  URI buildAuthorizeUrl(String state);

  Mono<OAuthCallbackResponse> handleCallback(String code);
}
