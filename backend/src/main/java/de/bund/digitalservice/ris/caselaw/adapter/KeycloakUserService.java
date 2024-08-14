package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOfficeUserGroup;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOfficeUserGroupService;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class KeycloakUserService implements UserService {
  private final DocumentationOfficeUserGroupService documentationOfficeUserGroupService;

  public KeycloakUserService(
      DocumentationOfficeUserGroupService documentationOfficeUserGroupService) {
    this.documentationOfficeUserGroupService = documentationOfficeUserGroupService;
  }

  public User getUser(OidcUser oidcUser) {
    return extractDocumentationOffice(oidcUser)
        .map(documentationOffice -> createUser(oidcUser, documentationOffice))
        .orElse(createUser(oidcUser, null));
  }

  public DocumentationOffice getDocumentationOffice(OidcUser oidcUser) {
    return getUser(oidcUser).documentationOffice();
  }

  public String getEmail(OidcUser oidcUser) {
    return oidcUser.getEmail();
  }

  private User createUser(OidcUser oidcUser, DocumentationOffice documentationOffice) {
    return User.builder()
        .name(oidcUser.getAttribute("name"))
        .email(oidcUser.getEmail())
        .documentationOffice(documentationOffice)
        .build();
  }

  private Optional<DocumentationOffice> extractDocumentationOffice(OidcUser oidcUser) {
    List<String> groups = Objects.requireNonNull(oidcUser.getAttribute("groups"));
    return this.documentationOfficeUserGroupService
        .getFirstUserGroupWithDocOffice(groups)
        .map(DocumentationOfficeUserGroup::docOffice);
  }
}
