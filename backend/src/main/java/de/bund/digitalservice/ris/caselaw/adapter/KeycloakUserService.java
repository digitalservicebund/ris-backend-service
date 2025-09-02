package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.UserTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.UserApiService;
import de.bund.digitalservice.ris.caselaw.domain.UserGroup;
import de.bund.digitalservice.ris.caselaw.domain.UserGroupService;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class KeycloakUserService extends UserService {
  private static final Logger LOGGER = LoggerFactory.getLogger(KeycloakUserService.class);
  private final UserApiService userApiService;

  public KeycloakUserService(UserGroupService userGroupService, UserApiService userApiService) {
    super(userGroupService);
    this.userApiService = userApiService;
  }

  /**
   * Creates a domain user object from a oidc user with their documentation office
   *
   * @param oidcUser the oidc user
   * @return the user domain object including the documentation office
   */
  @Override
  public User getUser(OidcUser oidcUser) {
    DocumentationOffice documentationOffice = getDocumentationOffice(oidcUser);
    if (documentationOffice == null) {
      return null;
    }
    return UserTransformer.transformToDomain(oidcUser, documentationOffice);
  }

  @Override
  public DocumentationOffice getDocumentationOffice(OidcUser oidcUser) {
    return getUserGroup(oidcUser).map(UserGroup::docOffice).orElse(null);
  }

  @Override
  public User getUser(UUID uuid) {
    LOGGER.info("Fetching user with uuid {}", uuid);
    return userApiService.getUser(uuid);
  }

  @Override
  public List<User> getUsersInSameDocOffice(UserGroup userGroup) {
    if (userGroup == null || userGroup.userGroupPathName() == null) {
      return Collections.emptyList();
    }
    try {
      LOGGER.info("Fetching all users for group {}", userGroup.userGroupPathName());
      return userApiService.getUsers(userGroup.userGroupPathName());
    } catch (Exception e) {
      LOGGER.error("Error reading group user information: ", e);
      return Collections.emptyList();
    }
  }
}
