package de.bund.digitalservice.ris.caselaw.domain;

import static de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationType.ACTIVE_CITATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.gravity9.jsonpatch.AddOperation;
import com.gravity9.jsonpatch.JsonPatch;
import com.gravity9.jsonpatch.JsonPatchOperation;
import com.gravity9.jsonpatch.ReplaceOperation;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentationUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.FmxService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentNumberFormatterException;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentNumberPatternException;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationOfficeNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitDeletionException;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitException;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitExistsException;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalPeriodical;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.domain.mapper.PatchMapperService;
import jakarta.validation.Validator;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({DocumentationUnitService.class, DatabaseDocumentationUnitStatusService.class})
class DocumentationUnitServiceTest {
  private static final UUID TEST_UUID = UUID.fromString("88888888-4444-4444-4444-121212121212");
  @MockitoSpyBean private DocumentationUnitService service;
  @MockitoBean private DatabaseDocumentationUnitRepository documentationUnitRepository;
  @MockitoBean private DocumentationUnitRepository repository;
  @MockitoBean private DocumentNumberService documentNumberService;
  @MockitoBean private DocumentNumberRecyclingService documentNumberRecyclingService;
  @MockitoBean private MailService mailService;
  @MockitoBean private DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @MockitoBean private AttachmentService attachmentService;
  @MockitoBean private PatchMapperService patchMapperService;
  @MockitoBean private Validator validator;
  @MockitoBean private OidcUser oidcUser;
  @MockitoBean private AuthService authService;
  @MockitoBean private DuplicateCheckService duplicateCheckService;
  @MockitoBean UserService userService;
  @MockitoBean private DocumentationUnitHistoryLogService historyLogService;
  @MockitoBean private FmxService fmxService;
  @MockitoBean private DocumentationOfficeService documentationOfficeService;
  @Captor private ArgumentCaptor<DocumentationUnitSearchInput> searchInputCaptor;
  @Captor private ArgumentCaptor<RelatedDocumentationUnit> relatedDocumentationUnitCaptor;

  @Test
  void testGenerateNewDocumentationUnit()
      throws DocumentationUnitExistsException,
          DocumentNumberPatternException,
          DocumentNumberFormatterException {
    DocumentationOffice documentationOffice =
        DocumentationOffice.builder().id(UUID.randomUUID()).build();
    User user = User.builder().documentationOffice(documentationOffice).build();
    DocumentationUnit documentationUnit = DocumentationUnit.builder().build();

    when(repository.createNewDocumentationUnit(any(), any(), any(), any()))
        .thenReturn(documentationUnit);
    when(documentNumberService.generateDocumentNumber(documentationOffice.abbreviation()))
        .thenReturn("nextDocumentNumber");
    // Can we use a captor to check if the document number was correctly created?
    // The chicken-egg-problem is, that we are dictating what happens when
    // repository.save(), so we can't just use a captor at the same time

    assertNotNull(service.generateNewDocumentationUnit(user, Optional.empty()));

    verify(documentNumberService).generateDocumentNumber(documentationOffice.abbreviation());
    verify(duplicateCheckService, times(1)).checkDuplicates("nextDocumentNumber");
    verify(repository)
        .createNewDocumentationUnit(
            DocumentationUnit.builder()
                .version(0L)
                .documentNumber("nextDocumentNumber")
                .coreData(
                    CoreData.builder()
                        .legalEffect(LegalEffect.NOT_SPECIFIED.getLabel())
                        .documentationOffice(documentationOffice)
                        .build())
                .build(),
            Status.builder()
                .publicationStatus(PublicationStatus.UNPUBLISHED)
                .withError(false)
                .build(),
            null,
            user);
  }

