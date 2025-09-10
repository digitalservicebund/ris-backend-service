package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import de.bund.digitalservice.ris.caselaw.TestMemoryAppender;
import de.bund.digitalservice.ris.caselaw.adapter.BareUserApiResponse.BareUser;
import de.bund.digitalservice.ris.caselaw.adapter.BareUserApiResponse.Group;
import de.bund.digitalservice.ris.caselaw.adapter.BareUserApiResponse.GroupApiResponse;
import de.bund.digitalservice.ris.caselaw.adapter.BareUserApiResponse.GroupResponse;
import de.bund.digitalservice.ris.caselaw.adapter.BareUserApiResponse.UserApiResponse;
import de.bund.digitalservice.ris.caselaw.adapter.BareUserApiResponse.UsersApiResponse;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.UserApiException;
import de.bund.digitalservice.ris.caselaw.domain.UserGroup;
import de.bund.digitalservice.ris.caselaw.domain.UserGroupService;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(SpringExtension.class)
@Import({BareIdUserApiService.class})
class BareIdUserApiServiceTest {

  BareIdUserApiService bareIdUserApiService;

  @MockitoBean RestTemplate restTemplate;
  @MockitoBean BareIdUserApiTokenService bareIdUserApiTokenService;
  @MockitoBean UserGroupService userGroupService;

  String instanceId = UUID.randomUUID().toString();

  private final BareUserApiResponse.Group caselawGroup =
      generateBareUserGroup(
          UUID.fromString("00000000-0000-0000-0000-000000000001"), "caselaw", "/caselaw");

  private final BareUserApiResponse.Group courtGroup =
      generateBareUserGroup(
          UUID.fromString("00000000-0000-0000-0000-000000000002"), "BGH", "/caselaw/BGH");

  private final BareUserApiResponse.Group internGroup =
      generateBareUserGroup(
          UUID.fromString("00000000-0000-0000-0000-000000000003"), "Intern", "/caselaw/BGH/Intern");

  private TestMemoryAppender memoryAppender;

  @BeforeEach
  void setUp() {
    bareIdUserApiService =
        new BareIdUserApiService(
            bareIdUserApiTokenService, userGroupService, restTemplate, instanceId);

    mockTokenResponse();
    mockEmptyUserApiResponse(); // return empty list for bgh
    mockInternalUserApiResponse(); // returns user for "intern"

    // caselaw/BGH/Intern
    mockRootGroupsResponse(); // returns top-level groups, including "caselaw"
    mockCaselawChildrenResponse(); // returns caselaw children ("BGH")
    mockCourtChildrenResponse(); // returns BGH children, including "Intern"
    mockInternResponse(); // returns Intern's children (empty)

    memoryAppender = new TestMemoryAppender(BareIdUserApiService.class);
  }

  @AfterEach
  void tearDown() {
    memoryAppender.detachLoggingTestAppender();
  }

  @Nested
  class GetUser {

    @Test
    void testGetUser_shouldSucceed() throws UserApiException {
      final UUID userId = UUID.randomUUID();

      var attributes =
          Map.of(
              "firstName", new BareUserApiResponse.AttributeValues(List.of("Foo")),
              "lastName", new BareUserApiResponse.AttributeValues(List.of("Taxpayer")));

      BareUserApiResponse.BareUser user = generateBareUser(userId, attributes);
      BareUserApiResponse.UserApiResponse userResponse =
          new BareUserApiResponse.UserApiResponse(user);

      ResponseEntity<BareUserApiResponse.UserApiResponse> mockResponse =
          ResponseEntity.ok(userResponse);

      doReturn(mockResponse)
          .when(restTemplate)
          .exchange(
              anyString(),
              eq(HttpMethod.GET),
              any(HttpEntity.class),
              eq(BareUserApiResponse.UserApiResponse.class));

      when(userGroupService.getUserGroupFromGroupPathNames(List.of("/caselaw")))
          .thenReturn(
              Optional.of(
                  UserGroup.builder()
                      .isInternal(false)
                      .docOffice(DocumentationOffice.builder().abbreviation("BFH").build())
                      .build()));

      var userResult = bareIdUserApiService.getUser(userId);

      Assertions.assertEquals("Foo Taxpayer", userResult.name());
      Assertions.assertEquals("FT", userResult.initials());
      Assertions.assertEquals("e2e_tests_bfh@digitalservice.bund.de", userResult.email());
      Assertions.assertEquals(userId, userResult.externalId());
      Assertions.assertEquals("BFH", userResult.documentationOffice().abbreviation());
      Assertions.assertFalse(userResult.internal());
    }

