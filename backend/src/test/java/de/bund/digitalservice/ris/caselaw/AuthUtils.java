package de.bund.digitalservice.ris.caselaw;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;

import de.bund.digitalservice.ris.caselaw.adapter.KeycloakUserService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.UserApiException;
import de.bund.digitalservice.ris.caselaw.domain.UserApiService;
import de.bund.digitalservice.ris.caselaw.domain.UserGroup;
import de.bund.digitalservice.ris.caselaw.domain.UserGroupService;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor;
import org.testcontainers.shaded.org.checkerframework.checker.nullness.qual.Nullable;

public class AuthUtils {

  // Updated to accept a nullable userId
  public static OidcLoginRequestPostProcessor getMockLogin(@Nullable UUID userId) {
    return getMockLoginWithDocOffice("/DS", "Internal", userId);
  }

  // Updated to accept a nullable userId
  public static OidcLoginRequestPostProcessor getMockLoginExternal(@Nullable UUID userId) {
    return getMockLoginWithDocOffice("/DS/Extern", "External", userId);
  }

  // Modified method to accept a nullable userId for the 'sub' claim
  public static OidcLoginRequestPostProcessor getMockLoginWithDocOffice(
      String docOfficeGroup, String role, @Nullable UUID userId) { // userId is now nullable
    return oidcLogin()
        .idToken(
            token ->
                token.claims(
                    claims -> {
                      claims.put("groups", Collections.singletonList(docOfficeGroup));
                      claims.put("roles", Collections.singletonList(role));
                      claims.put("name", "testUser"); // This is the name from OIDC token
                      claims.put("email", "test@test.com");
                      claims.put("given_name", "testUser");
                      // If userId is null, generate a random one
                      claims.put("sub", (userId != null ? userId : UUID.randomUUID()).toString());
                    }));
  }

  public static void setUpDocumentationOfficeMocks(
      KeycloakUserService userService,
      DocumentationOffice docOffice1,
      String docOffice1Group,
      DocumentationOffice docOffice2,
      String docOffice2Group) {
    doReturn(true).when(userService).isInternal(any());
    doReturn(docOffice1)
        .when(userService)
        .getDocumentationOffice(
            argThat(
                (OidcUser user) -> {
                  List<String> groups = user.getAttribute("groups");
                  return Objects.requireNonNull(groups).get(0).equals(docOffice1Group);
                }));
    doReturn(docOffice2)
        .when(userService)
        .getDocumentationOffice(
            argThat(
                (OidcUser user) -> {
                  List<String> groups = user.getAttribute("groups");
                  return Objects.requireNonNull(groups).get(0).equals(docOffice2Group);
                }));
  }

  public static DocumentationOffice buildDSDocOffice() {
    return DocumentationOffice.builder()
        .abbreviation("DS")
        .id(UUID.fromString("6be0bb1a-c196-484a-addf-822f2ab557f7"))
        .build();
  }

  public static User buildDSuser() {
    return User.builder().documentationOffice(buildDSDocOffice()).build();
  }

  public static DocumentationOffice buildCCRisDocOffice() {
    return DocumentationOffice.builder()
        .abbreviation("CC-RIS")
        .id(UUID.fromString("a2a0dc66-132b-4ae4-9acf-7f5336f7d156"))
        .build();
  }

  public static DocumentationOffice buildBGHDocOffice() {
    return DocumentationOffice.builder()
        .abbreviation("BGH")
        .id(UUID.fromString("bd350c93-7ff0-4409-9c62-371e3d0c749e"))
        .build();
  }

  public static DocumentationOffice buildBFHDocOffice() {
    return DocumentationOffice.builder()
        .abbreviation("BFH")
        .id(UUID.fromString("aba5a347-8761-4494-b6a3-4edc3028e571"))
        .build();
  }

  public static DocumentationOffice buildVVBundDocOffice() {
    return DocumentationOffice.builder()
        .abbreviation("VVBund")
        .id(UUID.fromString("ddc6339b-ffb2-49c8-b54b-81eb62a6b01c"))
        .build();
  }

  public static DocumentationOffice buildBZStDocOffice() {
    return DocumentationOffice.builder()
        .abbreviation("BZSt")
        .id(UUID.fromString("f13636bb-f611-480e-b50f-940f88b0de27"))
        .build();
  }

  public static void mockUserGroups(UserGroupService userGroupService) {
    doReturn(
            List.of(
                UserGroup.builder()
                    .docOffice(buildDSDocOffice())
                    .userGroupPathName("/DS")
                    .isInternal(true)
                    .build(),
                UserGroup.builder()
                    .id(UUID.fromString("2b733549-d2cc-40f0-b7f3-9bfa9f3c1b89"))
                    .docOffice(buildDSDocOffice())
                    .userGroupPathName("/DS/Extern")
                    .isInternal(false)
                    .build(),
                UserGroup.builder()
                    .docOffice(buildBGHDocOffice())
                    .userGroupPathName("/BGH")
                    .isInternal(true)
                    .build(),
                UserGroup.builder()
                    .id(UUID.fromString("3b733549-d2cc-40f0-b7f3-9bfa9f3c1b89"))
                    .docOffice(buildBGHDocOffice())
                    .userGroupPathName("/BGH/Extern")
                    .isInternal(true)
                    .build(),
                UserGroup.builder()
                    .id(UUID.fromString("032d5a5e-bf3f-470a-b2c7-63066cb88ebb"))
                    .docOffice(buildBFHDocOffice())
                    .userGroupPathName("/BFH")
                    .isInternal(true)
                    .build(),
                UserGroup.builder()
                    .docOffice(buildCCRisDocOffice())
                    .userGroupPathName("/CC-RIS")
                    .isInternal(true)
                    .build(),
                UserGroup.builder()
                    .docOffice(buildBZStDocOffice())
                    .userGroupPathName("/BZSt")
                    .isInternal(true)
                    .build()))
        .when(userGroupService)
        .getAllUserGroups();

    doAnswer(
            invocation -> {
              List<String> userGroups = invocation.getArgument(0);
              return userGroupService.getAllUserGroups().stream()
                  .filter(group -> userGroups.contains(group.userGroupPathName()))
                  .findFirst();
            })
        .when(userGroupService)
        .getUserGroupFromGroupPathNames(anyList());
  }

  /**
   * For testing getUser should return a user with id as the api service is unreachable
   *
   * @param userApiService the mocked service to adjust return to
   */
  public static void mockUserApi(UserApiService userApiService) {

    try {
      doAnswer(
              invocation -> {
                UUID id = invocation.getArgument(0);
                if (id == null) {
                  return null;
                }
                return User.builder().id(id).build();
              })
          .when(userApiService)
          .getUser(any(UUID.class));
    } catch (UserApiException ex) {
      // only mock so no error handling necessary
    }
  }
}