  @Test
  void testGenerateNewDocumentationUnitWithParameters()
      throws DocumentationUnitExistsException,
          DocumentNumberPatternException,
          DocumentNumberFormatterException {
    DocumentationOffice userDocumentationOffice =
        DocumentationOffice.builder().abbreviation("BAG").id(UUID.randomUUID()).build();
    User user = User.builder().documentationOffice(userDocumentationOffice).build();
    DocumentationOffice designatedDocumentationOffice =
        DocumentationOffice.builder().abbreviation("BGH").id(UUID.randomUUID()).build();
    DocumentationUnit documentationUnit = DocumentationUnit.builder().build();
    DocumentationUnitCreationParameters parameters =
        DocumentationUnitCreationParameters.builder()
            .documentationOffice(designatedDocumentationOffice)
            .fileNumber("fileNumber")
            .court(Court.builder().type("BGH").build())
            .decisionDate(LocalDate.now())
            .documentType(DocumentType.builder().label("Bes").build())
            .reference(
                Reference.builder()
                    .citation("2024, 4")
                    .legalPeriodical(LegalPeriodical.builder().abbreviation("BAG").build())
                    .build())
            .build();

    when(repository.createNewDocumentationUnit(any(), any(), any(), any()))
        .thenReturn(documentationUnit);

    when(documentNumberService.generateDocumentNumber(designatedDocumentationOffice.abbreviation()))
        .thenReturn("nextDocumentNumber");
    // Can we use a captor to check if the document number was correctly created?
    // The chicken-egg-problem is, that we are dictating what happens when
    // repository.save(), so we can't just use a captor at the same time

    assertNotNull(service.generateNewDocumentationUnit(user, Optional.of(parameters)));

    verify(documentNumberService)
        .generateDocumentNumber(designatedDocumentationOffice.abbreviation());
    verify(repository)
        .createNewDocumentationUnit(
            DocumentationUnit.builder()
                .version(0L)
                .documentNumber("nextDocumentNumber")
                .inboxStatus(InboxStatus.EXTERNAL_HANDOVER)
                .coreData(
                    CoreData.builder()
                        .creatingDocOffice(userDocumentationOffice)
                        .documentationOffice(designatedDocumentationOffice)
                        .fileNumbers(List.of(parameters.fileNumber()))
                        .court(parameters.court())
                        .legalEffect(LegalEffect.YES.getLabel())
                        .decisionDate(parameters.decisionDate())
                        .documentType(parameters.documentType())
                        .build())
                .build(),
            Status.builder()
                .publicationStatus(PublicationStatus.EXTERNAL_HANDOVER_PENDING)
                .withError(false)
                .build(),
            parameters.reference(),
            user);
  }

  @Test
  void testGetByDocumentnumber() throws DocumentationUnitNotExistsException {
    when(repository.findByDocumentNumber("ABCDE20220001"))
        .thenReturn(DocumentationUnit.builder().build());
    var documentationUnit = service.getByDocumentNumber("ABCDE20220001");
    assertEquals(DocumentationUnit.class, documentationUnit.getClass());

    verify(repository).findByDocumentNumber("ABCDE20220001");
  }

  @Test
  void testDeleteByUuid_withoutFileAttached() throws DocumentationUnitNotExistsException {
    // I think I shouldn't have to insert a specific DocumentationUnit object here?
    // But if I don't, the test by itself succeeds, but fails if all tests in this class run
    // something flaky with the repository mock? Investigate this later
    DocumentationUnit documentationUnit = DocumentationUnit.builder().uuid(TEST_UUID).build();
    // can we also test that the fileUuid from the DocumentationUnit is used? with a captor somehow?
    when(repository.findByUuid(TEST_UUID, null)).thenReturn(documentationUnit);

    var string = service.deleteByUuid(TEST_UUID);
    assertNotNull(string);
    assertEquals("Dokumentationseinheit gelöscht: " + TEST_UUID, string);

    verify(attachmentService, times(0)).deleteAllObjectsFromBucketForDocumentationUnit(TEST_UUID);
  }

