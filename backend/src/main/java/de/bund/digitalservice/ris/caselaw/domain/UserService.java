package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public abstract class UserService {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

  protected final UserGroupService userGroupService;

  protected UserService(UserGroupService userGroupService) {
    this.userGroupService = userGroupService;
  }

  public abstract User getUser(OidcUser oidcUser);

  public abstract User getUser(UUID uuid);

  public abstract List<User> getAllUsersOfSameGroup(UserGroup userGroup);

  public DocumentationOffice getDocumentationOffice(OidcUser oidcUser) {
    User user = getUser(oidcUser);
    if (user == null) return null;
    if (user.documentationOffice() == null) {
      LOGGER.warn("No doc office associated with user: {}", user.name());
    }
    return user.documentationOffice();
  }

  public String getEmail(OidcUser oidcUser) {
    return oidcUser.getEmail();
  }

  public Boolean isInternal(OidcUser oidcUser) {
    List<String> roles = oidcUser.getClaimAsStringList("roles");
    if (roles != null) {
      return roles.contains("Internal");
    }
    return false;
  }

  public Optional<UserGroup> getUserGroup(OidcUser oidcUser) {
    List<String> userGroups = Objects.requireNonNull(oidcUser.getAttribute("groups"));
    var matchingUserGroup = userGroupService.getFirstUserGroup(userGroups);
    if (matchingUserGroup.isEmpty()) {
      LOGGER.warn(
          "No doc office user group associated with given Keycloak user groups: {}", userGroups);
    }
    return matchingUserGroup;
  }
}
