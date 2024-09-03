package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOfficeUserGroup;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOfficeUserGroupService;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class KeycloakUserService implements UserService {
  private static final Logger LOGGER = LoggerFactory.getLogger(KeycloakUserService.class);
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

  public List<DocumentationOfficeUserGroup> getUserGroups(OidcUser oidcUser) {
    return documentationOfficeUserGroupService.getUserGroups().stream()
        .filter(
            group ->
                group.docOffice().equals(getDocumentationOffice(oidcUser)) && !group.isInternal())
        .toList();
  }

  public String getEmail(OidcUser oidcUser) {
    return oidcUser.getEmail();
  }

  private User createUser(OidcUser oidcUser, DocumentationOffice documentationOffice) {
    return User.builder()
        .name(oidcUser.getAttribute("name"))
        .email(oidcUser.getEmail())
        .documentationOffice(documentationOffice)
        .roles(oidcUser.getClaimAsStringList("roles"))
        .build();
  }

  private Optional<DocumentationOffice> extractDocumentationOffice(OidcUser oidcUser) {
    List<String> userGroups = Objects.requireNonNull(oidcUser.getAttribute("groups"));
    var matchingUserGroup =
        this.documentationOfficeUserGroupService.getUserGroups().stream()
            .filter(group -> userGroups.contains(group.userGroupPathName()))
            .findFirst();
    if (matchingUserGroup.isEmpty() && !userGroups.isEmpty()) {
      LOGGER.warn(
          "No doc office user group associated with given Keycloak user groups: {}", userGroups);
    }
    return matchingUserGroup.map(DocumentationOfficeUserGroup::docOffice);
  }
}