    @Test
    void testGetUser_whenApiReturnsNoBody_shouldThrowUserApiException() {
      final UUID userId = UUID.randomUUID();

      ResponseEntity<BareUserApiResponse.UserApiResponse> mockResponse =
          ResponseEntity.badRequest().build();

      doReturn(mockResponse)
          .when(restTemplate)
          .exchange(
              anyString(),
              eq(HttpMethod.GET),
              any(HttpEntity.class),
              eq(BareUserApiResponse.UserApiResponse.class));

      assertThatExceptionOfType(UserApiException.class)
          .isThrownBy(() -> bareIdUserApiService.getUser(userId))
          .withMessage("User not found or could not be parsed");
    }

    @Test
    void testGetUser_whenApiReturnsBodyWithoutUser_shouldThrowUserApiException() {
      final UUID userId = UUID.randomUUID();

      ResponseEntity<BareUserApiResponse.UserApiResponse> mockResponse =
          ResponseEntity.ok(new UserApiResponse(null));

      doReturn(mockResponse)
          .when(restTemplate)
          .exchange(
              anyString(),
              eq(HttpMethod.GET),
              any(HttpEntity.class),
              eq(BareUserApiResponse.UserApiResponse.class));

      assertThatExceptionOfType(UserApiException.class)
          .isThrownBy(() -> bareIdUserApiService.getUser(userId))
          .withMessage("User not found or could not be parsed");
    }

    @Test
    void testGetUser_whenApiThrowsRestClientException_shouldThrowUserApiException() {
      final UUID userId = UUID.randomUUID();

      doThrow(RestClientException.class)
          .when(restTemplate)
          .exchange(
              anyString(),
              eq(HttpMethod.GET),
              any(HttpEntity.class),
              eq(BareUserApiResponse.UserApiResponse.class));

      assertThatExceptionOfType(UserApiException.class)
          .isThrownBy(() -> bareIdUserApiService.getUser(userId))
          .withMessage("Error by fetching user");
    }

    @Test
    void testGetUser_whenGroupApiThrowsRestClientException_shouldThrowUserApiException() {
      final UUID userId = UUID.randomUUID();

      BareUser bareUser =
          new BareUser(userId, true, true, "user name", "email", Collections.emptyMap());
      when(restTemplate.exchange(
              anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(UserApiResponse.class)))
          .thenReturn(ResponseEntity.ok(new UserApiResponse(bareUser)));
      doThrow(RestClientException.class)
          .when(restTemplate)
          .exchange(
              anyString(),
              eq(HttpMethod.GET),
              any(HttpEntity.class),
              eq(BareUserApiResponse.GroupResponse.class));

      assertThatExceptionOfType(UserApiException.class)
          .isThrownBy(() -> bareIdUserApiService.getUser(userId))
          .withMessage("Error by getting groups for user");
    }

    @Test
    void testGetUser_whenGroupApiReturnsNoBody_shouldThrowUserApiException() {
      final UUID userId = UUID.randomUUID();

      BareUser bareUser =
          new BareUser(userId, true, true, "user name", "email", Collections.emptyMap());
      when(restTemplate.exchange(
              anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(UserApiResponse.class)))
          .thenReturn(ResponseEntity.ok(new UserApiResponse(bareUser)));
      when(restTemplate.exchange(
              anyString(),
              eq(HttpMethod.GET),
              any(HttpEntity.class),
              eq(BareUserApiResponse.GroupResponse.class)))
          .thenReturn(ResponseEntity.ok(null));

      assertThatExceptionOfType(UserApiException.class)
          .isThrownBy(() -> bareIdUserApiService.getUser(userId))
          .withMessage("User group could not be found");
    }

