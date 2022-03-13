package net.cryptic_game.common.domain.auth;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserOAuth(UUID userId,
                        String providerId,
                        String providerUserId,
                        OffsetDateTime created,
                        OffsetDateTime last) {

}
