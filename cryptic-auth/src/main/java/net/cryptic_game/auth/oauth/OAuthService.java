package net.cryptic_game.auth.oauth;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;
import net.cryptic_game.auth.oauth.OAuthProvider.Metadata;
import net.cryptic_game.auth.oauth.impl.OAuthServiceImpl;
import reactor.core.publisher.Mono;

public interface OAuthService {

  String CALLBACK_URI_TEMPLATE = "%s/auth/oauth/%s/callback";

  static String buildCallbackUri(final String baseUrl, final String providerId) {
    return OAuthServiceImpl.CALLBACK_URI_TEMPLATE.formatted(
        baseUrl,
        URLEncoder.encode(providerId, StandardCharsets.UTF_8)
    );
  }

  Set<Metadata> getMetadata();

  URI buildAuthorizeUri(String providerId, String state);

  Mono<Void> cancelFlow(UUID flowId);

  Mono<String> handleCallback(UUID flowId, String providerId, String code);
}