    @Test
    void testGetUser_whenGroupApiReturnsBodyWithoutGroups_shouldThrowUserApiException()
        throws UserApiException {
      final UUID userId = UUID.randomUUID();

      BareUser bareUser =
          new BareUser(userId, true, true, "user name", "email", Collections.emptyMap());
      when(restTemplate.exchange(
              anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(UserApiResponse.class)))
          .thenReturn(ResponseEntity.ok(new UserApiResponse(bareUser)));
      when(restTemplate.exchange(
              anyString(),
              eq(HttpMethod.GET),
              any(HttpEntity.class),
              eq(BareUserApiResponse.GroupResponse.class)))
          .thenReturn(ResponseEntity.ok(new GroupResponse(null)));

      User user = bareIdUserApiService.getUser(userId);

      assertThat(user.documentationOffice()).isNull();
    }

    @Test
    void testGetUser_whenGroupApiReturnsBodyWithEmptyGroupList_shouldThrowUserApiException()
        throws UserApiException {
      final UUID userId = UUID.randomUUID();

      BareUser bareUser =
          new BareUser(userId, true, true, "user name", "email", Collections.emptyMap());
      when(restTemplate.exchange(
              anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(UserApiResponse.class)))
          .thenReturn(ResponseEntity.ok(new UserApiResponse(bareUser)));
      when(restTemplate.exchange(
              anyString(),
              eq(HttpMethod.GET),
              any(HttpEntity.class),
              eq(BareUserApiResponse.GroupResponse.class)))
          .thenReturn(ResponseEntity.ok(new GroupResponse(Collections.emptyList())));

      User user = bareIdUserApiService.getUser(userId);

      assertThat(user.documentationOffice()).isNull();
    }

    @Test
    void testGetUser_whenAllGroupListDoesNotContainUserGroup_shouldReturnDocOfficeNull()
        throws UserApiException {
      final UUID userId = UUID.randomUUID();
      var attributes =
          Map.of(
              "firstName", new BareUserApiResponse.AttributeValues(List.of("user")),
              "lastName", new BareUserApiResponse.AttributeValues(List.of("name")));

      BareUser bareUser = generateBareUser(userId, attributes);
      when(restTemplate.exchange(
              anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(UserApiResponse.class)))
          .thenReturn(ResponseEntity.ok(new UserApiResponse(bareUser)));
      List<Group> userGroups =
          List.of(new Group(UUID.randomUUID(), "group name", "not the right group path"));
      when(restTemplate.exchange(
              anyString(),
              eq(HttpMethod.GET),
              any(HttpEntity.class),
              eq(BareUserApiResponse.GroupResponse.class)))
          .thenReturn(ResponseEntity.ok(new GroupResponse(userGroups)));
      List<UserGroup> allUserGroups =
          List.of(
              new UserGroup(
                  UUID.randomUUID(), "group path", DocumentationOffice.builder().build(), true));
      when(userGroupService.getAllUserGroups()).thenReturn(allUserGroups);

      var userResult = bareIdUserApiService.getUser(userId);

      assertThat(userResult.name()).isEqualTo("user name");
      Assertions.assertEquals("e2e_tests_bfh@digitalservice.bund.de", userResult.email());
      Assertions.assertEquals(userId, userResult.externalId());
    }

