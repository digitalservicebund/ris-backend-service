package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseUserGroupRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.UserGroupDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.UserGroupTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.UserGroup;
import de.bund.digitalservice.ris.caselaw.domain.UserGroupService;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserGroupService implements UserGroupService {
  private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseUserGroupService.class);
  private final DatabaseUserGroupRepository repository;
  private List<UserGroupDTO> userGroups;

  public DatabaseUserGroupService(DatabaseUserGroupRepository repository) {
    this.repository = repository;
    this.userGroups = new ArrayList<>();
  }

  /**
   * On application start, we want to
   *
   * <ol>
   *   <li>load the user groups into memory for performance reasons (-> right checks)
   * </ol>
   */
  @EventListener
  public void onApplicationEvent(ContextRefreshedEvent event) {
    this.userGroups = this.repository.findAll();

    if (userGroups.isEmpty()) {
      throw new NoSuchElementException(
          "User groups must not be empty. Please check migration script");
    }
  }

  /**
   * Retrieves all {@link UserGroup user group} as domain objects.
   *
   * <p>This method transforms a list of user groups from internal representation to domain objects
   * using the {@link UserGroupTransformer}. It returns a list of these transformed user groups.
   *
   * @return a {@link List} of {@link UserGroup} in domain form
   */
  @Override
  public List<UserGroup> getAllUserGroups() {
    return this.userGroups.stream().map(UserGroupTransformer::transformToDomain).toList();
  }

  /**
   * Returns the first match where a provided list of {@link UserGroup user groups} contains a known
   * {@link UserGroup user group}. Examples:
   *
   * <ol>
   *   <li>Known groups: /ABC/DEF, /HIJ/KLM | provided groups: /HIJ/KLM, /OPQ -> result: /HIJ/KLM
   *   <li>Known groups: /ABC/DEF, /HIJ/KLM | provided groups: /HIJ/KLM, /ABC/DEF -> result:
   *       /ABC/DEF
   * </ol>
   *
   * @param userGroupPathNames the {@link UserGroup user groups} to match against the known {@link
   *     UserGroup user groups}
   * @return the first match or an empty optional if no match could be found
   */
  @Override
  public Optional<UserGroup> getUserGroupFromGroupPathNames(List<String> userGroupPathNames) {
    var uniqueUserGroups =
        this.getAllUserGroups().stream()
            .filter(group -> userGroupPathNames.contains(group.userGroupPathName()))
            .distinct()
            .toList();

    if (uniqueUserGroups.isEmpty()) {
      LOGGER.warn(
          "No doc office user group associated with given Keycloak user groups: {}", userGroups);
      return Optional.empty();
    }

    if (uniqueUserGroups.size() > 1) {
      LOGGER.warn(
          "More then one doc office associated with given Keycloak user groups: {}", userGroups);
      return Optional.empty();
    }

    return uniqueUserGroups.stream().findFirst();
  }

  /**
   * Retrieves a list of external {@link UserGroup user group} associated with a given {@link
   * DocumentationOffice}.
   *
   * <p>This method filters all {@link UserGroup user group} to find those associated with the
   * specified {@link DocumentationOffice} and that are not marked as internal. It returns a list of
   * these external {@link UserGroup user group}s.
   *
   * @param documentationOffice the {@link DocumentationOffice} for which external user groups are
   *     to be retrieved
   * @return a {@link List} of {@link UserGroup} that are external and associated with the given
   *     {@link DocumentationOffice}
   */
  @Override
  public List<UserGroup> getExternalUserGroups(DocumentationOffice documentationOffice) {
    return getAllUserGroups().stream()
        .filter(group -> group.docOffice().equals(documentationOffice) && !group.isInternal())
        .toList();
  }
}