  @Test
  void testDeleteByUuid_withFileAttached() throws DocumentationUnitNotExistsException {
    DocumentationUnit documentationUnit =
        DocumentationUnit.builder()
            .uuid(TEST_UUID)
            .attachments(
                Collections.singletonList(
                    Attachment.builder().s3path(TEST_UUID.toString()).build()))
            .build();

    when(repository.findByUuid(TEST_UUID, null)).thenReturn(documentationUnit);

    var string = service.deleteByUuid(TEST_UUID);
    assertNotNull(string);
    assertEquals("Dokumentationseinheit gelöscht: " + TEST_UUID, string);

    verify(attachmentService).deleteAllObjectsFromBucketForDocumentationUnit(TEST_UUID);
  }

  @Test
  void testDeleteByUuid_withoutFileAttached_withExceptionFromRepository()
      throws DocumentationUnitNotExistsException {

    when(repository.findByUuid(TEST_UUID, null)).thenReturn(DocumentationUnit.builder().build());
    doThrow(new IllegalArgumentException())
        .when(repository)
        .delete(DocumentationUnit.builder().build());

    Assertions.assertThrows(
        DocumentationUnitDeletionException.class, () -> service.deleteByUuid(TEST_UUID));

    verify(repository, times(1)).findByUuid(TEST_UUID, null);
  }

  @Test
  void testDeleteByUuid_withLinks() throws DocumentationUnitNotExistsException {
    when(repository.findByUuid(TEST_UUID, null))
        .thenReturn(DocumentationUnit.builder().documentNumber("foo").build());
    when(repository.getAllRelatedDocumentationUnitsByDocumentNumber(any(String.class)))
        .thenReturn(Map.of(ACTIVE_CITATION, 2L));
    DocumentationUnitDeletionException throwable =
        Assertions.assertThrows(
            DocumentationUnitDeletionException.class, () -> service.deleteByUuid(TEST_UUID));
    Assertions.assertTrue(
        throwable
            .getMessage()
            .contains(
                "Die Dokumentationseinheit konnte nicht gelöscht werden, da (2: Aktivzitierung,)"));
  }

  @Test
  void testUpdateDocumentationUnit() throws DocumentationUnitNotExistsException {
    DocumentationUnit documentationUnit =
        DocumentationUnit.builder()
            .uuid(UUID.randomUUID())
            .documentNumber("ABCDE20220001")
            .attachments(
                Collections.singletonList(
                    Attachment.builder().uploadTimestamp(Instant.now()).build()))
            .build();
    when(repository.findByUuid(documentationUnit.uuid(), null)).thenReturn(documentationUnit);

    var du = service.updateDocumentationUnit(documentationUnit);
    assertEquals(du, documentationUnit);

    verify(repository).save(documentationUnit, null);
  }

  @Test
  void testPatchUpdateWithOnlyVersion_shouldNotIncrementVersion()
      throws DocumentationUnitNotExistsException {
    DocumentationUnit documentationUnit =
        DocumentationUnit.builder()
            .uuid(UUID.randomUUID())
            .documentNumber("ABCDE20220001")
            .attachments(
                Collections.singletonList(
                    Attachment.builder().uploadTimestamp(Instant.now()).build()))
            .version(0L)
            .build();
    User user = User.builder().build();
    when(repository.findByUuid(documentationUnit.uuid(), user)).thenReturn(documentationUnit);
    when(patchMapperService.calculatePatch(any(), any())).thenReturn(new JsonPatch(List.of()));
    when(patchMapperService.removePatchForSamePath(any(), any()))
        .thenReturn(new JsonPatch(List.of()));
    when(patchMapperService.handlePatchForSamePath(any(), any(), any(), any()))
        .thenReturn(
            RisJsonPatch.builder()
                .patch(new JsonPatch(List.of()))
                .documentationUnitVersion(1L)
                .errorPaths(Collections.emptyList())
                .build());

    JsonNode valueToReplace = new TextNode("0");
    JsonPatchOperation replaceOp = new ReplaceOperation("/version", valueToReplace);
    JsonPatch patch = new JsonPatch(List.of(replaceOp));
    var risJsonPatch = RisJsonPatch.builder().documentationUnitVersion(1L).patch(patch).build();

    var response = service.updateDocumentationUnit(documentationUnit.uuid(), risJsonPatch, user);
    assertEquals(0L, response.documentationUnitVersion());
  }

