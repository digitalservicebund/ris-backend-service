package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.UserTransformer;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.UserGroup;
import de.bund.digitalservice.ris.caselaw.domain.UserGroupService;
import de.bund.digitalservice.ris.caselaw.domain.UserRepository;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import jakarta.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
@Primary
public class DatabaseUserService extends UserService {
  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseUserService.class);

  private final UserRepository userRepository;
  private final UserService keycloakUserService;

  public DatabaseUserService(
      UserGroupService userGroupService,
      UserRepository userRepository,
      @Qualifier("keycloakUserService") UserService keycloakUserService) {
    super(userGroupService);
    this.userRepository = userRepository;
    this.keycloakUserService = keycloakUserService;
  }

  /** Nightly fetches users from the Keycloak User Service and persists them */
  @Scheduled(cron = "0 0 4 * * *", zone = "Europe/Berlin")
  @SchedulerLock(name = "fetch-users-from-api", lockAtMostFor = "PT5M")
  @Transactional
  public void fetchAndPersistUsersFromKeycloak() {
    userGroupService
        .getAllUserGroups()
        .forEach(
            userGroup ->
                userRepository.saveOrUpdate(
                    keycloakUserService.getUsersInSameDocOffice(userGroup).stream()
                        .map(
                            user ->
                                user.toBuilder().documentationOffice(userGroup.docOffice()).build())
                        .toList()));
  }

  /**
   * Get user domain object from oidc user by their id
   *
   * @param oidcUser
   * @return
   */
  @Override
  public User getUser(OidcUser oidcUser) {
    return userRepository
        .findByExternalId(UserTransformer.getOidcUserId(oidcUser))
        .orElseGet(
            () -> userRepository.saveOrUpdate(keycloakUserService.getUser(oidcUser)).orElse(null));
  }

  /**
   * Get user by database uuid or, if not exists, by external id
   *
   * @param uuid the user's id or external id
   * @return the user
   */
  @Override
  public User getUser(UUID uuid) {
    if (uuid == null) {
      return null;
    }
    return userRepository
        .getUser(uuid)
        .orElse(userRepository.findByExternalId(uuid).orElse(keycloakUserService.getUser(uuid)));
  }

  @Override
  public List<User> getUsersInSameDocOffice(UserGroup userGroup) {
    if (userGroup == null || userGroup.docOffice() == null) {
      return Collections.emptyList();
    }
    return userRepository.getAllUsersForDocumentationOffice(userGroup.docOffice()).stream()
        .toList();
  }
}
