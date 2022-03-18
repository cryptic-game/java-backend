package net.cryptic_game.auth.oauth;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import net.cryptic_game.auth.oauth.OAuthProvider.Metadata;
import net.cryptic_game.auth.oauth.exception.UnknownOAuthProviderException;
import net.cryptic_game.auth.oauth.impl.OAuthServiceImpl;

public interface OAuthService {

  String CALLBACK_URI_TEMPLATE = "%s/auth/oauth/%s/callback";

  static String buildCallbackUri(final String baseUrl, final String providerId) {
    return OAuthServiceImpl.CALLBACK_URI_TEMPLATE.formatted(
        baseUrl,
        URLEncoder.encode(providerId, StandardCharsets.UTF_8)
    );
  }

  Set<Metadata> getMetadata();

  OAuthProvider getProvider(String id) throws UnknownOAuthProviderException;
}
