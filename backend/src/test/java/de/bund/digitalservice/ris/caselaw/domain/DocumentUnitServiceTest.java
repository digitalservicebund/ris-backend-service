package de.bund.digitalservice.ris.caselaw.domain;

import static de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationType.ACTIVE_CITATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
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
  private static final String ISSUER_ADDRESS = "test-issuer@exporter.neuris";
  @SpyBean private DocumentUnitService service;

  @MockBean private DatabaseDocumentationUnitRepository documentationUnitRepository;
  @MockBean private DatabaseStatusRepository statusRepository;
  @MockBean private DocumentUnitRepository repository;
  @MockBean private DocumentNumberService documentNumberService;
  @MockBean private DocumentNumberRecyclingService documentNumberRecyclingService;
  @MockBean private EmailService emailService;
  @MockBean private HandoverReportRepository handoverReportRepository;
  @MockBean private DeltaMigrationRepository deltaMigrationRepository;
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
  void testHandoverByEmail() throws DocumentationUnitNotExistsException {
    when(repository.findByUuid(TEST_UUID))
        .thenReturn(Optional.ofNullable(DocumentUnit.builder().build()));
    XmlHandoverMail xmlHandoverMail =
        XmlHandoverMail.builder()
            .documentUnitUuid(TEST_UUID)
            .receiverAddress("receiver address")
            .mailSubject("subject")
            .xml("xml")
            .success(true)
            .statusMessages(List.of("status messages"))
            .fileName("filename")
            .handoverDate(Instant.now())
            .build();
    when(emailService.handOver(eq(DocumentUnit.builder().build()), anyString(), anyString()))
        .thenReturn(xmlHandoverMail);
    var mailResponse = service.handoverAsEmail(TEST_UUID, ISSUER_ADDRESS);
    assertThat(mailResponse).usingRecursiveComparison().isEqualTo(xmlHandoverMail);
    verify(repository).findByUuid(TEST_UUID);
    verify(emailService).handOver(eq(DocumentUnit.builder().build()), anyString(), anyString());
  }

  @Test
  void testHandoverByEmail_withoutDocumentUnitForUuid() {
    when(repository.findByUuid(TEST_UUID)).thenReturn(Optional.empty());

    Assertions.assertThrows(
        DocumentationUnitNotExistsException.class,
        () -> service.handoverAsEmail(TEST_UUID, ISSUER_ADDRESS));
    verify(repository).findByUuid(TEST_UUID);
    verify(emailService, never())
        .handOver(eq(DocumentUnit.builder().build()), anyString(), anyString());
  }

  @Test
  void testGetLastXmlHandoverMail() {
    XmlHandoverMail xmlHandoverMail =
        XmlHandoverMail.builder()
            .documentUnitUuid(TEST_UUID)
            .receiverAddress("receiver address")
            .mailSubject("subject")
            .xml("xml")
            .success(true)
            .statusMessages(List.of("message"))
            .fileName("filename")
            .handoverDate(Instant.now().minus(2, java.time.temporal.ChronoUnit.DAYS))
            .build();
    when(emailService.getHandoverResult(TEST_UUID)).thenReturn(List.of(xmlHandoverMail));
    when(handoverReportRepository.getAllByDocumentUnitUuid(TEST_UUID))
        .thenReturn(Collections.emptyList());
    DeltaMigration deltaMigration =
        DeltaMigration.builder()
            .migratedDate(Instant.now().minus(1, java.time.temporal.ChronoUnit.DAYS))
            .xml("<test><element></element></test>")
            .build();
    when(deltaMigrationRepository.getLatestMigration(TEST_UUID)).thenReturn(deltaMigration);

    var actual = service.getEventLog(TEST_UUID);
    assertThat(actual.get(1)).usingRecursiveComparison().isEqualTo(xmlHandoverMail);
    assertThat(actual.get(0))
        .usingRecursiveComparison()
        .isEqualTo(
            deltaMigration.toBuilder()
                .xml("<?xml version=\"1.0\" encoding=\"UTF-8\"?><test>\n  <element/>\n</test>\n")
                .build());

    verify(emailService).getHandoverResult(TEST_UUID);
    verify(deltaMigrationRepository).getLatestMigration(TEST_UUID);
  }

  @Test
  void testGetLastMigrated() {
    DeltaMigration deltaMigration =
        DeltaMigration.builder()
            .migratedDate(Instant.now().minus(1, java.time.temporal.ChronoUnit.DAYS))
            .xml("<test><element></element></test>")
            .build();
    when(deltaMigrationRepository.getLatestMigration(TEST_UUID)).thenReturn(deltaMigration);

    var actual = service.getEventLog(TEST_UUID);
    assertThat(actual.get(0))
        .usingRecursiveComparison()
        .isEqualTo(
            deltaMigration.toBuilder()
                .xml("<?xml version=\"1.0\" encoding=\"UTF-8\"?><test>\n  <element/>\n</test>\n")
                .build());

    verify(deltaMigrationRepository).getLatestMigration(TEST_UUID);
  }

  @Test
  void testGetLastHandoverReport() {
    HandoverReport report = new HandoverReport("documentNumber", "<html></html>", Instant.now());
    when(handoverReportRepository.getAllByDocumentUnitUuid(TEST_UUID)).thenReturn(List.of(report));
    when(emailService.getHandoverResult(TEST_UUID)).thenReturn(List.of());
    when(deltaMigrationRepository.getLatestMigration(TEST_UUID)).thenReturn(null);

    var events = service.getEventLog(TEST_UUID);
    assertThat(events.get(0)).usingRecursiveComparison().isEqualTo(report);

    verify(emailService).getHandoverResult(TEST_UUID);
  }

  @Test
  void testGetSortedEventLog() {
    Instant newest = Instant.now();
    Instant secondNewest = newest.minusSeconds(61);
    Instant thirdNewest = secondNewest.minusSeconds(61);
    Instant fourthNewest = thirdNewest.minusSeconds(61);
    Instant fifthNewest = fourthNewest.minusSeconds(61);

    HandoverReport report1 = new HandoverReport("documentNumber", "<html></html>", newest);

    XmlHandoverMail xml1 =
        XmlHandoverMail.builder()
            .documentUnitUuid(TEST_UUID)
            .receiverAddress("receiver address")
            .mailSubject("subject")
            .xml("xml")
            .success(true)
            .statusMessages(List.of("message"))
            .fileName("filename")
            .handoverDate(secondNewest)
            .build();

    HandoverReport report2 = new HandoverReport("documentNumber", "<html></html>", thirdNewest);

    XmlHandoverMail xml2 =
        XmlHandoverMail.builder()
            .documentUnitUuid(TEST_UUID)
            .receiverAddress("receiver address")
            .mailSubject("subject")
            .xml("xml")
            .success(true)
            .statusMessages(List.of("message"))
            .fileName("filename")
            .handoverDate(fourthNewest)
            .build();

    DeltaMigration deltaMigration = DeltaMigration.builder().migratedDate(fifthNewest).build();

    when(handoverReportRepository.getAllByDocumentUnitUuid(TEST_UUID))
        .thenReturn(List.of(report2, report1));
    when(emailService.getHandoverResult(TEST_UUID)).thenReturn(List.of(xml2, xml1));
    when(deltaMigrationRepository.getLatestMigration(TEST_UUID)).thenReturn(deltaMigration);

    List<EventRecord> list = service.getEventLog(TEST_UUID);
    assertThat(list).hasSize(5);
    assertThat(list.get(0)).usingRecursiveComparison().isEqualTo(report1);
    assertThat(list.get(1)).usingRecursiveComparison().isEqualTo(xml1);
    assertThat(list.get(2)).usingRecursiveComparison().isEqualTo(report2);
    assertThat(list.get(3)).usingRecursiveComparison().isEqualTo(xml2);
    assertThat(list.get(4)).usingRecursiveComparison().isEqualTo(deltaMigration);
    verify(emailService).getHandoverResult(TEST_UUID);
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

  @Test
  void testPreviewXml() throws DocumentationUnitNotExistsException {
    DocumentUnit testDocumentUnit = DocumentUnit.builder().build();
    XmlExportResult mockXmlExportResult =
        new XmlExportResult("some xml", true, List.of("success"), "foo.xml", Instant.now());
    when(repository.findByUuid(TEST_UUID)).thenReturn(Optional.ofNullable(testDocumentUnit));
    when(emailService.getXmlPreview(testDocumentUnit)).thenReturn(mockXmlExportResult);

    Assertions.assertEquals(mockXmlExportResult, service.createPreviewXml(TEST_UUID));
  }

  @Test
  void testPrettifyXml() {
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><child>value</child></root>";
    String prettyXml = DocumentUnitService.prettifyXml(xml);
    assertThat(prettyXml)
        .isEqualTo(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root>\n  <child>value</child>\n</root>\n");
  }
}
