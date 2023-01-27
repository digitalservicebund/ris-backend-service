package de.bund.digitalservice.ris.caselaw.adapter;

import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  @GetMapping(value = "me")
  public Mono<ResponseEntity<?>> validateSession(
      @AuthenticationPrincipal Mono<OAuth2User> oauth2User) {
    if (oauth2User == null) {
      return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null));
    } else {
      return Mono.just(
          ResponseEntity.status(HttpStatus.OK)
              .body(Objects.requireNonNull(oauth2User.block()).getAttribute("name")));
    }
  }
}
