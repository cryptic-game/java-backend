package net.cryptic_game.auth.jwt.impl;

import io.jsonwebtoken.Jwts;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import net.cryptic_game.auth.jwt.JwtConfig;
import net.cryptic_game.auth.jwt.JwtService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class JwtServiceImpl implements JwtService {

  private final Key key;
  private final JwtConfig config;

  public JwtServiceImpl(@Qualifier("jwtKey") final Key key, final JwtConfig config) {
    this.key = key;
    this.config = config;
  }

  @Override
  public String create(final UUID userId) {
    final Instant now = Instant.now();
    final Date nowDate = Date.from(now);

    return Jwts.builder()
        .setId(UUID.randomUUID().toString())
        .setSubject(userId.toString())
        .setIssuer(this.config.issuer())
        .setAudience(this.config.audience())
        .setIssuedAt(nowDate)
        .setNotBefore(nowDate)
        .setExpiration(Date.from(now.plus(this.config.lifetime())))
        .signWith(this.key)
        .compact();
  }
}
