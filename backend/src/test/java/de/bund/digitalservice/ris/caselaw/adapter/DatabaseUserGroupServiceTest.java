package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseUserGroupRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.UserGroupDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.UserGroupTransformer;
import de.bund.digitalservice.ris.caselaw.domain.UserGroup;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({
  DatabaseUserGroupService.class,
  DatabaseDocumentationOfficeRepository.class,
  DatabaseUserGroupRepository.class,
})
class DatabaseUserGroupServiceTest {

  private final UserGroupFromConfig dsInternalGroupConfig =
      UserGroupFromConfig.builder()
          .docOfficeAbbreviation("DS")
          .userGroupPathName("/DS")
          .isInternal(true)
          .build();
  private final UserGroupFromConfig dsExternalGroupConfig =
      UserGroupFromConfig.builder()
          .docOfficeAbbreviation("DS")
          .userGroupPathName("/DS")
          .isInternal(false)
          .build();

  private final DocumentationOfficeDTO dsDocOffice =
      DocumentationOfficeDTO.builder().abbreviation("DS").id(UUID.randomUUID()).build();
  private final DocumentationOfficeDTO bghDocOffice =
      DocumentationOfficeDTO.builder().abbreviation("BGH").id(UUID.randomUUID()).build();

  private final UserGroupDTO dsInternalGroupDTO =
      UserGroupDTO.builder()
          .documentationOffice(dsDocOffice)
          .isInternal(true)
          .userGroupPathName("/DS")
          .build();
  private final UserGroupDTO dsExternalGroupDTO =
      UserGroupDTO.builder()
          .documentationOffice(dsDocOffice)
          .isInternal(false)
          .userGroupPathName("/DS")
          .build();
  private final UserGroupDTO bghInternalGroupDTO =
      UserGroupDTO.builder()
          .documentationOffice(bghDocOffice)
          .isInternal(true)
          .userGroupPathName("/BGH")
          .build();

  private DatabaseUserGroupService service;

  @MockitoBean private DatabaseDocumentationOfficeRepository officeRepository;
  @MockitoBean private DatabaseUserGroupRepository groupRepository;

  @Test
  void shouldHandleEmptyConfig() {
    doReturn(List.of()).when(this.groupRepository).findAll();
    doReturn(List.of()).when(this.officeRepository).findAll();
    List<UserGroupFromConfig> configuredGroups = List.of();
    this.service =
        new DatabaseUserGroupService(groupRepository, officeRepository, configuredGroups);

    this.service.onApplicationEvent(null);

    verify(this.groupRepository, never()).saveAll(any());
    verify(this.groupRepository, never()).deleteAll(any());
  }

  @Test
  void shouldThrowOnNonExistentDocOffice() {
    doReturn(List.of()).when(this.groupRepository).findAll();
    doReturn(List.of()).when(this.officeRepository).findAll();
    List<UserGroupFromConfig> configuredGroups = List.of(dsInternalGroupConfig);
    this.service =
        new DatabaseUserGroupService(groupRepository, officeRepository, configuredGroups);

    assertThatThrownBy(() -> this.service.onApplicationEvent(null))
        .isInstanceOf(NoSuchElementException.class);

    verify(this.groupRepository, never()).saveAll(any());
    verify(this.groupRepository, never()).deleteAll(any());
  }

  @Test
  void shouldCreateNewGroup() {
    doReturn(List.of()).when(this.groupRepository).findAll();
    doReturn(List.of(dsDocOffice)).when(this.officeRepository).findAll();
    List<UserGroupFromConfig> configuredGroups = List.of(dsInternalGroupConfig);
    this.service =
        new DatabaseUserGroupService(groupRepository, officeRepository, configuredGroups);

    this.service.onApplicationEvent(null);

    verify(this.groupRepository, times(1)).saveAll(List.of(dsInternalGroupDTO));
    verify(this.groupRepository, never()).deleteAll(any());
  }

  @Test
  void shouldDeleteObsoleteGroup() {
    doReturn(List.of(dsInternalGroupDTO)).when(this.groupRepository).findAll();
    doReturn(List.of(dsDocOffice)).when(this.officeRepository).findAll();
    List<UserGroupFromConfig> configuredGroups = List.of();
    this.service =
        new DatabaseUserGroupService(groupRepository, officeRepository, configuredGroups);

    this.service.onApplicationEvent(null);

    verify(this.groupRepository, times(1)).deleteAll(List.of(dsInternalGroupDTO));
    verify(this.groupRepository, never()).saveAll(any());
  }