  @ParameterizedTest(name = "test patch with path: {0}, should trigger duplicate check")
  @MethodSource("provideDuplicateCheckPaths")
  void testPatchUpdateWithCoreData_shouldTriggerDuplicateCheck(String path)
      throws DocumentationUnitNotExistsException {
    // Arrange
    DocumentationUnit documentationUnit =
        DocumentationUnit.builder()
            .uuid(UUID.randomUUID())
            .documentNumber("ABCDE20220001")
            .attachments(
                Collections.singletonList(
                    Attachment.builder().uploadTimestamp(Instant.now()).build()))
            .version(0L)
            .build();

    JsonNode valueToAdd = new TextNode("old value");
    JsonPatchOperation addOperation = new AddOperation(path, valueToAdd);
    JsonPatch patch = new JsonPatch(List.of(addOperation));
    User user = User.builder().build();

    when(repository.findByUuid(documentationUnit.uuid(), user)).thenReturn(documentationUnit);
    when(patchMapperService.calculatePatch(any(), any())).thenReturn(new JsonPatch(List.of()));
    when(patchMapperService.removePatchForSamePath(any(), any())).thenReturn(patch);
    when(patchMapperService.applyPatchToEntity(any(), any())).thenReturn(documentationUnit);
    when(patchMapperService.handlePatchForSamePath(any(), any(), any(), any()))
        .thenReturn(
            RisJsonPatch.builder()
                .patch(new JsonPatch(List.of()))
                .documentationUnitVersion(1L)
                .errorPaths(Collections.emptyList())
                .build());

    JsonNode valueToReplace = new TextNode("value");
    JsonPatchOperation replaceOp = new ReplaceOperation(path, valueToReplace);
    JsonPatch jsonPatch = new JsonPatch(List.of(replaceOp));
    var risJsonPatch = RisJsonPatch.builder().patch(jsonPatch).build();

    // Act
    service.updateDocumentationUnit(documentationUnit.uuid(), risJsonPatch, user);

    // Assert
    verify(duplicateCheckService, times(1)).checkDuplicates("ABCDE20220001");
  }

  @ParameterizedTest(name = "test patch with path: {0}, should not trigger duplicate check")
  @MethodSource("provideNonDuplicateCheckPaths")
  void testPatchUpdateWithoutCoreData_shouldNotTriggerDuplicateCheck(String path)
      throws DocumentationUnitNotExistsException {
    // Arrange
    DocumentationUnit documentationUnit =
        DocumentationUnit.builder()
            .uuid(UUID.randomUUID())
            .documentNumber("ABCDE20220001")
            .attachments(
                Collections.singletonList(
                    Attachment.builder().uploadTimestamp(Instant.now()).build()))
            .version(0L)
            .build();

    JsonNode valueToAdd = new TextNode("old value");
    JsonPatchOperation addOperation = new AddOperation(path, valueToAdd);
    JsonPatch patch = new JsonPatch(List.of(addOperation));
    User user = User.builder().build();

    when(repository.findByUuid(documentationUnit.uuid(), user)).thenReturn(documentationUnit);
    when(patchMapperService.calculatePatch(any(), any())).thenReturn(new JsonPatch(List.of()));
    when(patchMapperService.removePatchForSamePath(any(), any())).thenReturn(patch);
    when(patchMapperService.applyPatchToEntity(any(), any())).thenReturn(documentationUnit);
    when(patchMapperService.handlePatchForSamePath(any(), any(), any(), any()))
        .thenReturn(
            RisJsonPatch.builder()
                .patch(new JsonPatch(List.of()))
                .documentationUnitVersion(1L)
                .errorPaths(Collections.emptyList())
                .build());

    JsonNode valueToReplace = new TextNode("value");
    JsonPatchOperation replaceOp = new ReplaceOperation(path, valueToReplace);
    JsonPatch jsonPatch = new JsonPatch(List.of(replaceOp));
    var risJsonPatch = RisJsonPatch.builder().patch(jsonPatch).build();

    // Act
    service.updateDocumentationUnit(documentationUnit.uuid(), risJsonPatch, user);

    // Assert
    verify(duplicateCheckService, never()).checkDuplicates("ABCDE20220001");
  }

