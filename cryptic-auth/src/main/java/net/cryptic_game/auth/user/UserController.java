package net.cryptic_game.auth.user;

import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

public interface UserController {

  @PostMapping(value = "name",
      consumes = MediaType.TEXT_PLAIN_VALUE,
      produces = MediaType.TEXT_PLAIN_VALUE)
  Mono<ResponseEntity<Void>> checkUsername(@RequestBody String name);

  @PostMapping(value = "register",
      consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
  Mono<Resource> register(
      @ModelAttribute RegisterBody body,
      @CookieValue(value = "register_token", required = false) String registerToken,
      @CookieValue(value = "flow", required = false) UUID flowId
  );

  record RegisterBody(String name) {

  }
}