    @Test
    void testGetUser_whenWithMoreThanOneDocumentationOffice_shouldReturnDocOfficeNull()
        throws UserApiException {
      final UUID userId = UUID.randomUUID();

      BareUser bareUser =
          new BareUser(userId, true, true, "user name", "email", Collections.emptyMap());
      when(restTemplate.exchange(
              anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(UserApiResponse.class)))
          .thenReturn(ResponseEntity.ok(new UserApiResponse(bareUser)));
      List<Group> userGroups =
          List.of(
              new Group(UUID.randomUUID(), "group name 1", "group path 1"),
              new Group(UUID.randomUUID(), "group name 2", "group path 2"));
      when(restTemplate.exchange(
              anyString(),
              eq(HttpMethod.GET),
              any(HttpEntity.class),
              eq(BareUserApiResponse.GroupResponse.class)))
          .thenReturn(ResponseEntity.ok(new GroupResponse(userGroups)));
      List<UserGroup> allUserGroups =
          List.of(
              new UserGroup(
                  UUID.randomUUID(),
                  "group path 1",
                  DocumentationOffice.builder().id(UUID.randomUUID()).build(),
                  true),
              new UserGroup(
                  UUID.randomUUID(),
                  "group path 2",
                  DocumentationOffice.builder().id(UUID.randomUUID()).build(),
                  true));
      when(userGroupService.getAllUserGroups()).thenReturn(allUserGroups);

      var userResult = bareIdUserApiService.getUser(userId);

      assertThat(userResult.documentationOffice()).isNull();
    }

    @Test
    void testGetUser_withEmptyNamesAttributes_shouldReturnNullName() throws UserApiException {
      final UUID userId = UUID.randomUUID();

      BareUserApiResponse.BareUser user = generateBareUser(userId, null);
      BareUserApiResponse.UserApiResponse userResponse =
          new BareUserApiResponse.UserApiResponse(user);

      ResponseEntity<BareUserApiResponse.UserApiResponse> mockResponse =
          ResponseEntity.ok(userResponse);

      doReturn(mockResponse)
          .when(restTemplate)
          .exchange(
              anyString(),
              eq(HttpMethod.GET),
              any(HttpEntity.class),
              eq(BareUserApiResponse.UserApiResponse.class));

      var userResult = bareIdUserApiService.getUser(userId);

      Assertions.assertNull(userResult.name());
      Assertions.assertEquals("e2e_tests_bfh@digitalservice.bund.de", userResult.email());
      Assertions.assertEquals(userId, userResult.externalId());
    }
  }

  private BareUserApiResponse.BareUser generateBareUser(
      UUID userId, Map<String, BareUserApiResponse.AttributeValues> attributes) {
    return new BareUserApiResponse.BareUser(
        userId,
        true,
        true,
        "e2e_tests_bfh@digitalservice.bund.de",
        "e2e_tests_bfh@digitalservice.bund.de",
        attributes);
  }

  @Nested
  class GetUsers {

    @Test
    void testGetUsers_forGroupPathWithUser_shouldSucceed() throws UserApiException {

      var results = bareIdUserApiService.getUsers("/caselaw/BGH/Intern");

      assertThat(results).hasSize(1);
      assertThat(results)
          .extracting("name", "email")
          .containsExactly(Tuple.tuple("Tina Taxpayer", "e2e_tests_bfh@digitalservice.bund.de"));
    }

    @Test
    void testGetUsers_withEmptyPath_shouldReturnEmptyList() throws UserApiException {

      List<User> users = bareIdUserApiService.getUsers("");

      assertThat(users).isEmpty();
      assertThat(memoryAppender.count(Level.ERROR)).isEqualTo(1L);
      assertThat(memoryAppender.getMessage(Level.ERROR, 0))
          .isEqualTo("User group path is empty or blank");
    }

    @Test
    void testGetUsers_withGetGroupChildrenThrowsException_shouldThrowUserApiException() {
      doThrow(RestClientException.class)
          .when(restTemplate)
          .exchange(
              anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(GroupApiResponse.class));

      assertThatExceptionOfType(UserApiException.class)
          .isThrownBy(() -> bareIdUserApiService.getUsers("/caselaw/BGH/Intern"))
          .withMessage("Error while fetching users");
    }

    @Test
    void testGetUsers_forGroupPathWithoutUser_shouldReturnEmptyList() throws UserApiException {
      var results = bareIdUserApiService.getUsers("/caselaw/BGH");
      Assertions.assertEquals(0, results.size());
    }

    @Test
    void testGetUsers_forWrongGroupPath_shouldReturnEmptyList() throws UserApiException {
      var results = bareIdUserApiService.getUsers("/caselaw/BGH/Something");
      Assertions.assertEquals(0, results.size());
    }

