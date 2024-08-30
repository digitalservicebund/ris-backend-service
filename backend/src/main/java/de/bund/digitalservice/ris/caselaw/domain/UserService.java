package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public interface UserService {
  User getUser(OidcUser oidcUser);

  Optional<DocumentationOfficeUserGroup> getUserGroup(OidcUser oidcUser);

  DocumentationOffice getDocumentationOffice(OidcUser oidcUser);

  List<DocumentationOfficeUserGroup> getUserGroups(OidcUser oidcUser);

  String getEmail(OidcUser oidcUser);

  Boolean isInternal(OidcUser oidcUser);
}
