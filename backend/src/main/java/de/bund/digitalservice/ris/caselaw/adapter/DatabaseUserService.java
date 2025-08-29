package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseUserGroupRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.UserGroupDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.UserGroupTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.UserTransformer;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.UserGroup;
import de.bund.digitalservice.ris.caselaw.domain.UserRepository;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
@Primary
public class DatabaseUserService extends UserService {
  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseUserService.class);

  private final UserRepository userRepository;
  private final DatabaseUserGroupRepository databaseUserGroupRepository;

  public DatabaseUserService(
      UserRepository userRepository, DatabaseUserGroupRepository databaseUserGroupRepository) {
    this.userRepository = userRepository;
    this.databaseUserGroupRepository = databaseUserGroupRepository;
  }

  @Override
  public User getUser(OidcUser oidcUser) {
    return UserTransformer.transformToDomain(
        userRepository.findByFirstNameAndLastNameAndDocumentationOffice(
            oidcUser.getGivenName(),
            oidcUser.getFamilyName(),
            extractDocumentationOffice(oidcUser).orElse(null)));
  }

  @Override
  public User getUser(UUID uuid) {
    return UserTransformer.transformToDomain(userRepository.getUser(uuid).orElse(null));
  }

  @Override
  public List<User> getUsers(OidcUser oidcUser) {
    return extractDocumentationOffice(oidcUser)
        .map(
            officeDTO ->
                userRepository.getAllUsersForDocumentationOffice(officeDTO).stream()
                    .map(UserTransformer::transformToDomain)
                    .toList())
        .orElse(Collections.emptyList());
  }

  @Override
  public Optional<UserGroup> getUserGroup(OidcUser oidcUser) {
    return Optional.ofNullable(
        UserGroupTransformer.transformToDomain(getUserGroupDTO(oidcUser).orElse(null)));
  }

  @Override
  public void persistUsers(List<User> users) {
    userRepository.saveAll(users.stream().map(UserTransformer::transformToDTO).toList());
  }

  public Optional<UserGroupDTO> getUserGroupDTO(OidcUser oidcUser) {
    List<String> userGroups = Objects.requireNonNull(oidcUser.getAttribute("groups"));
    var matchingUserGroup =
        databaseUserGroupRepository.findAll().stream()
            .filter(group -> userGroups.contains(group.getUserGroupPathName()))
            .findFirst();
    if (matchingUserGroup.isEmpty()) {
      LOGGER.warn(
          "No doc office user group associated with given Keycloak user groups: {}", userGroups);
    }
    return matchingUserGroup;
  }

  private Optional<DocumentationOfficeDTO> extractDocumentationOffice(OidcUser oidcUser) {
    return getUserGroupDTO(oidcUser).map(UserGroupDTO::getDocumentationOffice);
  }
}
