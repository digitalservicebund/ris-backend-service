package de.bund.digitalservice.ris.caselaw.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.gravity9.jsonpatch.AddOperation;
import com.gravity9.jsonpatch.JsonPatch;
import com.gravity9.jsonpatch.JsonPatchOperation;
import com.gravity9.jsonpatch.RemoveOperation;
import com.gravity9.jsonpatch.ReplaceOperation;
import com.gravity9.jsonpatch.TestOperation;
import de.bund.digitalservice.ris.caselaw.adapter.OAuthService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseApiKeyRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({OAuthService.class})
class OAuthServiceTest {

  @MockitoSpyBean private OAuthService service;

  @MockitoBean private DocumentationUnitService documentationUnitService;
  @MockitoBean private UserService userService;
  @MockitoBean private ProcedureService procedureService;
  @MockitoBean private DatabaseApiKeyRepository keyRepository;
  @MockitoBean private DatabaseDocumentationOfficeRepository officeRepository;
  @MockitoBean private SecurityContext securityContext;
  @MockitoBean private Authentication authentication;
  @MockitoBean private OidcUser oidcUser;

  @Test
  void testUserHasReadAccessByDocumentNumber_withStatusNull_shouldReturnTrue()
      throws DocumentationUnitNotExistsException {
    // Arrange
    String documentNumber = "DOC12345";
    Decision decision = Decision.builder().documentNumber(documentNumber).build();
    when(documentationUnitService.getByDocumentNumber(documentNumber)).thenReturn(decision);

    // Act
    Function<String, Boolean> result = service.userHasReadAccessByDocumentNumber();

    // Assert
    assertThat(result.apply("DOC12345")).isTrue();
  }

  @Test
  void testUserHasReadAccessByDocumentNumber_withStatusPublished_shouldReturnTrue()
      throws DocumentationUnitNotExistsException {

    // Arrange
    String documentNumber = "DOC12345";
    Decision decision =
        Decision.builder()
            .documentNumber(documentNumber)
            .status(Status.builder().publicationStatus(PublicationStatus.PUBLISHED).build())
            .build();
    when(documentationUnitService.getByDocumentNumber(documentNumber)).thenReturn(decision);

    // Act
    Function<String, Boolean> result = service.userHasReadAccessByDocumentNumber();

    // Assert
    assertThat(result.apply("DOC12345")).isTrue();
  }

  @Test
  void testUserHasReadAccessByDocumentNumber_withSameDocOffice_shouldReturnTrue()
      throws DocumentationUnitNotExistsException {

    // Arrange
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(oidcUser);
    String documentNumber = "DOC12345";
    DocumentationOffice office = DocumentationOffice.builder().abbreviation("DS").build();
    Decision decision =
        Decision.builder()
            .documentNumber(documentNumber)
            .status(Status.builder().publicationStatus(PublicationStatus.DUPLICATED).build())
            .coreData(CoreData.builder().documentationOffice(office).build())
            .build();
    when(documentationUnitService.getByDocumentNumber(documentNumber)).thenReturn(decision);
    when(userService.getDocumentationOffice(any())).thenReturn(Optional.of(office));

    // Act
    Function<String, Boolean> result = service.userHasReadAccessByDocumentNumber();

    // Assert
    assertThat(result.apply(documentNumber)).isTrue();
  }

  @Test
  void testUserHasReadAccessByDocumentNumber_withStatusPending_withSameDocOffice_shouldReturnTrue()
      throws DocumentationUnitNotExistsException {

    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(oidcUser);
    String documentNumber = "DOC12345";
    DocumentationOffice office = DocumentationOffice.builder().abbreviation("DS").build();
    Decision decision =
        Decision.builder()
            .documentNumber(documentNumber)
            .status(
                Status.builder()
                    .publicationStatus(PublicationStatus.EXTERNAL_HANDOVER_PENDING)
                    .build())
            .coreData(CoreData.builder().documentationOffice(office).build())
            .build();
    when(documentationUnitService.getByDocumentNumber(documentNumber)).thenReturn(decision);
    when(userService.getDocumentationOffice(any())).thenReturn(Optional.of(office));

    Function<String, Boolean> result = service.userHasReadAccessByDocumentNumber();

    assertThat(result.apply(documentNumber)).isTrue();
  }

