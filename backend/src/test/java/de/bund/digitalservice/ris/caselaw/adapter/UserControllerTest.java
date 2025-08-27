package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.UserGroup;
import de.bund.digitalservice.ris.caselaw.domain.UserGroupService;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = UserController.class)
@Import({SecurityConfig.class, TestConfig.class, KeycloakUserService.class})
class UserControllerTest {

  @Autowired private RisWebTestClient risWebClient;

  @MockitoBean private OidcUser oidcUser;
  @MockitoBean private ClientRegistrationRepository clientRegistrationRepository;
  @MockitoBean private OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager;
  @MockitoBean private UserGroupService userGroupService;
  @MockitoBean private BareIdUserApiService userApiService;

  @MockitoSpyBean private KeycloakUserService userService;

  private final List<User> testUsers = generateTestUsers();

  @Test
  void testGetUsers_shouldSucceed() {

    doReturn(Optional.of(UserGroup.builder().userGroupPathName("test").build()))
        .when(userService)
        .getUserGroup(any(OidcUser.class));

    when(userApiService.getUsers(anyString())).thenReturn(testUsers);

    var result =
        risWebClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/users")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<List<User>>() {})
            .returnResult();

    List<User> users = result.getResponseBody();
    assertThat(users).containsExactlyInAnyOrderElementsOf(testUsers);
  }

  @Test
  void testGetUsers_shouldReturnEmptyList_onFailed() {

    doReturn(Optional.empty()).when(userService).getUserGroup(any(OidcUser.class));

    var result =
        risWebClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/users")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<List<User>>() {})
            .returnResult();

    Assertions.assertEquals(0, result.getResponseBody().size());
    verify(userApiService, never()).getUsers(anyString());
  }

  @Test
  void testGetUsersWithFilter_withEmptyString_shouldReturnAllUsers() {

    doReturn(Optional.of(UserGroup.builder().userGroupPathName("test").build()))
        .when(userService)
        .getUserGroup(any(OidcUser.class));

    when(userApiService.getUsers(anyString())).thenReturn(testUsers);

    var result =
        risWebClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/users?q=")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<List<User>>() {})
            .returnResult();

    Assertions.assertEquals(3, result.getResponseBody().size());
  }

  @ParameterizedTest
  @ValueSource(strings = {"ch", "CH", "clara hoff", "Clara H", "Hoff"})
  void testGetUsersWithFilter_shouldReturnEmptyList_onFailed(String queryFilter) {

    doReturn(Optional.of(UserGroup.builder().userGroupPathName("test").build()))
        .when(userService)
        .getUserGroup(any(OidcUser.class));

    when(userApiService.getUsers(anyString())).thenReturn(testUsers);

    var result =
        risWebClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/users?q=" + queryFilter)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<List<User>>() {})
            .returnResult();

    Assertions.assertEquals(1, result.getResponseBody().size());
    Assertions.assertEquals(testUsers.getFirst(), result.getResponseBody().getFirst());
    Assertions.assertEquals("Clara Hoffmann", result.getResponseBody().getFirst().name());
  }

  private List<User> generateTestUsers() {

    var firstUser =
        User.builder()
            .id(UUID.randomUUID())
            .name("Clara Hoffmann")
            .email("clara.hoffmann@digitalservice.bund.de")
            .initials("CH")
            .build();

    var secondUser =
        User.builder()
            .id(UUID.randomUUID())
            .name("Jonas Bergmann")
            .email("jonas.bergmann@digitalservice.bund.de")
            .initials("JB")
            .build();

    var thirdServiceUnreachableUser = User.builder().id(UUID.randomUUID()).build();

    return List.of(firstUser, secondUser, thirdServiceUnreachableUser);
  }
}
