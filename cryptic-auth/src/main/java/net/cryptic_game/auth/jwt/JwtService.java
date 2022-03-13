package net.cryptic_game.auth.jwt;

import java.util.UUID;

public interface JwtService {

  String create(UUID userId);
}
