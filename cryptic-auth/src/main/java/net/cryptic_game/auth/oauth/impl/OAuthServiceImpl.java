package net.cryptic_game.auth.oauth.impl;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.cryptic_game.auth.oauth.OAuthProvider;
import net.cryptic_game.auth.oauth.OAuthProvider.Metadata;
import net.cryptic_game.auth.oauth.OAuthService;
import net.cryptic_game.auth.oauth.exception.UnknownOAuthProviderException;
import org.springframework.stereotype.Service;

@Service
public class OAuthServiceImpl implements OAuthService {

  private final Map<String, OAuthProvider> providers;
  private final Set<Metadata> metadata;

  public OAuthServiceImpl(final Set<OAuthProvider> providers) {
    this.providers = providers.stream()
        .collect(Collectors.toUnmodifiableMap(
            provider -> provider.getMetadata().id(),
            Function.identity()
        ));

    this.metadata = providers.stream()
        .map(OAuthProvider::getMetadata)
        .collect(Collectors.toUnmodifiableSet());
  }

  @Override
  public Set<Metadata> getMetadata() {
    return this.metadata;
  }

  @Override
  public OAuthProvider getProvider(final String id) throws UnknownOAuthProviderException {
    final OAuthProvider provider = this.providers.get(id);

    if (provider == null) {
      throw new UnknownOAuthProviderException(id);
    }

    return provider;
  }
}
