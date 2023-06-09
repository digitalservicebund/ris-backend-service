package de.bund.digitalservice.ris.caselaw.adapter;

import static de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatus.PUBLISHED;

import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthService {

  private final UserService userService;
  private final DocumentUnitService documentUnitService;

  public AuthService(UserService userService, DocumentUnitService documentUnitService) {
    this.userService = userService;
    this.documentUnitService = documentUnitService;
  }

  public boolean userHasReadAccess(String documentNumber, OidcUser oidcUser) {
    DocumentUnit documentUnit = documentUnitService.getByDocumentNumber(documentNumber).block();
    if (documentUnit == null) {
      return false;
    }
    DocumentationOffice documentationOffice = userService.getDocumentationOffice(oidcUser).block();
    if (documentUnit.coreData().documentationOffice().equals(documentationOffice)) {
      return true;
    }
    return documentUnit.status() == PUBLISHED || documentUnit.status() == null;
  }
}
