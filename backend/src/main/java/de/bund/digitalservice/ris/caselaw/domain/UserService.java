package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public interface UserService {
  User getUser(OidcUser oidcUser);

  Optional<UserGroup> getUserGroup(OidcUser oidcUser);

  DocumentationOffice getDocumentationOffice(OidcUser oidcUser);

  String getEmail(OidcUser oidcUser);

  Boolean isInternal(OidcUser oidcUser);

  User getUser(UUID userId);

  List<User> getUsersInSameDocOffice(OidcUser oidcUser);
}
