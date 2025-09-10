package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDSDocOffice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

import de.bund.digitalservice.ris.caselaw.domain.UserGroup;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = {"classpath:doc_office_init.sql", "classpath:user_group_init.sql"})
@Sql(
    scripts = {"classpath:procedures_cleanup.sql"},
    executionPhase = AFTER_TEST_METHOD)
class UserGroupIntegrationTest extends BaseIntegrationTest {

  @Autowired private RisWebTestClient risWebTestClient;
  @MockitoBean private UserService userService;

  UserGroup internalUserGroup =
      UserGroup.builder()
          .docOffice(buildDSDocOffice())
          .userGroupPathName("/DS/Intern")
          .isInternal(true)
          .build();

  UserGroup externalUserGroup =
      UserGroup.builder()
          .docOffice(buildDSDocOffice())
          .userGroupPathName("/DS/Extern")
          .isInternal(false)
          .build();
  UserGroup externalUserGroup1 =
      UserGroup.builder()
          .docOffice(buildDSDocOffice())
          .userGroupPathName("/DS/Extern/Agentur1")
          .isInternal(false)
          .build();
  UserGroup externalUserGroup2 =
      UserGroup.builder()
          .docOffice(buildDSDocOffice())
          .userGroupPathName("/DS/Extern/Agentur2")
          .isInternal(false)
          .build();

  @BeforeEach()
  void beforeEach() {
    doReturn(List.of(internalUserGroup, externalUserGroup, externalUserGroup1, externalUserGroup2))
        .when(userGroupService)
        .getAllUserGroups();
  }

  @Test
  void testGetUserGroups_withInternalUser_shouldReturnExternalUserGroupsOfDocOffice() {
    doReturn(Optional.of(buildDSDocOffice())).when(userService).getDocumentationOffice(any());
    doReturn(List.of(externalUserGroup, externalUserGroup1, externalUserGroup2))
        .when(userGroupService)
        .getExternalUserGroups(any());

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/user-group")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(UserGroup[].class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).hasSize(3);
              assertThat(response.getResponseBody()[0]).isEqualTo(externalUserGroup);
              assertThat(response.getResponseBody()[1]).isEqualTo(externalUserGroup1);
              assertThat(response.getResponseBody()[2]).isEqualTo(externalUserGroup2);
            });
  }

  @Test
  void testGetUserGroups_withExternalUser_shouldReturnExternalUserGroupsOfDocOffice() {
    doReturn(Optional.of(buildDSDocOffice())).when(userService).getDocumentationOffice(any());
    doReturn(List.of(externalUserGroup, externalUserGroup1, externalUserGroup2))
        .when(userGroupService)
        .getExternalUserGroups(any());

    risWebTestClient
        .withExternalLogin()
        .get()
        .uri("/api/v1/caselaw/user-group")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(UserGroup[].class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).hasSize(3);
              assertThat(response.getResponseBody()[0]).isEqualTo(externalUserGroup);
              assertThat(response.getResponseBody()[1]).isEqualTo(externalUserGroup1);
              assertThat(response.getResponseBody()[2]).isEqualTo(externalUserGroup2);
            });
  }

  @Test
  void testGetUserGroups_withExternalUser_shouldReturnNoUserGroups() {
    doReturn(Optional.of(buildDSDocOffice())).when(userService).getDocumentationOffice(any());
    doReturn(List.of()).when(userGroupService).getExternalUserGroups(any());

    risWebTestClient
        .withLogin("/NOT-EXISTING")
        .get()
        .uri("/api/v1/caselaw/user-group")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(UserGroup[].class)
        .consumeWith(response -> assertThat(response.getResponseBody()).isEmpty());
  }
}
