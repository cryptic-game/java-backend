package net.cryptic_game.auth.oauth.exception;

import de.m4rc3l.nova.core.exception.HttpException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public class UnknownOAuthProviderException extends HttpException {

  private static final String TYPE = "BAD_INPUT";
  private static final String MESSAGE_FORMAT = "Provider with id \"%s\" could not be found.";

  private final String providerId;

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.BAD_REQUEST;
  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public Map<String, String> getAdditionalProperties() {
    return Map.of("providerId", this.providerId);
  }

  @Override
  public String getMessage() {
    return String.format(MESSAGE_FORMAT, this.providerId);
  }
}
