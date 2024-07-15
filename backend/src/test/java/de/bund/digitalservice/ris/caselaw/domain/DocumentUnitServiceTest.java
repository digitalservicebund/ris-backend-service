package de.bund.digitalservice.ris.caselaw.domain;

import static de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationType.ACTIVE_CITATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseStatusRepository;
import jakarta.validation.Validator;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({DocumentUnitService.class, DatabaseDocumentUnitStatusService.class})
class DocumentUnitServiceTest {
  private static final UUID TEST_UUID = UUID.fromString("88888888-4444-4444-4444-121212121212");
  @SpyBean private DocumentUnitService service;

  @MockBean private DatabaseDocumentationUnitRepository documentationUnitRepository;
  @MockBean private DatabaseStatusRepository statusRepository;
  @MockBean private DocumentUnitRepository repository;
  @MockBean private DocumentNumberService documentNumberService;
  @MockBean private DocumentNumberRecyclingService documentNumberRecyclingService;
  @MockBean private EmailService emailService;
  @MockBean private DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @MockBean private AttachmentService attachmentService;
  @MockBean private Validator validator;
  @Captor private ArgumentCaptor<DocumentationUnitSearchInput> searchInputCaptor;
  @Captor private ArgumentCaptor<RelatedDocumentationUnit> relatedDocumentationUnitCaptor;

  @Test
  void testGenerateNewDocumentUnit()
      throws DocumentationUnitExistsException,
          DocumentNumberPatternException,
          DocumentNumberFormatterException {
    DocumentationOffice documentationOffice = DocumentationOffice.builder().build();
    DocumentUnit documentUnit = DocumentUnit.builder().build();

    when(repository.createNewDocumentUnit("nextDocumentNumber", documentationOffice))
        .thenReturn(documentUnit);
    when(documentNumberService.generateDocumentNumber(documentationOffice.abbreviation()))
        .thenReturn("nextDocumentNumber");
    // Can we use a captor to check if the document number was correctly created?
    // The chicken-egg-problem is, that we are dictating what happens when
    // repository.save(), so we can't just use a captor at the same time

    Assertions.assertNotNull(service.generateNewDocumentUnit(documentationOffice));

    verify(documentNumberService).generateDocumentNumber(documentationOffice.abbreviation());
    verify(repository).createNewDocumentUnit("nextDocumentNumber", documentationOffice);
  }

  @Test
  void testGetByDocumentnumber() {
    when(repository.findByDocumentNumber("ABCDE20220001"))
        .thenReturn(Optional.of(DocumentUnit.builder().build()));
    var documentUnit = service.getByDocumentNumber("ABCDE20220001");
    assertEquals(documentUnit.getClass(), DocumentUnit.class);

    verify(repository).findByDocumentNumber("ABCDE20220001");
  }

  @Test
  void testDeleteByUuid_withoutFileAttached() throws DocumentationUnitNotExistsException {
    // I think I shouldn't have to insert a specific DocumentUnit object here?
    // But if I don't, the test by itself succeeds, but fails if all tests in this class run
    // something flaky with the repository mock? Investigate this later
    DocumentUnit documentUnit = DocumentUnit.builder().uuid(TEST_UUID).build();
    // can we also test that the fileUuid from the DocumentUnit is used? with a captor somehow?
    when(repository.findByUuid(TEST_UUID)).thenReturn(Optional.of(documentUnit));

    var string = service.deleteByUuid(TEST_UUID);
    assertNotNull(string);
    assertEquals("Dokumentationseinheit gelöscht: " + TEST_UUID, string);

    verify(attachmentService, times(0)).deleteAllObjectsFromBucketForDocumentationUnit(TEST_UUID);
  }

  @Test
  void testDeleteByUuid_withFileAttached() throws DocumentationUnitNotExistsException {
    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(TEST_UUID)
            .attachments(
                Collections.singletonList(
                    Attachment.builder().s3path(TEST_UUID.toString()).build()))
            .build();

    when(repository.findByUuid(TEST_UUID)).thenReturn(Optional.ofNullable(documentUnit));

    var string = service.deleteByUuid(TEST_UUID);
    assertNotNull(string);
    assertEquals("Dokumentationseinheit gelöscht: " + TEST_UUID, string);

    verify(attachmentService).deleteAllObjectsFromBucketForDocumentationUnit(TEST_UUID);
  }