    @Test
    void testGetUsers_withGetGroupChildrenHasNoBody_shouldThrowUserApiException() {
      when(restTemplate.exchange(
              anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(GroupApiResponse.class)))
          .thenReturn(ResponseEntity.ok(null));

      assertThatExceptionOfType(UserApiException.class)
          .isThrownBy(() -> bareIdUserApiService.getUsers("/caselaw/BGH/Intern"))
          .withMessage("Children for group could not be found");
    }

    @Test
    void testGetUsers_withGetGroupChildrenBodyWithoutChildren_shouldThrowUserApiException() {
      when(restTemplate.exchange(
              anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(GroupApiResponse.class)))
          .thenReturn(
              ResponseEntity.ok(
                  new GroupApiResponse(UUID.randomUUID(), "group name", "group path", null)));

      assertThatExceptionOfType(UserApiException.class)
          .isThrownBy(() -> bareIdUserApiService.getUsers("/caselaw/BGH/Intern"))
          .withMessage("Children for group could not be found");
    }

    @Test
    void testGetUsers_withGetTopLevelGroupThrowsException_shouldThrowUserApiException() {
      doThrow(RestClientException.class)
          .when(restTemplate)
          .exchange(
              anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(GroupResponse.class));

      assertThatExceptionOfType(UserApiException.class)
          .isThrownBy(() -> bareIdUserApiService.getUsers("/caselaw/BGH/Intern"))
          .withMessage("Error while fetching top level groups");
    }

    @Test
    void testGetUsers_withGetTopLevelGroupHasNoBody_shouldThrowUserApiException() {
      when(restTemplate.exchange(
              anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(GroupResponse.class)))
          .thenReturn(ResponseEntity.ok(null));

      assertThatExceptionOfType(UserApiException.class)
          .isThrownBy(() -> bareIdUserApiService.getUsers("/caselaw/BGH/Intern"))
          .withMessage("Top level groups could not be found");
    }

    @Test
    void testGetUsers_withGetTopLevelGroupsBodyWithoutGroups_shouldThrowUserApiException() {
      when(restTemplate.exchange(
              anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(GroupResponse.class)))
          .thenReturn(ResponseEntity.ok(new GroupResponse(null)));

      assertThatExceptionOfType(UserApiException.class)
          .isThrownBy(() -> bareIdUserApiService.getUsers("/caselaw/BGH/Intern"))
          .withMessage("Top level groups could not be found");
    }

    @Test
    void testGetUsers_withUserApiThrowsException_shouldThrowException() {
      doThrow(RestClientException.class)
          .when(restTemplate)
          .exchange(
              anyString(),
              eq(HttpMethod.GET),
              any(HttpEntity.class),
              eq(BareUserApiResponse.UsersApiResponse.class));

      assertThatExceptionOfType(UserApiException.class)
          .isThrownBy(() -> bareIdUserApiService.getUsers("/caselaw/BGH/Intern"))
          .withMessage("Error while fetching users");
    }

    @Test
    void testGetUsers_withUserApiReturnsBodyIsNull_shouldThrowException() {
      when(restTemplate.exchange(
              anyString(),
              eq(HttpMethod.GET),
              any(HttpEntity.class),
              eq(BareUserApiResponse.UsersApiResponse.class)))
          .thenReturn(ResponseEntity.ok(null));

      assertThatExceptionOfType(UserApiException.class)
          .isThrownBy(() -> bareIdUserApiService.getUsers("/caselaw/BGH/Intern"))
          .withMessage("Could not fetch users");
    }

    @Test
    void testGetUsers_withUserApiReturnsBodyWithoutUsers_shouldThrowException() {
      when(restTemplate.exchange(
              anyString(),
              eq(HttpMethod.GET),
              any(HttpEntity.class),
              eq(BareUserApiResponse.UsersApiResponse.class)))
          .thenReturn(ResponseEntity.ok(new UsersApiResponse(null)));

      assertThatExceptionOfType(UserApiException.class)
          .isThrownBy(() -> bareIdUserApiService.getUsers("/caselaw/BGH/Intern"))
          .withMessage("Could not fetch users");
    }
  }