  @Test
  void testSearchByDocumentationUnitListEntry() throws DocumentationUnitNotExistsException {
    DocumentationUnitSearchInput documentationUnitSearchInput =
        DocumentationUnitSearchInput.builder().build();
    DocumentationUnitListItem documentationUnitListItem =
        DocumentationUnitListItem.builder().build();
    PageRequest pageRequest = PageRequest.of(0, 10);
    when(authService.userIsInternal()).thenReturn(user -> true);
    when(authService.isAssignedViaProcedure()).thenReturn(user -> true);
    when(repository.findByDocumentNumber(any()))
        .thenReturn(DocumentationUnit.builder().uuid(UUID.randomUUID()).build());
    when(repository.searchByDocumentationUnitSearchInput(
            pageRequest, oidcUser, documentationUnitSearchInput))
        .thenReturn(new PageImpl<>(List.of(documentationUnitListItem)));

    service.searchByDocumentationUnitSearchInput(
        pageRequest,
        oidcUser,
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty());
    verify(repository)
        .searchByDocumentationUnitSearchInput(pageRequest, oidcUser, documentationUnitSearchInput);
  }

  @Test
  void testSearchByDocumentationUnitListEntry_shouldNormalizeSpaces()
      throws DocumentationUnitNotExistsException {
    DocumentationUnitListItem documentationUnitListItem =
        DocumentationUnitListItem.builder().build();
    PageRequest pageRequest = PageRequest.of(0, 10);
    when(authService.userIsInternal()).thenReturn(user -> true);
    when(authService.isAssignedViaProcedure()).thenReturn(user -> true);
    when(repository.findByDocumentNumber(any()))
        .thenReturn(DocumentationUnit.builder().uuid(UUID.randomUUID()).build());
    when(repository.searchByDocumentationUnitSearchInput(
            any(PageRequest.class), any(OidcUser.class), any(DocumentationUnitSearchInput.class)))
        .thenReturn(new PageImpl<>(List.of(documentationUnitListItem)));

    service.searchByDocumentationUnitSearchInput(
        pageRequest,
        oidcUser,
        Optional.of("This\u00A0is\u202Fa\uFEFFtest\u2007docnumber\u180Ewith\u2060spaces"),
        Optional.of("This\u00A0is\u202Fa\uFEFFtest\u2007filenumber\u180Ewith\u2060spaces"),
        Optional.of("This\u00A0is\u202Fa\uFEFFtest\u2007courttype\u180Ewith\u2060spaces"),
        Optional.of("This\u00A0is\u202Fa\uFEFFtest\u2007courtlocation\u180Ewith\u2060spaces"),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty());

    // Capture the searchInput argument
    verify(repository)
        .searchByDocumentationUnitSearchInput(
            any(PageRequest.class), any(OidcUser.class), searchInputCaptor.capture());

    DocumentationUnitSearchInput capturedSearchInput = searchInputCaptor.getValue();

    // Verify that the searchInput fields have normalized spaces
    assertThat(capturedSearchInput.documentNumber())
        .isEqualTo("This is a test docnumber with spaces");
    assertThat(capturedSearchInput.fileNumber()).isEqualTo("This is a test filenumber with spaces");
    assertThat(capturedSearchInput.courtType()).isEqualTo("This is a test courttype with spaces");
    assertThat(capturedSearchInput.courtLocation())
        .isEqualTo("This is a test courtlocation with spaces");
  }

