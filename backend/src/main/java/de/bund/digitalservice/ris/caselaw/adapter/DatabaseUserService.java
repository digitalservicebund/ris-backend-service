package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.UserDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationOfficeTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.UserTransformer;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.UserGroup;
import de.bund.digitalservice.ris.caselaw.domain.UserGroupService;
import de.bund.digitalservice.ris.caselaw.domain.UserRepository;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
@Primary
public class DatabaseUserService extends UserService {
  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseUserService.class);

  private final UserRepository userRepository;
  private final KeycloakUserService keycloakUserService;

  public DatabaseUserService(
      UserGroupService userGroupService,
      UserRepository userRepository,
      KeycloakUserService keycloakUserService) {
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
                persistUsersOfDocOffice(
                    keycloakUserService.fetchUsers(userGroup),
                    DocumentationOfficeTransformer.transformToDTO(userGroup.docOffice())));
  }

  /**
   * Get user domain object from oidc user by their id
   *
   * @param oidcUser
   * @return
   */
  @Override
  public User getUser(OidcUser oidcUser) {
    return UserTransformer.transformToDomain(
        userRepository
            .findByExternalId(UserTransformer.getOidcUserId(oidcUser))
            .orElseGet(() -> fetchAndPersistUser(oidcUser)));
  }

  /**
   * Get user by database uuid or, if not exists, by external id
   *
   * @param uuid the user's id or external id
   * @return the user
   */
  @Override
  public User getUser(UUID uuid) {
    return UserTransformer.transformToDomain(
        userRepository.getUser(uuid).orElse(userRepository.findByExternalId(uuid).orElse(null)));
  }

  @Override
  public List<User> getAllUsersOfSameGroup(OidcUser oidcUser) {
    return getUserGroup(oidcUser)
        .map(UserGroup::docOffice)
        .map(DocumentationOfficeTransformer::transformToDTO)
        .map(
            officeDTO ->
                userRepository.getAllUsersForDocumentationOffice(officeDTO).stream()
                    .map(UserTransformer::transformToDomain)
                    .toList())
        .orElse(Collections.emptyList());
  }

  public void persistUsersOfDocOffice(
      List<User> users, @NotNull DocumentationOfficeDTO documentationOffice) {
    userRepository.saveOrUpdate(
        users.stream()
            .map(
                user ->
                    UserTransformer.transformToDTO(user).toBuilder()
                        .documentationOffice(documentationOffice)
                        .build())
            .toList());
  }

  /**
   * Fetches the oidc user from the keycloak service and persists them
   *
   * @param oidcUser the oidc user
   * @return the persisted user
   */
  public UserDTO fetchAndPersistUser(OidcUser oidcUser) {
    return userRepository
        .saveOrUpdate(UserTransformer.transformToDTO(keycloakUserService.getUser(oidcUser)))
        .orElse(null);
  }
}
