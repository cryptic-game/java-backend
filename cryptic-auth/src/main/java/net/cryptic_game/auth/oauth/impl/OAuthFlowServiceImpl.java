package net.cryptic_game.auth.oauth.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import net.cryptic_game.auth.oauth.OAuthFlowService;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

@Service
@RequiredArgsConstructor
public class OAuthFlowServiceImpl implements OAuthFlowService {

  private static final String REDIS_CHANNEL = "oauth_flow";
  private final ReactiveRedisTemplate<String, OAuthFlowResponse> oauthRedisTemplate;

  private final Map<UUID, MonoSink<OAuthFlowResponse>> flows = new HashMap<>();

  @PostConstruct
  private void postConstruct() {
    this.receiveFlowResponses();
  }

  private void receiveFlowResponses() {
    this.oauthRedisTemplate.listenTo(new ChannelTopic(REDIS_CHANNEL))
        .subscribe(message -> {
          final OAuthFlowResponse response = message.getMessage();
          final MonoSink<OAuthFlowResponse> sink = this.flows.remove(response.flowId());
          if (sink != null) {
            sink.success(response);
          }
        });
  }

  @Override
  public Mono<OAuthFlowResponse> receiveFlowResponse(final UUID flowId) {
    return Mono.create(sink -> {
      this.flows.put(flowId, sink);
      sink.onDispose(() -> this.flows.remove(flowId));
    });
  }

  @Override
  public Mono<Void> successfulCallback(final UUID flowId, final String providerId, final String providerUserId) {
    final OAuthFlowResponse message = new OAuthFlowResponse(flowId, providerId, providerUserId);
    return this.oauthRedisTemplate.convertAndSend(REDIS_CHANNEL, message).then();
  }

  @Override
  public Mono<Void> cancelFlow(final UUID flowId) {
    return Mono.empty();
  }
}
