package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

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
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class DatabaseUserGroupServiceTest {

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

  private DatabaseUserGroupService service;

  @MockitoBean private DatabaseUserGroupRepository groupRepository;
  @MockitoBean private DatabaseDocumentationOfficeRepository officeRepository;

  @Test
  void shouldThrowErrorWhenConfigIsEmpty() {
    when(groupRepository.findAll()).thenReturn(List.of());

    DatabaseUserGroupService databaseUserGroupService =
        new DatabaseUserGroupService(groupRepository);

    var event = new ContextRefreshedEvent(new StaticApplicationContext());
    assertThrows(
        NoSuchElementException.class, () -> databaseUserGroupService.onApplicationEvent(event));
  }

  @Test
  void shouldIdleWhenDatabaseGroupsMatchConfiguration() {

    doReturn(List.of(dsInternalGroupDTO, dsExternalGroupDTO)).when(this.groupRepository).findAll();
    doReturn(List.of(bghDocOffice, dsDocOffice)).when(this.officeRepository).findAll();

    this.service = new DatabaseUserGroupService(groupRepository);

    this.service.onApplicationEvent(null);

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

    this.service = new DatabaseUserGroupService(groupRepository);
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
        // Case sensitivity check
        Arguments.of(List.of("/a"), List.of("/A"), null));
  }

  @Test
  void shouldThrowWhenMoreThanOneMatchingGroup() {
    doReturn(List.of(dsInternalGroupDTO, dsExternalGroupDTO)).when(groupRepository).findAll();
    doReturn(List.of(dsDocOffice)).when(officeRepository).findAll();

    this.service = new DatabaseUserGroupService(groupRepository);

    service.onApplicationEvent(null);
    var pathNames = List.of("/DS");

    Optional<UserGroup> userGroups = service.getUserGroupFromGroupPathNames(pathNames);

    assertThat(userGroups).isEmpty();
  }
}
