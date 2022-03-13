package net.cryptic_game.auth.oauth.impl;

import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.cryptic_game.auth.oauth.OAuthFlowService;
import net.cryptic_game.auth.oauth.OAuthProvider;
import net.cryptic_game.auth.oauth.OAuthProvider.Metadata;
import net.cryptic_game.auth.oauth.OAuthService;
import net.cryptic_game.auth.user.UserService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class OAuthServiceImpl implements OAuthService {

  private final OAuthFlowService flowService;
  private final UserService userService;

  private final Map<String, OAuthProvider> providers;
  private final Set<Metadata> metadata;

  public OAuthServiceImpl(final OAuthFlowService flowService,
                          final UserService userService,
                          final Set<OAuthProvider> providers) {
    this.flowService = flowService;
    this.userService = userService;

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
  public URI buildAuthorizeUri(final String providerId, final String state) {
    final OAuthProvider provider = this.providers.get(providerId);

    if (provider == null) {
      return null;
    }

    return provider.buildAuthorizeUrl(state);
  }

  @Override
  public Mono<Void> cancelFlow(final UUID flowId) {
    return this.flowService.cancelFlow(flowId);
  }

  @Override
  public Mono<String> handleCallback(final UUID flowId, final String providerId, final String code) {
    final OAuthProvider provider = this.providers.get(providerId);

    return provider.handleCallback(code);
  }
}
