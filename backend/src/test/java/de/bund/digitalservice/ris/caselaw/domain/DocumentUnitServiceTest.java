package de.bund.digitalservice.ris.caselaw.domain;

import static de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationType.ACTIVE_CITATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyBoolean;
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
import jakarta.validation.Validator;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;

@ExtendWith(SpringExtension.class)
@Import({DocumentUnitService.class, DatabaseDocumentUnitStatusService.class})
class DocumentUnitServiceTest {
  private static final UUID TEST_UUID = UUID.fromString("88888888-4444-4444-4444-121212121212");
  private static final String ISSUER_ADDRESS = "test-issuer@exporter.neuris";
  @SpyBean private DocumentUnitService service;

  @MockBean private DocumentUnitRepository repository;
  @MockBean private DocumentNumberService documentNumberService;
  @MockBean private DocumentNumberRecyclingService documentNumberRecyclingService;
  @MockBean private EmailPublishService publishService;
  @MockBean private PublicationReportRepository publicationReportRepository;
  @MockBean private DatabaseDocumentUnitStatusService documentUnitStatusService;
  @MockBean private DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @MockBean private AttachmentService attachmentService;
  @MockBean private FeatureToggleService featureService;
  @MockBean private Validator validator;

  @Test
  void testGenerateNewDocumentUnit()
      throws DocumentationUnitExistsException,
          DocumentNumberPatternException,
          DocumentNumberFormatterException,
          DocumentationUnitNotExistsException {
    DocumentationOffice documentationOffice = DocumentationOffice.builder().build();
    DocumentUnit documentUnit = DocumentUnit.builder().build();

    when(repository.createNewDocumentUnit("nextDocumentNumber", documentationOffice))
        .thenReturn(documentUnit);
    when(documentNumberService.generateDocumentNumber(documentationOffice.abbreviation(), 5))
        .thenReturn("nextDocumentNumber");
    when(documentUnitStatusService.setInitialStatus(documentUnit)).thenReturn(documentUnit);
    // Can we use a captor to check if the document number was correctly created?
    // The chicken-egg-problem is, that we are dictating what happens when
    // repository.save(), so we can't just use a captor at the same time

    service.generateNewDocumentUnit(documentationOffice);
    // TODO.expectNextCount(1) // That it's a DocumentUnit is given by the generic extension..

    verify(documentNumberService).generateDocumentNumber(documentationOffice.abbreviation(), 5);
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
  void testDeleteByUuid_withoutFileAttached() {
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
  void testDeleteByUuid_withFileAttached() {
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

    // TODO .expectError()
    service.deleteByUuid(TEST_UUID);

    verify(repository).findByUuid(TEST_UUID);
  }

  @Test
  void testDeleteByUuid_withLinks() {
    when(repository.findByUuid(TEST_UUID))
        .thenReturn(Optional.ofNullable(DocumentUnit.builder().build()));
    when(repository.getAllDocumentationUnitWhichLink(TEST_UUID))
        .thenReturn(Map.of(ACTIVE_CITATION, 2L));
    // TODO throwable.getMessage().contains("Die Dokumentationseinheit konnte nicht gelöscht werden,
    // da (2: Aktivzitierung,)"))
    Assertions.assertNull(service.deleteByUuid(TEST_UUID));
  }

  @Test
  void testUpdateDocumentUnit() {
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

    verify(repository).save(eq(documentUnit), anyBoolean());
  }

  @Test
  void testPublishByEmail() throws DocumentationUnitNotExistsException {
    when(repository.findByUuid(TEST_UUID))
        .thenReturn(Optional.ofNullable(DocumentUnit.builder().build()));
    XmlPublication xmlPublication =
        XmlPublication.builder()
            .documentUnitUuid(TEST_UUID)
            .receiverAddress("receiver address")
            .mailSubject("subject")
            .xml("xml")
            .statusCode("200")
            .statusMessages(List.of("status messages"))
            .fileName("filename")
            .publishDate(Instant.now())
            .build();
    when(publishService.publish(eq(DocumentUnit.builder().build()), anyString()))
        .thenReturn(xmlPublication);
    when(documentUnitStatusService.setToPublishing(
            any(DocumentUnit.class), any(Instant.class), anyString()))
        .thenReturn(DocumentUnit.builder().build());
    var mailResponse = service.publishAsEmail(TEST_UUID, ISSUER_ADDRESS);
    assertThat(mailResponse).usingRecursiveComparison().isEqualTo(xmlPublication);
    verify(repository).findByUuid(TEST_UUID);
    verify(publishService).publish(eq(DocumentUnit.builder().build()), anyString());
    verify(documentUnitStatusService)
        .setToPublishing(any(DocumentUnit.class), any(Instant.class), anyString());
  }

  @Test
  void testPublishByEmail_withoutDocumentUnitForUuid() {
    when(repository.findByUuid(TEST_UUID)).thenReturn(Optional.empty());

    Assertions.assertThrows(
        DocumentationUnitNotExistsException.class,
        () -> service.publishAsEmail(TEST_UUID, ISSUER_ADDRESS));
    verify(repository).findByUuid(TEST_UUID);
    verify(publishService, never()).publish(eq(DocumentUnit.builder().build()), anyString());
  }

  @Test
  void testGetLastXmlPublication() {
    XmlPublication xmlPublication =
        XmlPublication.builder()
            .documentUnitUuid(TEST_UUID)
            .receiverAddress("receiver address")
            .mailSubject("subject")
            .xml("xml")
            .statusCode("200")
            .statusMessages(List.of("message"))
            .fileName("filename")
            .build();
    when(publishService.getPublications(TEST_UUID)).thenReturn(List.of(xmlPublication));
    when(publicationReportRepository.getAllByDocumentUnitUuid(TEST_UUID))
        .thenReturn(Collections.emptyList());

    var actual = service.getPublicationHistory(TEST_UUID);
    assertThat(actual.get(0)).usingRecursiveComparison().isEqualTo(xmlPublication);

    verify(publishService).getPublications(TEST_UUID);
  }

  @Test
  void testGetLastPublicationReport() {
    PublicationReport report =
        new PublicationReport("documentNumber", "<html></html>", Instant.now());
    when(publicationReportRepository.getAllByDocumentUnitUuid(TEST_UUID))
        .thenReturn(List.of(report));
    when(publishService.getPublications(TEST_UUID)).thenReturn(List.of());

    var publications = service.getPublicationHistory(TEST_UUID);
    assertThat(publications.get(0)).usingRecursiveComparison().isEqualTo(report);

    verify(publishService).getPublications(TEST_UUID);
  }

  @Test
  void testGetSortedPublicationLog() {
    Instant newest = Instant.now();
    Instant secondNewest = newest.minusSeconds(61);
    Instant thirdNewest = secondNewest.minusSeconds(61);
    Instant fourthNewest = thirdNewest.minusSeconds(61);

    PublicationReport report1 = new PublicationReport("documentNumber", "<html></html>", newest);

    XmlPublication xml1 =
        XmlPublication.builder()
            .documentUnitUuid(TEST_UUID)
            .receiverAddress("receiver address")
            .mailSubject("subject")
            .xml("xml")
            .statusCode("200")
            .statusMessages(List.of("message"))
            .fileName("filename")
            .publishDate(secondNewest)
            .build();

    PublicationReport report2 =
        new PublicationReport("documentNumber", "<html></html>", thirdNewest);

    XmlPublication xml2 =
        XmlPublication.builder()
            .documentUnitUuid(TEST_UUID)
            .receiverAddress("receiver address")
            .mailSubject("subject")
            .xml("xml")
            .statusCode("200")
            .statusMessages(List.of("message"))
            .fileName("filename")
            .publishDate(fourthNewest)
            .build();

    when(publicationReportRepository.getAllByDocumentUnitUuid(TEST_UUID))
        .thenReturn(List.of(report2, report1));
    when(publishService.getPublications(TEST_UUID)).thenReturn(List.of(xml2, xml1));

    List<PublicationHistoryRecord> list = service.getPublicationHistory(TEST_UUID);
    assertThat(list).hasSize(4);
    assertThat(list.get(0)).usingRecursiveComparison().isEqualTo(report1);
    assertThat(list.get(1)).usingRecursiveComparison().isEqualTo(xml1);
    assertThat(list.get(2)).usingRecursiveComparison().isEqualTo(report2);
    assertThat(list.get(3)).usingRecursiveComparison().isEqualTo(xml2);
    verify(publishService).getPublications(TEST_UUID);
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

    Slice<DocumentationUnitListItem> documentationUnitSearchEntries =
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
    // if doc office is empty, user is not allowed to edit
    assertThat(documentationUnitSearchEntries)
        .contains(documentationUnitListItem.toBuilder().isEditableByCurrentUser(false).build());
    verify(repository)
        .searchByDocumentationUnitSearchInput(
            pageRequest, documentationOffice, documentationUnitSearchInput);
  }

  @Test
  void testSearchByDocumentUnitListEntry_shouldSetEditPermissions() {
    DocumentationOffice documentationOffice =
        DocumentationOffice.builder().abbreviation("DS").build();

    DocumentationUnitListItem withoutOffice = DocumentationUnitListItem.builder().build();
    DocumentationUnitListItem withSameOffice =
        DocumentationUnitListItem.builder().documentationOffice(documentationOffice).build();
    DocumentationUnitListItem withDifferentOffice =
        DocumentationUnitListItem.builder()
            .documentationOffice(DocumentationOffice.builder().abbreviation("FOO").build())
            .build();

    PageRequest pageRequest = PageRequest.of(0, 10);
    DocumentationUnitSearchInput documentationUnitSearchInput =
        DocumentationUnitSearchInput.builder().build();

    when(repository.searchByDocumentationUnitSearchInput(
            pageRequest, documentationOffice, documentationUnitSearchInput))
        .thenReturn(new PageImpl<>(List.of(withoutOffice, withSameOffice, withDifferentOffice)));

    Slice<DocumentationUnitListItem> documentationUnitSearchEntries =
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
    // if doc office is empty, user is not allowed to edit
    assertThat(documentationUnitSearchEntries)
        .containsAll(
            List.of(
                withoutOffice.toBuilder().isEditableByCurrentUser(false).build(),
                withSameOffice.toBuilder().isEditableByCurrentUser(true).build(),
                withDifferentOffice.toBuilder().isEditableByCurrentUser(false).build()));
    verify(repository)
        .searchByDocumentationUnitSearchInput(
            pageRequest, documentationOffice, documentationUnitSearchInput);
  }

  @Test
  void testSearchByDocumentUnitListEntry_shouldRestrictAccessIfUserDocOfficeIsEmpty() {
    DocumentationUnitListItem withoutOffice = DocumentationUnitListItem.builder().build();
    DocumentationUnitListItem withOffice =
        DocumentationUnitListItem.builder()
            .documentationOffice(DocumentationOffice.builder().abbreviation("FOO").build())
            .build();

    DocumentationOffice documentationOffice = DocumentationOffice.builder().build();
    PageRequest pageRequest = PageRequest.of(0, 10);
    DocumentationUnitSearchInput documentationUnitSearchInput =
        DocumentationUnitSearchInput.builder().build();

    when(repository.searchByDocumentationUnitSearchInput(
            pageRequest, documentationOffice, documentationUnitSearchInput))
        .thenReturn(new PageImpl<>(List.of(withoutOffice, withOffice)));

    Slice<DocumentationUnitListItem> documentationUnitSearchEntries =
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
    // if doc office is empty, user is not allowed to edit
    assertThat(documentationUnitSearchEntries)
        .containsAll(
            List.of(
                withoutOffice.toBuilder().isEditableByCurrentUser(false).build(),
                withOffice.toBuilder().isEditableByCurrentUser(false).build()));
    verify(repository)
        .searchByDocumentationUnitSearchInput(
            pageRequest, documentationOffice, documentationUnitSearchInput);
  }

  @Test
  void testPreviewPublication() throws DocumentationUnitNotExistsException {
    DocumentUnit testDocumentUnit = DocumentUnit.builder().build();
    XmlResultObject mockXmlResultObject =
        new XmlResultObject("some xml", "200", List.of("success"), "foo.xml", Instant.now());
    when(repository.findByUuid(TEST_UUID)).thenReturn(Optional.ofNullable(testDocumentUnit));
    when(publishService.getPublicationPreview(testDocumentUnit)).thenReturn(mockXmlResultObject);

    Assertions.assertEquals(mockXmlResultObject, service.previewPublication(TEST_UUID));
  }

  private CompletableFuture<DeleteObjectResponse> buildEmptyDeleteObjectResponse() {
    return CompletableFuture.completedFuture(DeleteObjectResponse.builder().build());
  }
}
