package de.bund.digitalservice.ris.caselaw.domain;

import static de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationType.ACTIVE_CITATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import jakarta.validation.Validator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
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
  @MockBean private Validator validator;

  @Test
  void testGenerateNewDocumentUnit()
      throws DocumentationUnitExistsException,
          DocumentNumberPatternException,
          DocumentNumberFormatterException {
    DocumentationOffice documentationOffice = DocumentationOffice.builder().build();
    DocumentUnit documentUnit = DocumentUnit.builder().build();

    when(repository.createNewDocumentUnit("nextDocumentNumber", documentationOffice))
        .thenReturn(Mono.just(documentUnit));
    when(documentNumberService.generateDocumentNumber(documentationOffice.abbreviation(), 5))
        .thenReturn("nextDocumentNumber");
    when(documentUnitStatusService.setInitialStatus(documentUnit))
        .thenReturn(Mono.just(documentUnit));
    // Can we use a captor to check if the document number was correctly created?
    // The chicken-egg-problem is, that we are dictating what happens when
    // repository.save(), so we can't just use a captor at the same time

    StepVerifier.create(service.generateNewDocumentUnit(documentationOffice))
        .expectNextCount(1) // That it's a DocumentUnit is given by the generic extension..
        .verifyComplete();
    verify(documentNumberService).generateDocumentNumber(documentationOffice.abbreviation(), 5);
    verify(repository).createNewDocumentUnit("nextDocumentNumber", documentationOffice);
  }

  //  @Test
  //  void testGenerateNewDocumentUnitAndAttachFile_withExceptionFromBucket() throws S3Exception {
  //    // given
  //    var byteBuffer = ByteBuffer.wrap(new byte[] {});
  //
  //    doNothing().when(service).checkDocx(any(ByteBuffer.class));
  //    when(s3AsyncClient.putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class)))
  //        .thenThrow(SdkException.create("exception", null));
  //
  //    // when and then
  //    StepVerifier.create(service.attachFileToDocumentUnit(TEST_UUID, byteBuffer,
  // HttpHeaders.EMPTY))
  //        .expectErrorMatches(ex -> ex instanceof SdkException)
  //        .verify();
  //
  //    verify(s3AsyncClient).putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class));
  //    verify(repository, times(0)).save(any(DocumentUnit.class));
  //  }

  @Test
  void testGetByDocumentnumber() {
    when(repository.findByDocumentNumber("ABCDE20220001"))
        .thenReturn(Optional.of(DocumentUnit.builder().build()));
    StepVerifier.create(service.getByDocumentNumber("ABCDE20220001"))
        .consumeNextWith(documentUnit -> assertEquals(documentUnit.getClass(), DocumentUnit.class))
        .verifyComplete();
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

    StepVerifier.create(service.deleteByUuid(TEST_UUID))
        .consumeNextWith(
            string -> {
              assertNotNull(string);
              assertEquals("Dokumentationseinheit gelöscht: " + TEST_UUID, string);
            })
        .verifyComplete();

    verify(attachmentService, times(0)).deleteAllObjectsFromBucketForDocumentationUnit(TEST_UUID);
  }

  //  @Test
  //  void testDeleteByUuid_withFileAttached() {
  //    DocumentUnit documentUnit =
  //        DocumentUnit.builder().uuid(TEST_UUID)
  //
  // .attachments(Collections.singletonList(OriginalFileDocument.builder().s3path(TEST_UUID.toString()).build()))
  //                .build();
  //
  //    when(repository.findByUuid(TEST_UUID)).thenReturn(Optional.ofNullable(documentUnit));
  //    when(s3AsyncClient.deleteObject(any(DeleteObjectRequest.class)))
  //        .thenReturn(buildEmptyDeleteObjectResponse());
  //
  //    StepVerifier.create(service.deleteByUuid(TEST_UUID))
  //        .consumeNextWith(
  //            string -> {
  //              assertNotNull(string);
  //              assertEquals("Dokumentationseinheit gelöscht: " + TEST_UUID, string);
  //            })
  //        .verifyComplete();
  //
  //    verify(s3AsyncClient, times(1)).deleteObject(any(DeleteObjectRequest.class));
  //  }

  //  @Test
  //  void testDeleteByUuid_withoutFileAttached_withExceptionFromBucket() {
  //    when(repository.findByUuid(TEST_UUID))
  //        .thenReturn(Optional.ofNullable(DocumentUnit.builder()
  //
  // .attachments(Collections.singletonList(OriginalFileDocument.builder().s3path("fooPath").build()))
  //                .build()));
  //    when(s3AsyncClient.deleteObject(any(DeleteObjectRequest.class)))
  //        .thenThrow(SdkException.create("exception", null));
  //
  //    StepVerifier.create(service.deleteByUuid(TEST_UUID)).expectError().verify();
  //
  //    verify(repository).findByUuid(TEST_UUID);
  //  }

  //  @Test
  //  void testDeleteByUuid_withoutFileAttached_withExceptionFromRepository() {
  //    when(repository.findByUuid(TEST_UUID))
  //        .thenReturn(Optional.ofNullable(DocumentUnit.builder().build()));
  //    doThrow(new
  // IllegalArgumentException()).when(repository).delete(DocumentUnit.builder().build());
  //
  //    StepVerifier.create(service.deleteByUuid(TEST_UUID)).expectError().verify();
  //
  //    verify(repository).findByUuid(TEST_UUID);
  //  }

  @Test
  void testDeleteByUuid_withLinks() {
    when(repository.findByUuid(TEST_UUID))
        .thenReturn(Optional.ofNullable(DocumentUnit.builder().build()));
    when(repository.getAllDocumentationUnitWhichLink(TEST_UUID))
        .thenReturn(Map.of(ACTIVE_CITATION, 2L));

    StepVerifier.create(service.deleteByUuid(TEST_UUID))
        .expectErrorMatches(
            throwable ->
                throwable instanceof DocumentUnitDeletionException
                    && throwable
                        .getMessage()
                        .contains(
                            "Die Dokumentationseinheit konnte nicht gelöscht werden, da (2: Aktivzitierung,)"))
        .verify();
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

    StepVerifier.create(service.updateDocumentUnit(documentUnit))
        .consumeNextWith(du -> assertEquals(du, documentUnit))
        .verifyComplete();

    verify(repository).save(documentUnit);
  }

  @Test
  void testPublishByEmail() {
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
        .thenReturn(Mono.just(xmlPublication));
    when(documentUnitStatusService.setToPublishing(
            any(DocumentUnit.class), any(Instant.class), anyString()))
        .thenReturn(Mono.just(DocumentUnit.builder().build()));
    StepVerifier.create(service.publishAsEmail(TEST_UUID, ISSUER_ADDRESS))
        .consumeNextWith(
            mailResponse ->
                assertThat(mailResponse).usingRecursiveComparison().isEqualTo(xmlPublication))
        .verifyComplete();
    verify(repository).findByUuid(TEST_UUID);
    verify(publishService).publish(eq(DocumentUnit.builder().build()), anyString());
    verify(documentUnitStatusService)
        .setToPublishing(any(DocumentUnit.class), any(Instant.class), anyString());
  }

  @Test
  void testPublishByEmail_withoutDocumentUnitForUuid() {
    when(repository.findByUuid(TEST_UUID)).thenReturn(Optional.empty());

    StepVerifier.create(service.publishAsEmail(TEST_UUID, ISSUER_ADDRESS)).verifyError();
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
    when(publishService.getPublications(TEST_UUID)).thenReturn(Flux.just(xmlPublication));
    when(publicationReportRepository.getAllByDocumentUnitUuid(TEST_UUID))
        .thenReturn(Collections.emptyList());

    StepVerifier.create(service.getPublicationHistory(TEST_UUID))
        .consumeNextWith(
            actual -> assertThat(xmlPublication).usingRecursiveComparison().isEqualTo(actual))
        .verifyComplete();
    verify(publishService).getPublications(TEST_UUID);
  }

  @Test
  void testGetLastPublicationReport() {
    PublicationReport report =
        new PublicationReport("documentNumber", "<html></html>", Instant.now());
    when(publicationReportRepository.getAllByDocumentUnitUuid(TEST_UUID))
        .thenReturn(List.of(report));
    when(publishService.getPublications(TEST_UUID)).thenReturn(Flux.empty());

    StepVerifier.create(service.getPublicationHistory(TEST_UUID))
        .consumeNextWith(
            publications -> assertThat(publications).usingRecursiveComparison().isEqualTo(report))
        .verifyComplete();
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
    when(publishService.getPublications(TEST_UUID))
        .thenReturn(Flux.fromIterable(List.of(xml2, xml1)));

    StepVerifier.create(service.getPublicationHistory(TEST_UUID))
        .consumeNextWith(entry -> assertThat(entry).usingRecursiveComparison().isEqualTo(report1))
        .consumeNextWith(entry -> assertThat(entry).usingRecursiveComparison().isEqualTo(xml1))
        .consumeNextWith(entry -> assertThat(entry).usingRecursiveComparison().isEqualTo(report2))
        .consumeNextWith(entry -> assertThat(entry).usingRecursiveComparison().isEqualTo(xml2))
        .verifyComplete();
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
    assertThat(documentationUnitSearchEntries).contains(documentationUnitListItem);
    verify(repository)
        .searchByDocumentationUnitSearchInput(
            pageRequest, documentationOffice, documentationUnitSearchInput);
  }

  //  @Test
  //  void testCheckDocx_withValidDocument() {
  //    ByteBuffer byteBuffer = buildBuffer("word/document.xml");
  //    assertDoesNotThrow(() -> service.checkDocx(byteBuffer));
  //  }

  //  @Test
  //  void testCheckDocx_withInvalidFormat() {
  //    ByteBuffer byteBuffer = buildBuffer("word/document.csv");
  //    assertThrows(ResponseStatusException.class, () -> service.checkDocx(byteBuffer));
  //  }
  //
  //  @Test
  //  void testCheckDocx_withCorruptedDocx() {
  //    byte[] corruptedData = new byte[1024];
  //    new Random().nextBytes(corruptedData);
  //    ByteBuffer byteBuffer = ByteBuffer.wrap(corruptedData);
  //
  //    assertThrows(ResponseStatusException.class, () -> service.checkDocx(byteBuffer));
  //  }
  //
  //  @Test
  //  void testCheckDocx_withEmptyBuffer() {
  //    byte[] emptyData = new byte[] {};
  //    ByteBuffer byteBuffer = ByteBuffer.wrap(emptyData);
  //
  //    assertThrows(ResponseStatusException.class, () -> service.checkDocx(byteBuffer));
  //  }

  @Test
  void testPreviewPublication() {
    DocumentUnit testDocumentUnit = DocumentUnit.builder().build();
    XmlResultObject mockXmlResultObject =
        new XmlResultObject("some xml", "200", List.of("success"), "foo.xml", Instant.now());
    when(repository.findByUuid(TEST_UUID)).thenReturn(Optional.ofNullable(testDocumentUnit));
    when(publishService.getPublicationPreview(testDocumentUnit))
        .thenReturn(Mono.just(mockXmlResultObject));

    StepVerifier.create(service.previewPublication(TEST_UUID))
        .expectNext(mockXmlResultObject)
        .verifyComplete();
  }

  private CompletableFuture<DeleteObjectResponse> buildEmptyDeleteObjectResponse() {
    return CompletableFuture.completedFuture(DeleteObjectResponse.builder().build());
  }

  private ByteBuffer buildBuffer(String entry) {
    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
      ZipEntry zipEntry = new ZipEntry(entry);
      zipOutputStream.putNextEntry(zipEntry);
      zipOutputStream.closeEntry();
      zipOutputStream.finish();

      byte[] zipBytes = byteArrayOutputStream.toByteArray();
      return ByteBuffer.wrap(zipBytes);
    } catch (IOException exception) {
      throw new RuntimeException("Failed to create zip", exception);
    }
  }
}
