package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.UserTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.UserGroup;
import de.bund.digitalservice.ris.caselaw.domain.UserGroupService;
import de.bund.digitalservice.ris.caselaw.domain.UserRepository;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({DatabaseUserService.class})
class DatabaseUserServiceTest {

  @MockitoBean private UserRepository userRepository;

  @MockitoBean private UserGroupService userGroupService;

  @MockitoBean(name = "keycloakUserService")
  private UserService keycloakUserService;

  @Autowired private DatabaseUserService databaseUserService;

  private static final UUID USER_UUID = UUID.randomUUID();
  private static final UUID EXTERNAL_ID = UUID.randomUUID();
  private static final UUID GROUP_UUID = UUID.randomUUID();

  private final User user =
      User.builder()
          .id(USER_UUID)
          .externalId(EXTERNAL_ID)
          .documentationOffice(DocumentationOffice.builder().abbreviation("BVerwG").build())
          .build();

  private final UserGroup userGroup =
      UserGroup.builder()
          .id(GROUP_UUID)
          .docOffice(DocumentationOffice.builder().abbreviation("BVerwG").build())
          .build();

  @BeforeEach
  void setup() throws DocumentationUnitNotExistsException {
    databaseUserService =
        new DatabaseUserService(userGroupService, userRepository, keycloakUserService);
    databaseUserService.onApplicationEvent(null);
  }

  @Test
  void testFetchAndPersistUsersFromKeycloak_shouldFetchAndSaveUsers() {
    List<UserGroup> userGroups = List.of(userGroup);
    List<User> usersFromKeycloak = List.of(User.builder().build());

    when(userGroupService.getAllUserGroups()).thenReturn(userGroups);
    when(keycloakUserService.getUsersInSameDocOffice(userGroup)).thenReturn(usersFromKeycloak);

    databaseUserService.fetchAndPersistUsersFromKeycloak();

    ArgumentCaptor<List<User>> userCaptor = ArgumentCaptor.forClass(List.class);
    verify(userRepository, times(1)).saveOrUpdate(userCaptor.capture());

    List<User> capturedUsers = userCaptor.getValue();
    assertThat(capturedUsers).hasSize(1);
    assertThat(capturedUsers.get(0).documentationOffice()).isEqualTo(userGroup.docOffice());
  }

  @Test
  void testGetUserByOidcUser_withUserInDatabase_shouldReturnUserFromDatabase() {
    OidcUser oidcUser =
        new DefaultOidcUser(
            Collections.emptyList(),
            OidcIdToken.withTokenValue("token").claim("sub", EXTERNAL_ID.toString()).build());

    when(userRepository.findByExternalId(UserTransformer.getOidcUserId(oidcUser)))
        .thenReturn(Optional.of(user));

    User retrievedUser = databaseUserService.getUser(oidcUser);

    assertEquals(user, retrievedUser);
    verify(userRepository, times(1)).findByExternalId(EXTERNAL_ID);
    verify(keycloakUserService, never()).getUser(any(OidcUser.class));
    verify(userRepository, never()).saveOrUpdate(any(User.class));
  }

  @Test
  void
      testGetUserByOidcUser_withUserNotInDatabaseButInKeycloak_shouldReturnAndPersistUserFromKeycloak() {
    OidcUser oidcUser =
        new DefaultOidcUser(
            Collections.emptyList(),
            OidcIdToken.withTokenValue("token").claim("sub", EXTERNAL_ID.toString()).build());

    when(userRepository.findByExternalId(UserTransformer.getOidcUserId(oidcUser)))
        .thenReturn(Optional.empty());
    when(keycloakUserService.getUser(oidcUser)).thenReturn(user);
    when(userRepository.saveOrUpdate(user)).thenReturn(Optional.of(user));

    User retrievedUser = databaseUserService.getUser(oidcUser);

    assertEquals(user, retrievedUser);
    verify(userRepository, times(1)).findByExternalId(EXTERNAL_ID);
    verify(keycloakUserService, times(1)).getUser(oidcUser);
    verify(userRepository, times(1)).saveOrUpdate(user);
  }

