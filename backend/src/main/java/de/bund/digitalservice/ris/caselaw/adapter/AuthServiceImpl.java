package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.AuthService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import java.util.UUID;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
  private final de.bund.digitalservice.ris.caselaw.adapter.AuthService authService;

  public AuthServiceImpl(de.bund.digitalservice.ris.caselaw.adapter.AuthService authService) {
    this.authService = authService;
  }

  @Override
  public boolean userHasWriteAccess(OidcUser oidcUser, DocumentationUnit documentationUnit) {
    return authService.userHasWriteAccess(oidcUser, documentationUnit);
  }

  @Override
  public boolean userIsInternal(OidcUser oidcUser) {
    return authService.userIsInternal().apply(oidcUser);
  }

  @Override
  public boolean isAssignedViaProcedure(UUID uuid) {
    return authService.isAssignedViaProcedure().apply(uuid);
  }
}