  @Test
  void shouldRecreateGroupWithNewConfig_InternalFlagChanged() {
    doReturn(List.of(dsInternalGroupDTO)).when(this.groupRepository).findAll();
    doReturn(List.of(dsDocOffice)).when(this.officeRepository).findAll();
    List<UserGroupFromConfig> configuredGroups = List.of(dsExternalGroupConfig);
    this.service =
        new DatabaseUserGroupService(groupRepository, officeRepository, configuredGroups);

    this.service.onApplicationEvent(null);

    verify(this.groupRepository, times(1)).deleteAll(List.of(dsInternalGroupDTO));
    verify(this.groupRepository, times(1)).saveAll(List.of(dsExternalGroupDTO));
  }

  @Test
  void shouldRecreateGroupWithNewConfig_DocOfficeChanged() {
    doReturn(List.of(bghInternalGroupDTO)).when(this.groupRepository).findAll();
    doReturn(List.of(dsDocOffice, bghDocOffice)).when(this.officeRepository).findAll();
    List<UserGroupFromConfig> configuredGroups = List.of(dsInternalGroupConfig);
    this.service =
        new DatabaseUserGroupService(groupRepository, officeRepository, configuredGroups);

    this.service.onApplicationEvent(null);

    verify(this.groupRepository, times(1)).deleteAll(List.of(bghInternalGroupDTO));
    verify(this.groupRepository, times(1)).saveAll(List.of(dsInternalGroupDTO));
  }

  @Test
  void shouldIdleWhenDatabaseGroupsMatchConfiguration() {
    doReturn(List.of(dsInternalGroupDTO, dsExternalGroupDTO)).when(this.groupRepository).findAll();
    doReturn(List.of(bghDocOffice, dsDocOffice)).when(this.officeRepository).findAll();
    List<UserGroupFromConfig> configuredGroups =
        List.of(dsExternalGroupConfig, dsInternalGroupConfig);
    this.service =
        new DatabaseUserGroupService(groupRepository, officeRepository, configuredGroups);

    this.service.onApplicationEvent(null);

    verify(this.groupRepository, never()).saveAll(any());
    verify(this.groupRepository, never()).deleteAll(any());
    var expectedGroups =
        Stream.of(dsInternalGroupDTO, dsExternalGroupDTO)
            .map(UserGroupTransformer::transformToDomain)
            .toList();
    assertThat(this.service.getAllUserGroups()).isEqualTo(expectedGroups);
  }

  @ParameterizedTest
  @MethodSource("provideUserGroupMatchingTestData")
  void shouldReturnFirstMatchingUserGroup(
      List<String> knownUserGroups, List<String> providedUserGroups, String expectedMatch) {
    doReturn(
            knownUserGroups.stream()
                .map(
                    pathName ->
                        UserGroupDTO.builder()
                            .documentationOffice(dsDocOffice)
                            .isInternal(true)
                            .userGroupPathName(pathName)
                            .build())
                .toList())
        .when(this.groupRepository)
        .findAll();

    this.service = new DatabaseUserGroupService(groupRepository, officeRepository, List.of());
    this.service.onApplicationEvent(null);

    var result =
        this.service
            .getUserGroupFromGroupPathNames(providedUserGroups)
            .map(UserGroup::userGroupPathName);

    assertThat(result).isEqualTo(Optional.ofNullable(expectedMatch));
  }

  private static Stream<Arguments> provideUserGroupMatchingTestData() {
    return Stream.of(
        // Exact match at the beginning of the list
        Arguments.of(List.of("/A", "/B", "/C"), List.of("/A", "/D"), "/A"),
        // Exact match in the middle of the list
        Arguments.of(List.of("/A", "/B", "/C"), List.of("/D", "/B"), "/B"),
        // Exact match at the end of the list
        Arguments.of(List.of("/A", "/B", "/C"), List.of("/D", "/C"), "/C"),
        // No match found
        Arguments.of(List.of("/A", "/B"), List.of("/C", "/D"), null),
        // Empty provided groups list
        Arguments.of(List.of("/A", "/B"), List.of(), null),
        // Empty known groups list
        Arguments.of(List.of(), List.of("/A", "/B"), null),
        // Case sensitivity check
        Arguments.of(List.of("/a"), List.of("/A"), null));
  }

  @Test
  void shouldThrowWhenMoreThanOneMatchingGroup() {
    doReturn(List.of(dsInternalGroupDTO, dsExternalGroupDTO)).when(groupRepository).findAll();
    doReturn(List.of(dsDocOffice)).when(officeRepository).findAll();

    service =
        new DatabaseUserGroupService(
            groupRepository,
            officeRepository,
            List.of(dsInternalGroupConfig, dsExternalGroupConfig));

    service.onApplicationEvent(null);
    var pathNames = List.of("/DS");

    Optional<UserGroup> userGroups = service.getUserGroupFromGroupPathNames(pathNames);

    assertThat(userGroups).isEmpty();
  }
}
