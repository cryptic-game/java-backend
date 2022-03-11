package net.cryptic_game.auth.oauth;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import net.cryptic_game.auth.oauth.discord.DiscordOAuthProvider;
import net.cryptic_game.auth.oauth.github.GitHubOAuthProvider;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class OAuthService {

  private static final String CALLBACK_URI_TEMPLATE = "%s/auth/oauth/provider/%s/callback";
  private final Map<String, OAuthProvider> provider;

  public OAuthService(
      final DiscordOAuthProvider discordOAuthProvider,
      final GitHubOAuthProvider gitHubOAuthProvider
  ) {
    this.provider = Map.of(
        "discord", discordOAuthProvider,
        "github", gitHubOAuthProvider
    );
  }

  public static String buildCallbackUri(final String baseUrl, final String providerId) {
    return CALLBACK_URI_TEMPLATE.formatted(
        baseUrl,
        URLEncoder.encode(providerId, StandardCharsets.UTF_8)
    );
  }

  public URI buildAuthorizeUri(final String providerId, final String state) {
    final OAuthProvider provider = this.provider.get(providerId);

    if (provider == null) {
      return null;
    }

    return provider.buildAuthorizeUrl(state);
  }

  public Mono<OAuthCallbackResponse> handleCallback(
      final String providerId,
      final String code,
      final String challengeId
  ) {
    final OAuthProvider provider = this.provider.get(providerId);

    return provider.handleCallback(code);
  }
}
