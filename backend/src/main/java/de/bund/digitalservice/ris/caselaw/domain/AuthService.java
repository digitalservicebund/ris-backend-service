package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;
import java.util.function.Function;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public interface AuthService {
  boolean userHasWriteAccess(OidcUser oidcUser, DocumentationUnit documentationUnit);

  boolean userHasWriteAccess(
      OidcUser oidcUser,
      DocumentationOffice creatingDocOffice,
      DocumentationOffice documentationOffice,
      Status status);

  Function<OidcUser, Boolean> userIsInternal();

  Function<UUID, Boolean> isAssignedViaProcedure();
}
