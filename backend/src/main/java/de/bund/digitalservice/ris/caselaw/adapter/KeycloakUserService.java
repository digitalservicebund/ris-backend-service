package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.UserApiService;
import de.bund.digitalservice.ris.caselaw.domain.UserGroup;
import de.bund.digitalservice.ris.caselaw.domain.UserGroupService;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class KeycloakUserService extends UserService {
  private static final Logger LOGGER = LoggerFactory.getLogger(KeycloakUserService.class);
  private final UserGroupService userGroupService;
  private final UserApiService userApiService;

  public KeycloakUserService(UserGroupService userGroupService, UserApiService userApiService) {
    this.userGroupService = userGroupService;
    this.userApiService = userApiService;
  }

  @Override
  public User getUser(OidcUser oidcUser) {
    return getUserGroup(oidcUser)
        .map(UserGroup::docOffice)
        .map(documentationOffice -> createUser(oidcUser, documentationOffice))
        .orElse(createUser(oidcUser, null));
  }

  @Override
  public DocumentationOffice getDocumentationOffice(OidcUser oidcUser) {
    return getUser(oidcUser).documentationOffice();
  }

  @Override
  public User getUser(UUID uuid) {
    LOGGER.info("Fetching user with uuid {}", uuid);
    return userApiService.getUser(uuid);
  }

  @Override
  public List<User> getUsers(OidcUser oidcUser) {
    var optionalUserGroup = getUserGroup(oidcUser);
    if (optionalUserGroup.isPresent()) {
      return getUsers(optionalUserGroup.get());
    } else {
      return Collections.emptyList();
    }
  }

  public List<User> getUsers(UserGroup group) {
    try {
      LOGGER.info("Fetching all users for group {}", group.userGroupPathName());
      return userApiService.getUsers(group.userGroupPathName());

    } catch (Exception e) {
      LOGGER.error("Error reading group user information: ", e);
      return Collections.emptyList();
    }
  }

  @Override
  public Optional<UserGroup> getUserGroup(OidcUser oidcUser) {
    List<String> userGroups = Objects.requireNonNull(oidcUser.getAttribute("groups"));
    var matchingUserGroup =
        this.userGroupService.getAllUserGroups().stream()
            .filter(group -> userGroups.contains(group.userGroupPathName()))
            .findFirst();
    if (matchingUserGroup.isEmpty()) {
      LOGGER.warn(
          "No doc office user group associated with given Keycloak user groups: {}", userGroups);
    }
    return matchingUserGroup;
  }
}