  @Test
  void testSearchLinkableDocumentationUnits_shouldNormalizeSpaces() {
    DocumentationOffice documentationOffice = DocumentationOffice.builder().build();
    RelatedDocumentationUnit relatedDocumentationUnit =
        RelatedDocumentationUnit.builder()
            .uuid(UUID.randomUUID())
            .fileNumber(
                "This\u00A0is\u202Fa\uFEFFtest\u2007filenumber\u180Ewith\u2060spaces.") // String
            // with
            // non-breaking space
            .build();
    PageRequest pageRequest = PageRequest.of(0, 10);
    String documentNumberToExclude = "DOC12345";

    // Configure the mock repository to return a non-null Slice object
    when(repository.searchLinkableDocumentationUnits(
            any(RelatedDocumentationUnit.class),
            any(DocumentationOffice.class),
            any(String.class),
            any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(relatedDocumentationUnit)));

    // Call the service method
    service.searchLinkableDocumentationUnits(
        relatedDocumentationUnit,
        documentationOffice,
        Optional.of(documentNumberToExclude),
        pageRequest);

    // Capture the relatedDocumentationUnit argument
    verify(repository)
        .searchLinkableDocumentationUnits(
            relatedDocumentationUnitCaptor.capture(),
            any(DocumentationOffice.class),
            any(String.class),
            any(Pageable.class));

    RelatedDocumentationUnit capturedRelatedDocumentationUnit =
        relatedDocumentationUnitCaptor.getValue();

    // Verify that the fileNumber field has normalized spaces
    assertThat(capturedRelatedDocumentationUnit.getFileNumber())
        .isEqualTo("This is a test filenumber with spaces.");
  }

  @Test
  void test_setPublicationDateTime_shouldSaveLastPublicationDateTime() {
    DocumentationUnit documentationUnit = DocumentationUnit.builder().build();

    service.setPublicationDateTime(documentationUnit.uuid());

    verify(repository, times(1)).saveLastPublicationDateTime(documentationUnit.uuid());
  }

  @Test
  void test_assignDocumentationOffice_shouldUnassignProcedures()
      throws DocumentationUnitNotExistsException {
    // Arrange
    User user = User.builder().build();
    DocumentationUnit documentationUnit = DocumentationUnit.builder().build();
    when(repository.findByUuid(documentationUnit.uuid(), user)).thenReturn(documentationUnit);

    // Act
    service.assignDocumentationOffice(documentationUnit.uuid(), UUID.randomUUID(), user);

    // Assert
    verify(repository, times(1)).unassignProcedures(documentationUnit.uuid());
  }

  @Test
  void test_assignDocumentationOffice_shouldSaveNewDocumentationOffice()
      throws DocumentationUnitNotExistsException {
    // Arrange
    User user = User.builder().build();
    UUID documentationOfficeId = UUID.randomUUID();
    DocumentationOffice documentationOffice =
        DocumentationOffice.builder().id(documentationOfficeId).build();
    DocumentationUnit documentationUnit = DocumentationUnit.builder().build();
    when(documentationOfficeService.findByUuid(documentationOfficeId))
        .thenReturn(documentationOffice);
    when(repository.findByUuid(documentationUnit.uuid(), user)).thenReturn(documentationUnit);

    // Act
    service.assignDocumentationOffice(documentationUnit.uuid(), documentationOfficeId, user);

    // Assert
    verify(repository, times(1))
        .saveDocumentationOffice(documentationUnit.uuid(), documentationOffice, user);
  }

  @Test
  void test_assignDocumentationOffice_withPendingProcedure_shouldThrowDocumentationUnitException()
      throws DocumentationUnitNotExistsException {
    // Arrange
    User user = User.builder().build();
    UUID documentationOfficeId = UUID.randomUUID();
    DocumentationOffice documentationOffice =
        DocumentationOffice.builder().id(documentationOfficeId).build();
    PendingProceeding documentationUnit = PendingProceeding.builder().build();
    when(repository.findByUuid(documentationUnit.uuid(), user)).thenReturn(documentationUnit);

    // Assert
    assertThatThrownBy(
            () ->
                // Act
                service.assignDocumentationOffice(
                    documentationUnit.uuid(), documentationOfficeId, user))
        .isInstanceOf(DocumentationUnitException.class)
        .hasMessageContaining(
            "The documentation office could not be reassigned: Document is not a decision.");

    verify(repository, never()).unassignProcedures(documentationUnit.uuid());
    verify(repository, never())
        .saveDocumentationOffice(documentationUnit.uuid(), documentationOffice, user);
  }