  @Test
  void testDeleteByUuid_withoutFileAttached_withExceptionFromRepository() {
    when(repository.findByUuid(TEST_UUID))
        .thenReturn(Optional.ofNullable(DocumentUnit.builder().build()));
    doThrow(new IllegalArgumentException()).when(repository).delete(DocumentUnit.builder().build());

    Assertions.assertThrows(IllegalArgumentException.class, () -> service.deleteByUuid(TEST_UUID));

    verify(repository).findByUuid(TEST_UUID);
  }

  @Test
  void testDeleteByUuid_withLinks() {
    when(repository.findByUuid(TEST_UUID))
        .thenReturn(Optional.ofNullable(DocumentUnit.builder().build()));
    when(repository.getAllDocumentationUnitWhichLink(TEST_UUID))
        .thenReturn(Map.of(ACTIVE_CITATION, 2L));
    DocumentUnitDeletionException throwable =
        Assertions.assertThrows(
            DocumentUnitDeletionException.class, () -> service.deleteByUuid(TEST_UUID));
    Assertions.assertTrue(
        throwable
            .getMessage()
            .contains(
                "Die Dokumentationseinheit konnte nicht gelöscht werden, da (2: Aktivzitierung,)"));
  }

  @Test
  void testUpdateDocumentUnit() throws DocumentationUnitNotExistsException {
    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(UUID.randomUUID())
            .documentNumber("ABCDE20220001")
            .attachments(
                Collections.singletonList(
                    Attachment.builder().uploadTimestamp(Instant.now()).build()))
            .build();
    when(repository.findByUuid(documentUnit.uuid())).thenReturn(Optional.of(documentUnit));

    var du = service.updateDocumentUnit(documentUnit);
    assertEquals(du, documentUnit);

    verify(repository).save(documentUnit);
  }

  @Test
  void testSearchByDocumentUnitListEntry() {
    DocumentationOffice documentationOffice = DocumentationOffice.builder().build();
    DocumentationUnitSearchInput documentationUnitSearchInput =
        DocumentationUnitSearchInput.builder().build();
    DocumentationUnitListItem documentationUnitListItem =
        DocumentationUnitListItem.builder().build();
    PageRequest pageRequest = PageRequest.of(0, 10);

    when(repository.searchByDocumentationUnitSearchInput(
            pageRequest, documentationOffice, documentationUnitSearchInput))
        .thenReturn(new PageImpl<>(List.of(documentationUnitListItem)));

    service.searchByDocumentationUnitSearchInput(
        pageRequest,
        documentationOffice,
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
        .searchByDocumentationUnitSearchInput(
            pageRequest, documentationOffice, documentationUnitSearchInput);
  }

  @Test
  void testSearchByDocumentUnitListEntry_shouldNormalizeSpaces() {
    DocumentationOffice documentationOffice = DocumentationOffice.builder().build();
    DocumentationUnitListItem documentationUnitListItem =
        DocumentationUnitListItem.builder().build();
    PageRequest pageRequest = PageRequest.of(0, 10);

    when(repository.searchByDocumentationUnitSearchInput(
            any(PageRequest.class),
            any(DocumentationOffice.class),
            any(DocumentationUnitSearchInput.class)))
        .thenReturn(new PageImpl<>(List.of(documentationUnitListItem)));

    service.searchByDocumentationUnitSearchInput(
        pageRequest,
        documentationOffice,
        Optional.of("This\u00A0is\u202Fa\uFEFFtest\u2007docnumber\u180Ewith\u2060spaces"),
        Optional.of("This\u00A0is\u202Fa\uFEFFtest\u2007filenumber\u180Ewith\u2060spaces"),
        Optional.of("This\u00A0is\u202Fa\uFEFFtest\u2007courttype\u180Ewith\u2060spaces"),
        Optional.of("This\u00A0is\u202Fa\uFEFFtest\u2007courtlocation\u180Ewith\u2060spaces"),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty());

    // Capture the searchInput argument
    verify(repository)
        .searchByDocumentationUnitSearchInput(
            any(PageRequest.class), any(DocumentationOffice.class), searchInputCaptor.capture());

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
        relatedDocumentationUnit, documentationOffice, documentNumberToExclude, pageRequest);

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
}
