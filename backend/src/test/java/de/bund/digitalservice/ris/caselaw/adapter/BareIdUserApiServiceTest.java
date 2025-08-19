package de.bund.digitalservice.ris.caselaw.adapter;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.domain.UserApiException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

@ExtendWith(SpringExtension.class)
@Import({BareIdUserApiService.class})
class BareIdUserApiServiceTest {

  BareIdUserApiService bareIdUserApiService;

  @MockitoBean RestTemplate restTemplate;
  @MockitoBean BareIdUserApiTokenService bareIdUserApiTokenService;

  String instanceId = UUID.randomUUID().toString();

  private final BareUserApiResponse.Group caselawGroup =
      generateBareUserGroup(UUID.fromString("00000000-0000-0000-0000-000000000001"), "caselaw");

  private final BareUserApiResponse.Group courtGroup =
      generateBareUserGroup(UUID.fromString("00000000-0000-0000-0000-000000000002"), "BGH");

  private final BareUserApiResponse.Group internGroup =
      generateBareUserGroup(UUID.fromString("00000000-0000-0000-0000-000000000003"), "Intern");

  @BeforeEach
  void setUp() {
    bareIdUserApiService =
        new BareIdUserApiService(bareIdUserApiTokenService, restTemplate, instanceId);

    mockTokenResponse();
    mockEmptyUserApiResponse(); // return empty list for bgh
    mockInternalUserApiResponse(); // returns user for "intern"

    // caselaw/BGH/Intern
    mockRootGroupsResponse(); // returns top-level groups, including "caselaw"
    mockCaselawChildrenResponse(); // returns caselaw children ("BGH")
    mockCourtChildrenResponse(); // returns BGH children, including "Intern"
    mockInternResponse(); // returns Intern's children (empty)
  }

  @Test
  void testGetUser_shouldSucceed() {
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

    var userResult = bareIdUserApiService.getUser(userId);

    Assertions.assertEquals("Foo Taxpayer", userResult.name());
    Assertions.assertEquals("FT", userResult.initials());
    Assertions.assertEquals("e2e_tests_bfh@digitalservice.bund.de", userResult.email());
    Assertions.assertEquals(userId, userResult.id());
  }

  @Test
  void testGetUser_whenApiReturnsBadRequest_shouldReturnUserWithGivenId() {
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

    var userResult = bareIdUserApiService.getUser(userId);

    Assertions.assertEquals(userId, userResult.id());
    Assertions.assertNull(userResult.name());
  }

  @Test
  void testGetUser_withEmptyNamesAttributes_shouldReturnNullName() {
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
    Assertions.assertEquals(userId, userResult.id());
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

  @Test
  void testGetUsers_shouldSucceed() {
    var results = bareIdUserApiService.getUsers("/caselaw/BGH/Intern");
    Assertions.assertEquals(1, results.size());
    Assertions.assertEquals("Tina Taxpayer", results.getFirst().name());
    Assertions.assertEquals("e2e_tests_bfh@digitalservice.bund.de", results.getFirst().email());
  }

  @Test
  void testGetUsers_withEmptyPath_shouldThrowException() {
    var exception = assertThrows(UserApiException.class, () -> bareIdUserApiService.getUsers(""));
    Assertions.assertEquals("User group path is empty or blank", exception.getMessage());
  }

  @Test
  void testGetUsers_withBadRequest_shouldThrowException() {
    ResponseEntity<BareUserApiResponse.UsersApiResponse> mockUsersResponse =
        ResponseEntity.badRequest().build();

    doReturn(mockUsersResponse)
        .when(restTemplate)
        .exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(BareUserApiResponse.UsersApiResponse.class));

    UUID userGroupId = UUID.randomUUID();

    var exception =
        assertThrows(UserApiException.class, () -> bareIdUserApiService.getUsers(userGroupId));
    Assertions.assertEquals("Could not fetch users", exception.getMessage());
  }

  private BareUserApiResponse.Group generateBareUserGroup(UUID uuid, String name) {
    return new BareUserApiResponse.Group(uuid, name, "/" + name);
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

    stubGroupAPIResponse(
        caselawGroup.uuid().toString(),
        BareUserApiResponse.GroupApiResponse.class,
        groupApiTopLevelResponse);
  }

  private void mockCourtChildrenResponse() {
    BareUserApiResponse.GroupResponse children =
        new BareUserApiResponse.GroupResponse(List.of(internGroup));

    BareUserApiResponse.GroupApiResponse courtWithIntern =
        new BareUserApiResponse.GroupApiResponse(
            courtGroup.uuid(), courtGroup.name(), courtGroup.path(), children);

    stubGroupAPIResponse(
        courtGroup.uuid().toString(), BareUserApiResponse.GroupApiResponse.class, courtWithIntern);
  }

  private void mockInternResponse() {
    BareUserApiResponse.GroupResponse children =
        new BareUserApiResponse.GroupResponse(Collections.emptyList());

    BareUserApiResponse.GroupApiResponse courtWithIntern =
        new BareUserApiResponse.GroupApiResponse(
            internGroup.uuid(), internGroup.name(), internGroup.path(), children);

    stubGroupAPIResponse(
        internGroup.uuid().toString(), BareUserApiResponse.GroupApiResponse.class, courtWithIntern);
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

  private <T> void stubGroupAPIResponse(String urlEndsWith, Class<T> type, T body) {
    doReturn(ResponseEntity.ok(body))
        .when(restTemplate)
        .exchange(endsWith(urlEndsWith), eq(HttpMethod.GET), any(HttpEntity.class), eq(type));
  }
}
