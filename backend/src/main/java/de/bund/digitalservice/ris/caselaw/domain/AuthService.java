package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public interface AuthService {
  boolean userHasWriteAccess(OidcUser oidcUser, DocumentationUnit documentationUnit);

  boolean userIsInternal(OidcUser oidcUser);

  boolean isAssignedViaProcedure(UUID uuid);
}
