package net.cryptic_game.auth.oauth;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface OAuthFlowService {

  Mono<OAuthFlowResponse> receiveFlowResponse(UUID flowId);

  Mono<Void> successfulCallback(UUID flowId, String providerId, String providerUserId);

  Mono<Void> cancelFlow(UUID flowId);

  record OAuthFlowResponse(UUID flowId, String providerId, String providerUserId) {

  }
}
