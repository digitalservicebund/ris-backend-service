package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.UserTransformer;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.UserGroup;
import de.bund.digitalservice.ris.caselaw.domain.UserGroupService;
import de.bund.digitalservice.ris.caselaw.domain.UserRepository;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
@Primary
public class DatabaseUserService extends UserService {

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

  /** On application start, we want to check if the user table is empty and if so, initialize */
  @EventListener
  public void onApplicationEvent(ContextRefreshedEvent event) {
    fetchAndPersistUsersFromKeycloak();
  }

  /** Nightly fetches users from the Keycloak User Service and persists them */
  @Scheduled(cron = "0 0 4 * * *", zone = "Europe/Berlin")
  @SchedulerLock(name = "fetch-users-from-api", lockAtMostFor = "PT5M")
  public void fetchAndPersistUsersFromKeycloak() {
    userGroupService
        .getAllUserGroups()
        .forEach(
            userGroup ->
                userRepository.saveOrUpdate(
                    keycloakUserService.getUsersInSameDocOffice(userGroup).stream()
                        .map(
                            user ->
                                user.toBuilder()
                                    .documentationOffice(userGroup.docOffice())
                                    .internal(userGroup.isInternal())
                                    .build())
                        .toList()));
  }

  /**
   * Retrieve user by the oidc user's id. First attempt to find in database. If it can't be found,
   * request from keycloak user service and persist it if found.
   *
   * @param oidcUser the oidc user
   * @return the user domain object
   */
  @Override
  public User getUser(OidcUser oidcUser) {
    User user =
        userRepository
            .findByExternalId(UserTransformer.getOidcUserId(oidcUser))
            .orElseGet(
                () ->
                    userRepository
                        .saveOrUpdate(keycloakUserService.getUser(oidcUser))
                        .orElse(null));
    /* The email address is currently needed for the scheduled publication. It is not yet part of the user table,
    so we add it via the oidc user here, so that the /auth/me endpoint returns the email address.
    In the future, this should be removed again and either the email address added to the user table
    or the need for email addresses / notifications replaced */
    return user != null ? user.toBuilder().email(oidcUser.getEmail()).build() : null;
  }

  /**
   * Aim to get the user from the database
   *
   * <ol>
   *   <li>By database uuid or, if not exists,
   *   <li>By external id or, if not exists, null
   * </ol>
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
        .orElseGet(() -> userRepository.findByExternalId(uuid).orElse(null));
  }

  /**
   * Get all users from database that belong to the same UserGroup
   *
   * @param userGroup the user group
   * @return all users found in the same group
   */
  @Override
  public List<User> getUsersInSameDocOffice(UserGroup userGroup) {
    if (userGroup == null || userGroup.docOffice() == null) {
      return Collections.emptyList();
    }
    return userRepository.getAllUsersForDocumentationOffice(userGroup.docOffice()).stream()
        .toList();
  }
}