  private BareUserApiResponse.Group generateBareUserGroup(UUID uuid, String name, String path) {
    return new BareUserApiResponse.Group(uuid, name, path);
  }

  private void mockRootGroupsResponse() {
    BareUserApiResponse.GroupResponse groupResponse =
        new BareUserApiResponse.GroupResponse(List.of(caselawGroup));

    ResponseEntity<BareUserApiResponse.GroupResponse> mockGroupResponse =
        ResponseEntity.ok(groupResponse);

    doReturn(mockGroupResponse)
        .when(restTemplate)
        .exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(BareUserApiResponse.GroupResponse.class));
  }

  private void mockCaselawChildrenResponse() {
    BareUserApiResponse.GroupResponse groupsResponse =
        new BareUserApiResponse.GroupResponse(List.of(courtGroup));

    BareUserApiResponse.GroupApiResponse groupApiTopLevelResponse =
        new BareUserApiResponse.GroupApiResponse(UUID.randomUUID(), "BGH", "/BGH", groupsResponse);

    stubGroupAPIResponse(caselawGroup.uuid().toString(), groupApiTopLevelResponse);
  }

  private void mockCourtChildrenResponse() {
    BareUserApiResponse.GroupResponse children =
        new BareUserApiResponse.GroupResponse(List.of(internGroup));

    BareUserApiResponse.GroupApiResponse courtWithIntern =
        new BareUserApiResponse.GroupApiResponse(
            courtGroup.uuid(), courtGroup.name(), courtGroup.path(), children);

    stubGroupAPIResponse(courtGroup.uuid().toString(), courtWithIntern);
  }

  private void mockInternResponse() {
    BareUserApiResponse.GroupResponse children =
        new BareUserApiResponse.GroupResponse(Collections.emptyList());

    BareUserApiResponse.GroupApiResponse courtWithIntern =
        new BareUserApiResponse.GroupApiResponse(
            internGroup.uuid(), internGroup.name(), internGroup.path(), children);

    stubGroupAPIResponse(internGroup.uuid().toString(), courtWithIntern);
  }

  private void mockInternalUserApiResponse() {

    var attributes =
        Map.of(
            "firstName", new BareUserApiResponse.AttributeValues(List.of("Tina")),
            "lastName", new BareUserApiResponse.AttributeValues(List.of("Taxpayer")));

    BareUserApiResponse.BareUser user = generateBareUser(UUID.randomUUID(), attributes);

    BareUserApiResponse.UsersApiResponse userApiResponse =
        new BareUserApiResponse.UsersApiResponse(List.of(user));

    ResponseEntity<BareUserApiResponse.UsersApiResponse> mockUsersResponse =
        ResponseEntity.ok(userApiResponse);

    doReturn(mockUsersResponse)
        .when(restTemplate)
        .exchange(
            endsWith(internGroup.uuid().toString() + "/users"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(BareUserApiResponse.UsersApiResponse.class));
  }

  private void mockEmptyUserApiResponse() {

    BareUserApiResponse.UsersApiResponse userApiResponse =
        new BareUserApiResponse.UsersApiResponse(List.of());

    ResponseEntity<BareUserApiResponse.UsersApiResponse> mockUsersResponse =
        ResponseEntity.ok(userApiResponse);

    doReturn(mockUsersResponse)
        .when(restTemplate)
        .exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(BareUserApiResponse.UsersApiResponse.class));
  }

  private void mockTokenResponse() {

    OAuth2AccessToken mockToken =
        new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "mocked-token-value",
            Instant.now(),
            Instant.now().plusSeconds(3600));
    when(bareIdUserApiTokenService.getAccessToken()).thenReturn(mockToken);
  }

  private <T> void stubGroupAPIResponse(String urlEndsWith, T body) {
    doReturn(ResponseEntity.ok(body))
        .when(restTemplate)
        .exchange(
            endsWith(urlEndsWith),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(GroupApiResponse.class));
  }
}