  @Test
  void test_assignDocumentationOffice_shouldThrowDocumentationUnitNotExistsException()
      throws DocumentationUnitNotExistsException {
    // Arrange
    User user = User.builder().build();
    UUID documentationOfficeId = UUID.randomUUID();
    DocumentationOffice documentationOffice =
        DocumentationOffice.builder().id(documentationOfficeId).build();
    PendingProceeding documentationUnit = PendingProceeding.builder().build();
    when(repository.findByUuid(documentationUnit.uuid(), user))
        .thenThrow(new DocumentationUnitNotExistsException());

    // Assert
    assertThatThrownBy(
            () ->
                // Act
                service.assignDocumentationOffice(
                    documentationUnit.uuid(), documentationOfficeId, user))
        .isInstanceOf(DocumentationUnitNotExistsException.class)
        .hasMessageContaining("Documentation unit does not exist");

    verify(repository, never()).unassignProcedures(documentationUnit.uuid());
    verify(repository, never())
        .saveDocumentationOffice(documentationUnit.uuid(), documentationOffice, user);
  }

  @Test
  void test_assignDocumentationOffice_shouldThrowDocumentationOfficeNotExistsException()
      throws DocumentationUnitNotExistsException {
    // Arrange
    User user = User.builder().build();
    UUID documentationOfficeId = UUID.randomUUID();
    DocumentationOffice documentationOffice =
        DocumentationOffice.builder().id(documentationOfficeId).build();
    DocumentationUnit documentationUnit = DocumentationUnit.builder().build();
    var errorMessage =
        String.format("The documentation office with id %s doesn't exist.", documentationOfficeId);
    when(documentationOfficeService.findByUuid(documentationOfficeId))
        .thenThrow(new DocumentationOfficeNotExistsException(errorMessage));
    when(repository.findByUuid(documentationUnit.uuid(), user)).thenReturn(documentationUnit);

    // Assert
    assertThatThrownBy(
            () ->
                // Act
                service.assignDocumentationOffice(
                    documentationUnit.uuid(), documentationOfficeId, user))
        .isInstanceOf(DocumentationOfficeNotExistsException.class)
        .hasMessageContaining(errorMessage);

    verify(repository, never()).unassignProcedures(documentationUnit.uuid());
    verify(repository, never())
        .saveDocumentationOffice(documentationUnit.uuid(), documentationOffice, user);
  }

  static Stream<String> provideDuplicateCheckPaths() {
    return Stream.of(
        "/coreData/ecli",
        "/coreData/deviatingEclis",
        "/coreData/fileNumbers",
        "/coreData/deviatingFileNumbers",
        "/coreData/court",
        "/coreData/deviatingCourts",
        "/coreData/decisionDate",
        "/coreData/deviatingDecisionDates",
        "/coreData/documentType");
  }

  static Stream<String> provideNonDuplicateCheckPaths() {
    return Stream.of(
        "/coreData/appraisalBody",
        "/coreData/procedure",
        "/coreData/procedure/createdAt",
        "/coreData/procedure/documentationUnitCount",
        "/coreData/procedure/label",
        "/coreData/procedure/id",
        "/coreData/legalEffect",
        "/coreData/leadingDecisionNormReferences",
        "/coreData/yearsOfDispute",
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
        "/version",
        "/longTexts/tenor",
        "/longTexts/reasons",
        "/longTexts/caseFacts",
        "/longTexts/decisionReasons",
        "/longTexts/dissentingOpinion",
        "/longTexts/otherLongText",
        "/longTexts/outline",
        "/caselawReferences",
        "/literatureReferences");
  }
}
