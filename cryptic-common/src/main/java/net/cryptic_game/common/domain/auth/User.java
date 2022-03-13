package net.cryptic_game.common.domain.auth;

import java.time.OffsetDateTime;
import java.util.UUID;

public record User(UUID id,
                   String name,
                   OffsetDateTime created,
                   OffsetDateTime last) {

}
