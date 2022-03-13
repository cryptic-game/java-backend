package net.cryptic_game.auth.oauth;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface OAuthFlowService {

  Mono<String> receiveFlowResponse(UUID flowId);

  Mono<Void> successfulCallback(UUID flowId, UUID userId);

  Mono<Void> cancelFlow(UUID flowId);

  record OAuthFlowResponse(UUID flowId, UUID userId) {

  }
}
