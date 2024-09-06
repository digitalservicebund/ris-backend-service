package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeUserGroupRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeUserGroupDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationOfficeUserGroupTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOfficeUserGroup;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOfficeUserGroupService;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class DatabaseDocumentationOfficeUserGroupService
    implements DocumentationOfficeUserGroupService {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(DatabaseDocumentationOfficeUserGroupService.class);
  private final DatabaseDocumentationOfficeUserGroupRepository repository;
  private final DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  private List<DocumentationOfficeUserGroupDTO> documentationOfficeUserGroups;

  private final List<DocumentationOfficeConfigUserGroup> userGroupsFromConfig;

  public DatabaseDocumentationOfficeUserGroupService(
      DatabaseDocumentationOfficeUserGroupRepository repository,
      DatabaseDocumentationOfficeRepository documentationOfficeRepository,
      List<DocumentationOfficeConfigUserGroup> documentationOfficeConfigUserGroups) {
    this.repository = repository;
    this.documentationOfficeRepository = documentationOfficeRepository;
    this.documentationOfficeUserGroups = new ArrayList<>();
    this.userGroupsFromConfig = documentationOfficeConfigUserGroups;
  }

  /**
   * On application start, we want to
   *
   * <ol>
   *   <li>sync the user group database with the statically configured list of user groups
   *   <li>load the user groups into memory for performance reasons (-> right checks)
   * </ol>
   */
  @EventListener
  public void onApplicationEvent(ContextRefreshedEvent event) {
    this.documentationOfficeUserGroups = this.repository.findAll();
    var docOffices = this.documentationOfficeRepository.findAll();

    var userGroupsToBeDeleted =
        documentationOfficeUserGroups.stream().filter(this::groupIsNotInConfig).toList();
    if (!userGroupsToBeDeleted.isEmpty()) {
      String groupsString = groupsToString(userGroupsToBeDeleted);
      LOGGER.info("Deleting doc office user groups: {}", groupsString);
      this.repository.deleteAll(userGroupsToBeDeleted);
    }

    var userGroupsToBeCreated =
        userGroupsFromConfig.stream()
            .filter(this::isGroupNotInDatabase)
            .map(groupFromConfig -> transformToDTO(groupFromConfig, docOffices))
            .toList();
    if (!userGroupsToBeCreated.isEmpty()) {
      String groupsString = groupsToString(userGroupsToBeCreated);
      LOGGER.info("Creating new doc office user groups: {}", groupsString);
      this.repository.saveAll(userGroupsToBeCreated);
    }

    this.documentationOfficeUserGroups = this.repository.findAll();
  }

  /**
   * Retrieves all {@link DocumentationOfficeUserGroup user group} as domain objects.
   *
   * <p>This method transforms a list of user groups from internal representation to domain objects
   * using the {@link DocumentationOfficeUserGroupTransformer}. It returns a list of these
   * transformed user groups.
   *
   * @return a {@link List} of {@link DocumentationOfficeUserGroup} in domain form
   */
  @Override
  public List<DocumentationOfficeUserGroup> getAllUserGroups() {
    return this.documentationOfficeUserGroups.stream()
        .map(DocumentationOfficeUserGroupTransformer::transformToDomain)
        .toList();
  }

  /**
   * Retrieves a list of external {@link DocumentationOfficeUserGroup user group} associated with a
   * given {@link DocumentationOffice}.
   *
   * <p>This method filters all {@link DocumentationOfficeUserGroup user group} to find those
   * associated with the specified {@link DocumentationOffice} and that are not marked as internal.
   * It returns a list of these external {@link DocumentationOfficeUserGroup user group}s.
   *
   * @param documentationOffice the {@link DocumentationOffice} for which external user groups are
   *     to be retrieved
   * @return a {@link List} of {@link DocumentationOfficeUserGroup} that are external and associated
   *     with the given {@link DocumentationOffice}
   */
  @Override
  public List<DocumentationOfficeUserGroup> getExternalUserGroups(
      DocumentationOffice documentationOffice) {
    return getAllUserGroups().stream()
        .filter(group -> group.docOffice().equals(documentationOffice) && !group.isInternal())
        .toList();
  }

  private boolean isGroupNotInDatabase(DocumentationOfficeConfigUserGroup groupFromConfig) {
    return this.documentationOfficeUserGroups.stream()
        .noneMatch(groupFromDb -> isConfigEqualToGroupFromDb(groupFromConfig, groupFromDb));
  }

  private boolean groupIsNotInConfig(DocumentationOfficeUserGroupDTO groupFromDb) {
    return this.userGroupsFromConfig.stream()
        .noneMatch(groupFromConfig -> isConfigEqualToGroupFromDb(groupFromConfig, groupFromDb));
  }

  private boolean isConfigEqualToGroupFromDb(
      DocumentationOfficeConfigUserGroup groupFromConfig,
      DocumentationOfficeUserGroupDTO groupFromDb) {
    return groupFromConfig.userGroupPathName().equals(groupFromDb.getUserGroupPathName())
        && groupFromConfig.isInternal() == groupFromDb.isInternal()
        && groupFromConfig
            .docOfficeAbbreviation()
            .equals(groupFromDb.getDocumentationOffice().getAbbreviation());
  }

  /**
   * Will throw if a doc office for a configured user group does not exist -> Application won't
   * start. Make sure to mock this class in tests.
   */
  private @NotNull DocumentationOfficeDTO getMatchingDocumentationOffice(
      DocumentationOfficeConfigUserGroup groupFromConfig, List<DocumentationOfficeDTO> docOffices) {
    return docOffices.stream()
        .filter(office -> office.getAbbreviation().equals(groupFromConfig.docOfficeAbbreviation()))
        .findFirst()
        .orElseThrow();
  }

  private DocumentationOfficeUserGroupDTO transformToDTO(
      DocumentationOfficeConfigUserGroup groupFromConfig, List<DocumentationOfficeDTO> docOffices) {
    return DocumentationOfficeUserGroupDTO.builder()
        .documentationOffice(this.getMatchingDocumentationOffice(groupFromConfig, docOffices))
        .userGroupPathName(groupFromConfig.userGroupPathName())
        .isInternal(groupFromConfig.isInternal())
        .build();
  }

  private static String groupsToString(
      List<DocumentationOfficeUserGroupDTO> userGroupsToBeCreated) {
    return userGroupsToBeCreated.stream()
        .map(DocumentationOfficeUserGroupDTO::getUserGroupPathName)
        .collect(Collectors.joining(", "));
  }
}
