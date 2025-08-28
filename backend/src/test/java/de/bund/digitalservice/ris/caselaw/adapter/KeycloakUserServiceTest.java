package de.bund.digitalservice.ris.caselaw.adapter;

import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.domain.UserApiService;
import de.bund.digitalservice.ris.caselaw.domain.UserGroup;
import de.bund.digitalservice.ris.caselaw.domain.UserGroupService;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({KeycloakUserService.class})
class KeycloakUserServiceTest {

  @Autowired private KeycloakUserService keycloakUserService;

  @MockitoBean private UserGroupService userGroupService;
  @MockitoBean private UserApiService userApiService;
  @MockitoBean private OidcUser oidcUser;

  private static final List<String> userGroupPathName =
      List.of("caselaw", "caselaw/BGH", "caselaw/BGH/Intern");

  @Test
  void getUserGroups_whenMultipleGroupsGive_returnsLongestSegment() {
    // Arrange
    when(oidcUser.<List<String>>getAttribute("groups")).thenReturn(userGroupPathName);
    when(userGroupService.getAllUserGroups()).thenReturn(mockUserGroups());

    // Act
    var userGroup = keycloakUserService.getUserGroup(oidcUser);

    // Assert
    Assertions.assertTrue(userGroup.isPresent());
    Assertions.assertEquals("caselaw/BGH/Intern", userGroup.get().userGroupPathName());
  }

  @ParameterizedTest
  @MethodSource("emptyAndNullGroups")
  void getUserGroups_whenNoGroupsNullOrEmpty_returnsEmptyOptional(List<UserGroup> groups) {
    // Arrange
    when(oidcUser.<List<String>>getAttribute("groups")).thenReturn(userGroupPathName);
    when(userGroupService.getAllUserGroups()).thenReturn(groups);

    // Act
    var userGroup = keycloakUserService.getUserGroup(oidcUser);

    // Assert
    Assertions.assertTrue(userGroup.isEmpty());
  }

  private static Stream<List<UserGroup>> emptyAndNullGroups() {
    return Stream.of(null, List.of());
  }

  public static List<UserGroup> mockUserGroups() {
    return userGroupPathName.stream()
        .map(groupName -> UserGroup.builder().userGroupPathName(groupName).build())
        .toList();
  }
}