  @Test
  void testGetUserByOidcUser_withUserNotInDatabaseAndKeycloak_shouldReturnNull() {
    OidcUser oidcUser =
        new DefaultOidcUser(
            Collections.emptyList(),
            OidcIdToken.withTokenValue("token").claim("sub", EXTERNAL_ID.toString()).build());

    when(userRepository.findByExternalId(UserTransformer.getOidcUserId(oidcUser)))
        .thenReturn(Optional.empty());
    when(keycloakUserService.getUser(oidcUser)).thenReturn(null);
    when(userRepository.saveOrUpdate(any(User.class))).thenReturn(Optional.empty());

    User retrievedUser = databaseUserService.getUser(oidcUser);

    assertNull(retrievedUser);
    verify(userRepository, times(1)).findByExternalId(EXTERNAL_ID);
    verify(keycloakUserService, times(1)).getUser(oidcUser);
  }

  @Test
  void testGetUserByUUID_withUserInDatabaseById_shouldReturnUserFromDatabase() {
    when(userRepository.getUser(USER_UUID)).thenReturn(Optional.of(user));

    User retrievedUser = databaseUserService.getUser(USER_UUID);

    assertEquals(user, retrievedUser);
    verify(userRepository, times(1)).getUser(USER_UUID);
    verify(userRepository, never()).findByExternalId(any(UUID.class));
    verify(keycloakUserService, never()).getUser(any(UUID.class));
    verify(userRepository, never()).saveOrUpdate(any(User.class));
  }

  @Test
  void testGetUserByUUID_withUserNotInDatabaseByIdButByExternalId_shouldReturnUserFromDatabase() {
    when(userRepository.getUser(USER_UUID)).thenReturn(Optional.empty());
    when(userRepository.findByExternalId(USER_UUID)).thenReturn(Optional.of(user));

    User retrievedUser = databaseUserService.getUser(USER_UUID);

    assertEquals(user, retrievedUser);
    verify(userRepository, times(1)).getUser(USER_UUID);
    verify(userRepository, times(1)).findByExternalId(USER_UUID);
    verify(keycloakUserService, never()).getUser(any(UUID.class));
    verify(userRepository, never()).saveOrUpdate(any(User.class));
  }

  @Test
  void testGetUserByUUID_withUserNotInDatabaseAndKeycloak_shouldReturnNull() {
    when(userRepository.getUser(USER_UUID)).thenReturn(Optional.empty());
    when(userRepository.findByExternalId(USER_UUID)).thenReturn(Optional.empty());

    User retrievedUser = databaseUserService.getUser(USER_UUID);

    assertNull(retrievedUser);
    verify(userRepository, times(1)).getUser(USER_UUID);
    verify(userRepository, times(1)).findByExternalId(USER_UUID);
    verify(userRepository, never()).saveOrUpdate(any(User.class));
  }

  @Test
  void testGetUserByUUID_withNullUUID_shouldReturnNull() {
    User retrievedUser = databaseUserService.getUser((UUID) null);

    assertNull(retrievedUser);
    verify(userRepository, never()).getUser(any(UUID.class));
  }

  @Test
  void testGetUsersInSameDocOffice_withValidUserGroup_shouldReturnUsers() {
    List<User> users = List.of(user);
    when(userRepository.getAllUsersForDocumentationOffice(userGroup.docOffice())).thenReturn(users);

    List<User> retrievedUsers = databaseUserService.getUsersInSameDocOffice(userGroup);

    assertEquals(users, retrievedUsers);
    verify(userRepository, times(1)).getAllUsersForDocumentationOffice(userGroup.docOffice());
  }

  @Test
  void testGetUsersInSameDocOffice_withNullUserGroup_shouldReturnEmptyList() {
    List<User> retrievedUsers = databaseUserService.getUsersInSameDocOffice((UserGroup) null);

    assertThat(retrievedUsers).isEmpty();
    verify(userRepository, never()).getAllUsersForDocumentationOffice(any());
  }

  @Test
  void testGetUsersInSameDocOffice_withNullDocumentationOffice_shouldReturnEmptyList() {
    UserGroup userGroupWithNullDocOffice = UserGroup.builder().id(UUID.randomUUID()).build();
    List<User> retrievedUsers =
        databaseUserService.getUsersInSameDocOffice(userGroupWithNullDocOffice);

    assertThat(retrievedUsers).isEmpty();
    verify(userRepository, never()).getAllUsersForDocumentationOffice(any());
  }
}
