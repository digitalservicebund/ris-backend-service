package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.domain.User;
import java.util.UUID;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public class UserTransformer {

  private UserTransformer() {}

  public static User transformToDomain(OidcUser auth2User) {

    return User.builder()
        .id(UUID.fromString(auth2User.getIdToken().toString()))
        .name(auth2User.getUserInfo().getFullName())
        .email(auth2User.getUserInfo().getEmail())
        .build();
  }
}
