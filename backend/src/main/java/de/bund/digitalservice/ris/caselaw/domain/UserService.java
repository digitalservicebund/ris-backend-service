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

  public abstract Optional<User> getUser(OidcUser oidcUser);

  public abstract Optional<User> getUser(UUID uuid);

  public abstract List<User> getUsersInSameDocOffice(UserGroup userGroup);

  public List<User> getUsersInSameDocOffice(OidcUser oidcUser) {
    return getUsersInSameDocOffice(getUserGroup(oidcUser).orElse(null));
  }

  public Optional<DocumentationOffice> getDocumentationOffice(OidcUser oidcUser) {
    var optionalUser = getUser(oidcUser);
    if (optionalUser.isEmpty()) return Optional.empty();
    User user = optionalUser.get();
    if (user.documentationOffice() == null)
      LOGGER.warn("No doc office associated with optionalUser: {}", user.name());

    return Optional.ofNullable(user.documentationOffice());
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
    var matchingUserGroup = userGroupService.getUserGroupFromGroupPathNames(userGroups);
    if (matchingUserGroup.isEmpty()) {
      LOGGER.warn(
          "No doc office user group associated with given Keycloak user groups: {}", userGroups);
    }
    return matchingUserGroup;
  }
}