  @Test
  void testUserHasReadAccessByDocumentNumber_withOtherDocOffice_shouldReturnFalse()
      throws DocumentationUnitNotExistsException {

    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(oidcUser);
    String documentNumber = "DOC12345";
    DocumentationOffice office = DocumentationOffice.builder().abbreviation("BGH").build();
    Decision decision =
        Decision.builder()
            .documentNumber(documentNumber)
            .status(
                Status.builder()
                    .publicationStatus(PublicationStatus.EXTERNAL_HANDOVER_PENDING)
                    .build())
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .build())
            .build();
    when(documentationUnitService.getByDocumentNumber(documentNumber)).thenReturn(decision);
    when(userService.getDocumentationOffice(any())).thenReturn(Optional.of(office));

    Function<String, Boolean> result = service.userHasReadAccessByDocumentNumber();

    assertThat(result.apply(documentNumber)).isFalse();
  }

  @Test
  void
      testUserHasReadAccessByDocumentNumber_withStatusPending_withCreatingDocOffice_shouldReturnTrue()
          throws DocumentationUnitNotExistsException {

    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(oidcUser);
    String documentNumber = "DOC12345";
    DocumentationOffice office = DocumentationOffice.builder().abbreviation("BGH").build();
    Decision decision =
        Decision.builder()
            .documentNumber(documentNumber)
            .status(
                Status.builder()
                    .publicationStatus(PublicationStatus.EXTERNAL_HANDOVER_PENDING)
                    .build())
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .creatingDocOffice(DocumentationOffice.builder().abbreviation("BGH").build())
                    .build())
            .build();
    when(documentationUnitService.getByDocumentNumber(documentNumber)).thenReturn(decision);
    when(userService.getDocumentationOffice(any())).thenReturn(Optional.of(office));

    Function<String, Boolean> result = service.userHasReadAccessByDocumentNumber();

    assertThat(result.apply(documentNumber)).isTrue();
  }

  @Test
  void
      testUserHasReadAccessByDocumentNumber_withStatusUnpublished_withCreatingDocOffice_shouldReturnFalse()
          throws DocumentationUnitNotExistsException {

    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(oidcUser);
    String documentNumber = "DOC12345";
    DocumentationOffice office = DocumentationOffice.builder().abbreviation("BGH").build();
    Decision decision =
        Decision.builder()
            .documentNumber(documentNumber)
            .status(Status.builder().publicationStatus(PublicationStatus.UNPUBLISHED).build())
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .build())
            .build();
    when(documentationUnitService.getByDocumentNumber(documentNumber)).thenReturn(decision);
    when(userService.getDocumentationOffice(any())).thenReturn(Optional.of(office));

    Function<String, Boolean> result = service.userHasReadAccessByDocumentNumber();

    assertThat(result.apply(documentNumber)).isFalse();
  }

  @Test
  void testUserHasReadAccessByDocumentationUnitId_withSameDocOffice_shouldReturnTrue()
      throws DocumentationUnitNotExistsException {
    // Arrange
    UUID testUUID = UUID.randomUUID();
    Decision decision = Decision.builder().uuid(testUUID).build();
    when(documentationUnitService.getByUuid(testUUID)).thenReturn(decision);

    // Act
    Function<UUID, Boolean> result = service.userHasReadAccessByDocumentationUnitId();

    // Assert
    assertThat(result.apply(testUUID)).isTrue();
  }

  @Test
  void testUserHasReadAccessByDocumentationUnitId_withOtherDocOffice_shouldReturnFalse()
      throws DocumentationUnitNotExistsException {
    // Arrange
    UUID testUUID = UUID.randomUUID();
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(oidcUser);

    DocumentationOffice office = DocumentationOffice.builder().abbreviation("BGH").build();
    Decision decision =
        Decision.builder()
            .uuid(testUUID)
            .status(Status.builder().publicationStatus(PublicationStatus.DUPLICATED).build())
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .build())
            .build();

    when(userService.getDocumentationOffice(any())).thenReturn(Optional.of(office));
    when(documentationUnitService.getByUuid(testUUID)).thenReturn(decision);

    // Act
    Function<UUID, Boolean> result = service.userHasReadAccessByDocumentationUnitId();

    // Assert
    assertThat(result.apply(testUUID)).isFalse();
  }

  @Test
  void
      testUserHasReadAccessByDocumentationUnitId_withDocumentationUnitNotExistsException_returnsFalse()
          throws DocumentationUnitNotExistsException {
    UUID testUUID = UUID.randomUUID();

    when(documentationUnitService.getByUuid(testUUID))
        .thenThrow(new DocumentationUnitNotExistsException("Documentation unit not found"));

    Function<UUID, Boolean> result = service.userHasReadAccessByDocumentationUnitId();

    assertThat(result.apply(testUUID)).isFalse();
  }

  @Test
  void testUserHasReadAccessByDocumentationUnitId_withNoDocumentationUnit_returnsFalse()
      throws DocumentationUnitNotExistsException {
    UUID testUUID = UUID.randomUUID();

    when(documentationUnitService.getByUuid(testUUID)).thenReturn(null);

    Function<UUID, Boolean> result = service.userHasReadAccessByDocumentationUnitId();

    assertThat(result.apply(testUUID)).isFalse();
  }

  @Test
  void testUserIsInternal_withInternalUser_shouldReturnTrue() {
    // Arrange
    when(userService.isInternal(any(OidcUser.class))).thenReturn(true);

    // Act
    Function<OidcUser, Boolean> result = service.userIsInternal();

    // Assert
    assertThat(result.apply(oidcUser)).isTrue();
  }

  @Test
  void testUserIsInternal_withExternalUser_shouldReturnFalse() {
    // Arrange
    when(oidcUser.getClaimAsStringList("roles")).thenReturn(List.of("External"));

    // Act
    Function<OidcUser, Boolean> result = service.userIsInternal();

    // Assert
    assertThat(result.apply(oidcUser)).isFalse();
  }

  @Test
  void testUserHasWriteAccessByProcedureId_withSameDocOffice_shouldReturnTrue() {
    // Arrange
    UUID uuid = UUID.randomUUID();
    when(procedureService.getDocumentationOfficeByUUID(uuid))
        .thenReturn(DocumentationOffice.builder().abbreviation("DS").build());
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(oidcUser);
    DocumentationOffice office = DocumentationOffice.builder().abbreviation("DS").build();
    when(userService.getDocumentationOffice(any())).thenReturn(Optional.of(office));

    // Act
    Function<UUID, Boolean> result = service.userHasWriteAccessByProcedureId();

    // Assert
    assertThat(result.apply(uuid)).isTrue();
  }

  @Test
  void testUserHasWriteAccessByProcedureId_withOtherDocOffice_shouldReturnFalse() {
    // Arrange
    UUID uuid = UUID.randomUUID();
    when(procedureService.getDocumentationOfficeByUUID(uuid))
        .thenReturn(DocumentationOffice.builder().abbreviation("BGH").build());
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(oidcUser);
    DocumentationOffice office = DocumentationOffice.builder().abbreviation("DS").build();
    when(userService.getDocumentationOffice(any())).thenReturn(Optional.of(office));

    // Act
    Function<UUID, Boolean> result = service.userHasWriteAccessByProcedureId();

    // Assert
    assertThat(result.apply(uuid)).isFalse();
  }

  @Test
  void testUserHasWriteAccessByDocumentationUnit_withoutDocumentationUnit_shouldReturnFalse()
      throws DocumentationUnitNotExistsException {
    // Arrange
    UUID uuid = UUID.randomUUID();
    when(documentationUnitService.getByUuid(uuid)).thenReturn(null);

    // Act
    Function<UUID, Boolean> result = service.userHasWriteAccess();

    // Assert
    assertThat(result.apply(uuid)).isFalse();
  }

  @Test
  void testUserHasWriteAccessByDocumentationUnit_withPublishedStatus_shouldReturnTrue()
      throws DocumentationUnitNotExistsException {

    // Arrange
    UUID uuid = UUID.randomUUID();
    DocumentationOffice documentationOffice = DocumentationOffice.builder().build();
    SecurityContextHolder.setContext(securityContext);
    when(documentationUnitService.getByUuid(uuid))
        .thenReturn(
            Decision.builder()
                .coreData(CoreData.builder().documentationOffice(documentationOffice).build())
                .status(Status.builder().publicationStatus(PublicationStatus.PUBLISHED).build())
                .build());
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(oidcUser);
    when(userService.getDocumentationOffice(oidcUser)).thenReturn(Optional.of(documentationOffice));

    // Act
    Function<UUID, Boolean> result = service.userHasWriteAccess();

    // Assert
    assertThat(result.apply(uuid)).isTrue();
  }

  @Test
  void testUserHasWriteAccessByDocumentationUnit_withSameDocOffice_shouldReturnTrue()
      throws DocumentationUnitNotExistsException {

    // Arrange
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(oidcUser);
    UUID testUUID = UUID.randomUUID();
    DocumentationOffice office = DocumentationOffice.builder().abbreviation("DS").build();
    Decision decision =
        Decision.builder()
            .uuid(testUUID)
            .coreData(CoreData.builder().documentationOffice(office).build())
            .build();
    when(documentationUnitService.getByUuid(testUUID)).thenReturn(decision);
    when(userService.getDocumentationOffice(any())).thenReturn(Optional.of(office));

    // Act
    Function<UUID, Boolean> result = service.userHasWriteAccess();

    // Assert
    assertThat(result.apply(testUUID)).isTrue();
  }

  @Test
  void
      testUserHasWriteAccessByDocumentationUnit_withStatusPending_withSameDocOffice_shouldReturnTrue()
          throws DocumentationUnitNotExistsException {

    // Arrange
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(oidcUser);
    UUID testUUID = UUID.randomUUID();
    DocumentationOffice office = DocumentationOffice.builder().abbreviation("DS").build();
    Decision decision =
        Decision.builder()
            .uuid(testUUID)
            .status(
                Status.builder()
                    .publicationStatus(PublicationStatus.EXTERNAL_HANDOVER_PENDING)
                    .build())
            .coreData(CoreData.builder().documentationOffice(office).build())
            .build();
    when(documentationUnitService.getByUuid(testUUID)).thenReturn(decision);
    when(userService.getDocumentationOffice(any())).thenReturn(Optional.of(office));

    // Act
    Function<UUID, Boolean> result = service.userHasWriteAccess();

    // Assert
    assertThat(result.apply(testUUID)).isTrue();
  }

  @Test
  void testUserHasWriteAccessByDocumentationUnit_withOtherDocOffice_shouldReturnFalse()
      throws DocumentationUnitNotExistsException {

    // Arrange
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(oidcUser);
    UUID testUUID = UUID.randomUUID();
    DocumentationOffice office = DocumentationOffice.builder().abbreviation("BGH").build();
    Decision decision =
        Decision.builder()
            .uuid(testUUID)
            .status(
                Status.builder()
                    .publicationStatus(PublicationStatus.EXTERNAL_HANDOVER_PENDING)
                    .build())
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .build())
            .build();
    when(documentationUnitService.getByUuid(testUUID)).thenReturn(decision);
    when(userService.getDocumentationOffice(any())).thenReturn(Optional.of(office));

    // Act
    Function<UUID, Boolean> result = service.userHasWriteAccess();

    // Assert
    assertThat(result.apply(testUUID)).isFalse();
  }

  @Test
  void
      testUserHasWriteAccessByDocumentationUnit_withStatusPending_withCreatingDocOffice_shouldReturnTrue()
          throws DocumentationUnitNotExistsException {

    // Arrange
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(oidcUser);
    UUID testUUID = UUID.randomUUID();
    DocumentationOffice office = DocumentationOffice.builder().abbreviation("BGH").build();
    Decision decision =
        Decision.builder()
            .uuid(testUUID)
            .status(
                Status.builder()
                    .publicationStatus(PublicationStatus.EXTERNAL_HANDOVER_PENDING)
                    .build())
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .creatingDocOffice(DocumentationOffice.builder().abbreviation("BGH").build())
                    .build())
            .build();
    when(documentationUnitService.getByUuid(testUUID)).thenReturn(decision);
    when(userService.getDocumentationOffice(any())).thenReturn(Optional.of(office));

    // Act
    Function<UUID, Boolean> result = service.userHasWriteAccess();

    // Assert
    assertThat(result.apply(testUUID)).isTrue();
  }

  @Test
  void
      testUserHasWriteAccessByDocumentationUnit_withStatusUnpublished_withCreatingDocOffice_shouldReturnFalse()
          throws DocumentationUnitNotExistsException {

    // Arrange
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(oidcUser);
    UUID testUUID = UUID.randomUUID();
    DocumentationOffice office = DocumentationOffice.builder().abbreviation("BGH").build();
    Decision decision =
        Decision.builder()
            .uuid(testUUID)
            .status(Status.builder().publicationStatus(PublicationStatus.UNPUBLISHED).build())
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .creatingDocOffice(DocumentationOffice.builder().abbreviation("BGH").build())
                    .build())
            .build();
    when(documentationUnitService.getByUuid(testUUID)).thenReturn(decision);
    when(userService.getDocumentationOffice(any())).thenReturn(Optional.of(office));

    // Act
    Function<UUID, Boolean> result = service.userHasWriteAccess();

    // Assert
    assertThat(result.apply(testUUID)).isFalse();
  }

  // ** Tests for @Override userHasWriteAccess(OidcUser, DocumentationOffice, DocumentationOffice,
  // Status) **

  @Test
  void testUserHasWriteAccessWithParameters_withUnpublishedStatus_withSameDocOffice_returnsTrue() {
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(oidcUser);
    DocumentationOffice office = DocumentationOffice.builder().abbreviation("DS").build();
    when(userService.getDocumentationOffice(any())).thenReturn(Optional.of(office));

    assertThat(
            service.userHasWriteAccess(
                oidcUser,
                office,
                office,
                Status.builder().publicationStatus(PublicationStatus.UNPUBLISHED).build()))
        .isTrue();
  }

  @Test
  void testUserHasWriteAccessWithParameters_withPendingStatus_withSameDocOffice_returnsTrue() {
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(oidcUser);
    DocumentationOffice office = DocumentationOffice.builder().abbreviation("DS").build();
    when(userService.getDocumentationOffice(any())).thenReturn(Optional.of(office));

    assertThat(
            service.userHasWriteAccess(
                oidcUser,
                office,
                office,
                Status.builder()
                    .publicationStatus(PublicationStatus.EXTERNAL_HANDOVER_PENDING)
                    .build()))
        .isTrue();
  }

  @Test
  void
      testUserHasWriteAccessWithParameters_withUnpublishedStatus_withUserEqualsCreatingDocOffice_returnsFalse() {
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(oidcUser);
    DocumentationOffice office = DocumentationOffice.builder().abbreviation("DS").build();
    DocumentationOffice creatingOffice = DocumentationOffice.builder().abbreviation("BGH").build();
    when(userService.getDocumentationOffice(any())).thenReturn(Optional.of(creatingOffice));

    assertThat(
            service.userHasWriteAccess(
                oidcUser,
                creatingOffice,
                office,
                Status.builder().publicationStatus(PublicationStatus.UNPUBLISHED).build()))
        .isFalse();
  }

  @Test
  void
      testUserHasWriteAccessWithParameters_withPendingStatus_withUserEqualsCreatingDocOffice_returnsTrue() {
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(oidcUser);
    DocumentationOffice office = DocumentationOffice.builder().abbreviation("DS").build();
    DocumentationOffice creatingOffice = DocumentationOffice.builder().abbreviation("BGH").build();
    when(userService.getDocumentationOffice(any())).thenReturn(Optional.of(creatingOffice));

    assertThat(
            service.userHasWriteAccess(
                oidcUser,
                creatingOffice,
                office,
                Status.builder()
                    .publicationStatus(PublicationStatus.EXTERNAL_HANDOVER_PENDING)
                    .build()))
        .isTrue();
  }

  @Test
  void testUserHasWriteAccessWithParameters_withPendingStatus_withOtherDocOffice_returnsFalse() {
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(oidcUser);
    DocumentationOffice office = DocumentationOffice.builder().abbreviation("DS").build();
    DocumentationOffice otherOffice = DocumentationOffice.builder().abbreviation("BSG").build();
    when(userService.getDocumentationOffice(any())).thenReturn(Optional.of(otherOffice));

    assertThat(
            service.userHasWriteAccess(
                oidcUser,
                office,
                office,
                Status.builder()
                    .publicationStatus(PublicationStatus.EXTERNAL_HANDOVER_PENDING)
                    .build()))
        .isFalse();
  }

  @Test
  void
      testUserHasWriteAccessWithParameters_withUnpublishedStatus_withOtherDocOffice_returnsFalse() {
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(oidcUser);
    DocumentationOffice office = DocumentationOffice.builder().abbreviation("DS").build();
    DocumentationOffice otherOffice = DocumentationOffice.builder().abbreviation("BSG").build();
    when(userService.getDocumentationOffice(any())).thenReturn(Optional.of(otherOffice));

    assertThat(
            service.userHasWriteAccess(
                oidcUser,
                office,
                office,
                Status.builder().publicationStatus(PublicationStatus.UNPUBLISHED).build()))
        .isFalse();
  }

  @Test
  void testUserHasWriteAccessWithParameters_withNullStatus_returnsFalse() {
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(oidcUser);
    DocumentationOffice office = DocumentationOffice.builder().abbreviation("BGH").build();
    DocumentationOffice creatingOffice = DocumentationOffice.builder().abbreviation("DS").build();
    when(userService.getDocumentationOffice(any())).thenReturn(Optional.of(creatingOffice));

    assertThat(service.userHasWriteAccess(oidcUser, creatingOffice, office, null)).isFalse();
  }

  @Test
  void testUserHasSameDocOfficeAsDocument_withStatusPending_withSameDocOffice_shouldReturnTrue()
      throws DocumentationUnitNotExistsException {

    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(oidcUser);
    String documentNumber = "DOC12345";
    DocumentationOffice office = DocumentationOffice.builder().abbreviation("DS").build();
    Decision decision =
        Decision.builder()
            .documentNumber(documentNumber)
            .status(
                Status.builder()
                    .publicationStatus(PublicationStatus.EXTERNAL_HANDOVER_PENDING)
                    .build())
            .coreData(CoreData.builder().documentationOffice(office).build())
            .build();
    when(documentationUnitService.getByDocumentNumber(documentNumber)).thenReturn(decision);
    when(userService.getDocumentationOffice(any())).thenReturn(Optional.of(office));

    Function<String, Boolean> result = service.userHasSameDocOfficeAsDocument();

    assertThat(result.apply(documentNumber)).isTrue();
  }

  @Test
  void testUserHasSameDocOfficeAsDocument__withStatusPending_withOtherDocOffice_shouldReturnFalse()
      throws DocumentationUnitNotExistsException {

    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(oidcUser);
    String documentNumber = "DOC12345";
    DocumentationOffice office = DocumentationOffice.builder().abbreviation("BGH").build();
    Decision decision =
        Decision.builder()
            .documentNumber(documentNumber)
            .status(
                Status.builder()
                    .publicationStatus(PublicationStatus.EXTERNAL_HANDOVER_PENDING)
                    .build())
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .build())
            .build();
    when(documentationUnitService.getByDocumentNumber(documentNumber)).thenReturn(decision);
    when(userService.getDocumentationOffice(any())).thenReturn(Optional.of(office));

    Function<String, Boolean> result = service.userHasSameDocOfficeAsDocument();

    assertThat(result.apply(documentNumber)).isFalse();
  }

  @Test
  void testIsAssignedViaProcedure_withoutDocumentationUnit_shouldReturnFalse()
      throws DocumentationUnitNotExistsException {

    // Arrange
    UUID uuid = UUID.randomUUID();
    when(documentationUnitService.getByUuid(uuid)).thenReturn(null);

    // Act
    Function<UUID, Boolean> result = service.isAssignedViaProcedure();

    // Assert
    assertThat(result.apply(uuid)).isFalse();
  }

  @Test
  void testIsAssignedViaProcedure_withAssignedProcedure_shouldReturnTrue()
      throws DocumentationUnitNotExistsException {

    // Arrange
    UUID documentationUnitId = UUID.randomUUID();
    UUID userGroupId = UUID.randomUUID();
    SecurityContextHolder.setContext(securityContext);
    when(documentationUnitService.getByUuid(documentationUnitId))
        .thenReturn(
            Decision.builder()
                .coreData(
                    CoreData.builder()
                        .procedure(Procedure.builder().userGroupId(userGroupId).build())
                        .build())
                .build());
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(oidcUser);
    when(userService.getUserGroup(oidcUser))
        .thenReturn(Optional.ofNullable(UserGroup.builder().id(userGroupId).build()));

    // Act
    Function<UUID, Boolean> result = service.isAssignedViaProcedure();

    // Assert
    assertThat(result.apply(documentationUnitId)).isTrue();
  }

  @Test
  void test_isAssignedViaProcedure_withUnAssignedProcedure_shouldReturnFalse()
      throws DocumentationUnitNotExistsException {

    // Arrange
    UUID documentationUnitId = UUID.randomUUID();
    UUID userGroupId = UUID.randomUUID();
    SecurityContextHolder.setContext(securityContext);
    when(documentationUnitService.getByUuid(documentationUnitId))
        .thenReturn(
            Decision.builder()
                .coreData(
                    CoreData.builder()
                        .procedure(Procedure.builder().userGroupId(null).build())
                        .build())
                .build());
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(oidcUser);
    when(userService.getUserGroup(oidcUser))
        .thenReturn(Optional.ofNullable(UserGroup.builder().id(userGroupId).build()));

    // Act
    Function<UUID, Boolean> result = service.isAssignedViaProcedure();

    // Assert
    assertThat(result.apply(documentationUnitId)).isFalse();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/previousDecisions",
        "/ensuingDecisions",
        "/contentRelatedIndexing/keywords",
        "/contentRelatedIndexing/fieldsOfLaw",
        "/contentRelatedIndexing/norms",
        "/contentRelatedIndexing/activeCitations",
        "/contentRelatedIndexing/jobProfiles",
        "/contentRelatedIndexing/dismissalTypes",
        "/contentRelatedIndexing/dismissalGrounds",
        "/contentRelatedIndexing/collectiveAgreements",
        "/contentRelatedIndexing/hasLegislativeMandate",
        "/shortTexts/decisionName",
        "/shortTexts/headline",
        "/shortTexts/guidingPrinciple",
        "/shortTexts/headnote",
        "/shortTexts/otherHeadnote",
        "/note",
        "/version"
      })
  void test_isPatchAllowedForExternalUsers_withAllowedPath_shouldReturnTrue(String path) {
    // Arrange
    List<JsonPatchOperation> operations = List.of(new AddOperation(path, new TextNode("anyValue")));
    RisJsonPatch patch = RisJsonPatch.builder().patch(new JsonPatch(operations)).build();

    // Act
    Function<RisJsonPatch, Boolean> result = service.isPatchAllowedForExternalUsers();

    // Assert
    assertThat(result.apply(patch)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/coreData/court",
        "/coreData/deviatingCourts",
        "/coreData/fileNumbers",
        "/coreData/deviatingFileNumbers",
        "/coreData/decisionDate",
        "/coreData/deviatingDecisionDates",
        "/coreData/appraisalBody",
        "/coreData/documentType",
        "/coreData/ecli",
        "/coreData/deviatingEclis",
        "/coreData/procedure",
        "/coreData/procedure/createdAt",
        "/coreData/procedure/documentationUnitCount",
        "/coreData/procedure/label",
        "/coreData/procedure/id",
        "/coreData/legalEffect",
        "/coreData/leadingDecisionNormReferences",
        "/coreData/yearsOfDispute",
        "/longTexts/tenor",
        "/longTexts/reasons",
        "/longTexts/caseFacts",
        "/longTexts/decisionReasons",
        "/longTexts/dissentingOpinion",
        "/longTexts/otherLongText",
        "/longTexts/outline",
        "/caselawReferences",
        "/literatureReferences"
      })
  void test_isPatchAllowedForExternalUsers_withProhibitedPath_shouldReturnFalse(String path) {
    // Arrange
    List<JsonPatchOperation> operations = List.of(new AddOperation(path, new TextNode("anyValue")));
    RisJsonPatch patch = RisJsonPatch.builder().patch(new JsonPatch(operations)).build();

    // Act
    Function<RisJsonPatch, Boolean> result = service.isPatchAllowedForExternalUsers();

    // Assert
    assertThat(result.apply(patch)).isFalse();
  }

  @Test
  void test_isPatchAllowedForExternalUsers_withProhibitedPathInTestOperation_shouldReturnTrue() {
    // Arrange
    JsonNode anyValue = new TextNode("newValue");
    String firstAllowedPath = "/previousDecisions";
    String secondAllowedPath = "/ensuingDecisions";
    String thirdAllowedPath = "/contentRelatedIndexing/keywords";
    String prohibitedPath = "/coreData/court";
    List<JsonPatchOperation> operations =
        List.of(
            new AddOperation(firstAllowedPath, new TextNode("anyValue")),
            new ReplaceOperation(secondAllowedPath, anyValue),
            new RemoveOperation(thirdAllowedPath),
            new TestOperation(prohibitedPath, anyValue));
    RisJsonPatch patch = RisJsonPatch.builder().patch(new JsonPatch(operations)).build();

    // Act
    Function<RisJsonPatch, Boolean> result = service.isPatchAllowedForExternalUsers();

    // Assert
    assertThat(result.apply(patch)).isTrue();
  }

  @Test
  void test_isPatchAllowedForExternalUsers_withAtLeastOneProhibitedPath_shouldReturnFalse() {
    // Arrange
    JsonNode anyValue = new TextNode("newValue");
    String firstAllowedPath = "/previousDecisions";
    String secondAllowedPath = "/ensuingDecisions";
    String prohibitedPath = "/coreData/court";
    List<JsonPatchOperation> operations =
        List.of(
            new AddOperation(firstAllowedPath, new TextNode("anyValue")),
            new ReplaceOperation(secondAllowedPath, anyValue),
            new RemoveOperation(prohibitedPath));
    RisJsonPatch patch = RisJsonPatch.builder().patch(new JsonPatch(operations)).build();

    // Act
    Function<RisJsonPatch, Boolean> result = service.isPatchAllowedForExternalUsers();

    // Assert
    assertThat(result.apply(patch)).isFalse();
  }
}
