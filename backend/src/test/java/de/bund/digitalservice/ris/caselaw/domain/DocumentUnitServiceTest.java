package de.bund.digitalservice.ris.caselaw.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentationOfficeDTO;
import jakarta.validation.Validator;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

@ExtendWith(SpringExtension.class)
@Import({DocumentUnitService.class, DatabaseDocumentUnitStatusService.class})
@TestPropertySource(properties = "otc.obs.bucket-name:testBucket")
class DocumentUnitServiceTest {
  private static final UUID TEST_UUID = UUID.fromString("88888888-4444-4444-4444-121212121212");
  private static final String ISSUER_ADDRESS = "test-issuer@exporter.neuris";
  @SpyBean private DocumentUnitService service;

  @MockBean private DocumentUnitRepository repository;

  @MockBean private DatabaseDocumentationOfficeRepository documentationOfficeRepository;

  @MockBean private DocumentNumberService documentNumberService;

  @MockBean private S3AsyncClient s3AsyncClient;

  @MockBean private EmailPublishService publishService;

  @MockBean private PublicationReportRepository publicationReportRepository;

  @MockBean private DatabaseDocumentUnitStatusService documentUnitStatusService;

  @MockBean private Validator validator;

  @Test
  void testGenerateNewDocumentUnit() {
    DocumentationOffice documentationOffice = DocumentationOffice.builder().build();
    DocumentUnit documentUnit = DocumentUnit.builder().build();

    when(repository.createNewDocumentUnit("nextDocumentNumber", documentationOffice))
        .thenReturn(Mono.just(documentUnit));
    when(documentNumberService.generateNextDocumentNumber(documentationOffice))
        .thenReturn(Mono.just("nextDocumentNumber"));
    when(documentUnitStatusService.setInitialStatus(documentUnit))
        .thenReturn(Mono.just(documentUnit));
    // Can we use a captor to check if the document number was correctly created?
    // The chicken-egg-problem is, that we are dictating what happens when
    // repository.save(), so we can't just use a captor at the same time

    StepVerifier.create(service.generateNewDocumentUnit(documentationOffice))
        .expectNextCount(1) // That it's a DocumentUnit is given by the generic type..
        .verifyComplete();
    verify(documentNumberService).generateNextDocumentNumber(documentationOffice);
    verify(repository).createNewDocumentUnit("nextDocumentNumber", documentationOffice);
  }

  // @Test public void testGenerateNewDocumentUnit_withException() {}

  @Test
  void testAttachFileToDocumentUnit() {
    // given
    var byteBuffer = ByteBuffer.wrap(new byte[] {});
    var headerMap = new LinkedMultiValueMap<String, String>();
    headerMap.put("Content-Type", List.of("content/type"));
    headerMap.put("X-Filename", List.of("testfile.docx"));
    var httpHeaders = HttpHeaders.readOnlyHttpHeaders(headerMap);

    var savedDocumentUnit =
        DocumentUnit.builder()
            .uuid(TEST_UUID)
            .s3path(TEST_UUID.toString())
            .filetype("docx")
            .build();
    when(repository.attachFile(TEST_UUID, TEST_UUID.toString(), "docx", "testfile.docx"))
        .thenReturn(Mono.just(savedDocumentUnit));
    when(repository.findByUuid(TEST_UUID)).thenReturn(Mono.just(savedDocumentUnit));

    doNothing().when(service).checkDocx(any(ByteBuffer.class));
    when(s3AsyncClient.putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class)))
        .thenReturn(CompletableFuture.completedFuture(PutObjectResponse.builder().build()));

    var putObjectRequestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
    var asyncRequestBodyCaptor = ArgumentCaptor.forClass(AsyncRequestBody.class);

    try (MockedStatic<UUID> mockedUUIDStatic = mockStatic(UUID.class)) {
      mockedUUIDStatic.when(UUID::randomUUID).thenReturn(TEST_UUID);

      // when and then
      StepVerifier.create(service.attachFileToDocumentUnit(TEST_UUID, byteBuffer, httpHeaders))
          .consumeNextWith(
              documentUnit -> {
                assertNotNull(documentUnit);
                assertEquals(savedDocumentUnit, documentUnit);
              })
          .verifyComplete();

      verify(s3AsyncClient)
          .putObject(putObjectRequestCaptor.capture(), asyncRequestBodyCaptor.capture());
      assertEquals("testBucket", putObjectRequestCaptor.getValue().bucket());
      assertEquals(TEST_UUID.toString(), putObjectRequestCaptor.getValue().key());
      assertEquals("content/type", putObjectRequestCaptor.getValue().contentType());
      StepVerifier.create(asyncRequestBodyCaptor.getValue())
          .expectNext(ByteBuffer.wrap(new byte[] {}))
          .verifyComplete();
      verify(repository).attachFile(TEST_UUID, TEST_UUID.toString(), "docx", "testfile.docx");
    }
  }

  @Test
  void testRemoveFileFromDocumentUnit() {
    var documentUnitBefore =
        DocumentUnit.builder()
            .uuid(TEST_UUID)
            .s3path(TEST_UUID.toString())
            .filename("testfile.docx")
            .build();

    var documentUnitAfter = DocumentUnit.builder().uuid(TEST_UUID).build();

    when(repository.findByUuid(TEST_UUID)).thenReturn(Mono.just(documentUnitBefore));
    // is the thenReturn ok? Or am I bypassing the actual functionality-test?
    when(repository.removeFile(TEST_UUID)).thenReturn(Mono.just(documentUnitAfter));
    when(s3AsyncClient.deleteObject(any(DeleteObjectRequest.class)))
        .thenReturn(buildEmptyDeleteObjectResponse());

    StepVerifier.create(service.removeFileFromDocumentUnit(TEST_UUID))
        .consumeNextWith(
            documentUnit -> {
              assertNotNull(documentUnit);
              assertEquals(documentUnitAfter, documentUnit);
            })
        .verifyComplete();

    verify(repository).removeFile(TEST_UUID);
  }

  @Test
  void testGenerateNewDocumentUnitAndAttachFile_withExceptionFromBucket() throws S3Exception {
    // given
    var byteBuffer = ByteBuffer.wrap(new byte[] {});

    doNothing().when(service).checkDocx(any(ByteBuffer.class));
    when(s3AsyncClient.putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class)))
        .thenThrow(SdkException.create("exception", null));

    // when and then
    StepVerifier.create(service.attachFileToDocumentUnit(TEST_UUID, byteBuffer, HttpHeaders.EMPTY))
        .expectErrorMatches(ex -> ex instanceof SdkException)
        .verify();

    verify(s3AsyncClient).putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class));
    verify(repository, times(0)).save(any(DocumentUnit.class));
  }

  @Test
  void testGetAll() {
    PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Order.desc("creationtimestamp")));
    var docOffice = DocumentationOffice.builder().label("do1").build();

    UUID docOfficeUuid = UUID.randomUUID();
    List<DocumentUnitListEntry> entries =
        Arrays.asList(
            DocumentUnitListEntry.builder().documentationOffice(docOffice).build(),
            DocumentUnitListEntry.builder().documentationOffice(docOffice).build());
    when(documentationOfficeRepository.findByLabel("do1"))
        .thenReturn(
            Mono.just(DocumentationOfficeDTO.builder().label("do1").id(docOfficeUuid).build()));
    when(repository.findAll(pageRequest, docOffice)).thenReturn(Flux.fromIterable(entries));
    when(repository.countByDataSourceAndDocumentationOffice(DataSource.NEURIS, docOffice))
        .thenReturn(Mono.just((long) entries.size()));

    StepVerifier.create(service.getAll(pageRequest, docOffice))
        .assertNext(
            page -> {
              assertEquals(entries.size(), page.getNumberOfElements());
              assertTrue(entries.containsAll(page.getContent()));
            })
        .verifyComplete();

    verify(repository).findAll(pageRequest, docOffice);
  }

  @Test
  void testGetByDocumentnumber() {
    when(repository.findByDocumentNumber("ABCDE20220001"))
        .thenReturn(Mono.just(DocumentUnit.builder().build()));
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
    when(repository.countLinksByChildDocumentUnitUuid(TEST_UUID)).thenReturn(Mono.just(0L));
    when(repository.findByUuid(TEST_UUID)).thenReturn(Mono.just(documentUnit));
    when(repository.delete(any(DocumentUnit.class))).thenReturn(Mono.just(mock(Void.class)));

    StepVerifier.create(service.deleteByUuid(TEST_UUID))
        .consumeNextWith(
            string -> {
              assertNotNull(string);
              assertEquals("Dokumentationseinheit gelöscht: " + TEST_UUID, string);
            })
        .verifyComplete();

    verify(s3AsyncClient, times(0)).deleteObject(any(DeleteObjectRequest.class));
  }

  @Test
  void testDeleteByUuid_withFileAttached() {
    DocumentUnit documentUnit =
        DocumentUnit.builder().uuid(TEST_UUID).s3path(TEST_UUID.toString()).build();

    when(repository.countLinksByChildDocumentUnitUuid(TEST_UUID)).thenReturn(Mono.just(0L));
    when(repository.findByUuid(TEST_UUID)).thenReturn(Mono.just(documentUnit));
    when(repository.delete(any(DocumentUnit.class))).thenReturn(Mono.just(mock(Void.class)));
    when(s3AsyncClient.deleteObject(any(DeleteObjectRequest.class)))
        .thenReturn(buildEmptyDeleteObjectResponse());

    StepVerifier.create(service.deleteByUuid(TEST_UUID))
        .consumeNextWith(
            string -> {
              assertNotNull(string);
              assertEquals("Dokumentationseinheit gelöscht: " + TEST_UUID, string);
            })
        .verifyComplete();

    verify(s3AsyncClient, times(1)).deleteObject(any(DeleteObjectRequest.class));
  }

  @Test
  void testDeleteByUuid_withoutFileAttached_withExceptionFromBucket() {
    when(repository.countLinksByChildDocumentUnitUuid(TEST_UUID)).thenReturn(Mono.just(0L));
    when(repository.findByUuid(TEST_UUID)).thenReturn(Mono.just(DocumentUnit.builder().build()));
    when(s3AsyncClient.deleteObject(any(DeleteObjectRequest.class)))
        .thenThrow(SdkException.create("exception", null));

    StepVerifier.create(service.deleteByUuid(TEST_UUID)).expectError().verify();

    verify(repository).findByUuid(TEST_UUID);
  }

  @Test
  void testDeleteByUuid_withoutFileAttached_withExceptionFromRepository() {
    when(repository.countLinksByChildDocumentUnitUuid(TEST_UUID)).thenReturn(Mono.just(0L));
    when(repository.findByUuid(TEST_UUID)).thenReturn(Mono.just(DocumentUnit.builder().build()));
    doThrow(new IllegalArgumentException()).when(repository).delete(DocumentUnit.builder().build());

    StepVerifier.create(service.deleteByUuid(TEST_UUID)).expectError().verify();

    verify(repository).findByUuid(TEST_UUID);
  }

  @Test
  void testUpdateDocumentUnit() {
    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(UUID.randomUUID())
            .documentNumber("ABCDE20220001")
            .creationtimestamp(Instant.now())
            .fileuploadtimestamp(Instant.now())
            .proceedingDecisions(null)
            .build();
    DocumentationOffice documentationOffice = mock(DocumentationOffice.class);
    when(repository.save(documentUnit)).thenReturn(Mono.just(documentUnit));
    when(repository.findAllLinkedDocumentUnitsByParentDocumentUnitUuidAndType(
            any(UUID.class), eq(DocumentationUnitLinkType.ACTIVE_CITATION)))
        .thenReturn(Flux.empty());

    StepVerifier.create(service.updateDocumentUnit(documentUnit, documentationOffice))
        .consumeNextWith(du -> assertEquals(du, documentUnit))
        .verifyComplete();

    verify(repository).save(documentUnit);
  }

  @Test
  void testPublishByEmail() {
    when(repository.findByUuid(TEST_UUID)).thenReturn(Mono.just(DocumentUnit.builder().build()));
    XmlMail xmlMail =
        new XmlMail(
            TEST_UUID,
            "receiver address",
            "subject",
            "xml",
            "200",
            List.of("status messages"),
            "filename",
            Instant.now(),
            PublishState.UNKNOWN);
    when(publishService.publish(eq(DocumentUnit.builder().build()), anyString()))
        .thenReturn(Mono.just(new XmlMailResponse(TEST_UUID, xmlMail)));
    when(documentUnitStatusService.updateStatus(
            any(DocumentUnit.class),
            any(DocumentUnitStatus.class),
            any(Instant.class),
            anyString()))
        .thenReturn(Mono.just(DocumentUnit.builder().build()));
    StepVerifier.create(service.publishAsEmail(TEST_UUID, ISSUER_ADDRESS))
        .consumeNextWith(
            mailResponse ->
                assertThat(mailResponse)
                    .usingRecursiveComparison()
                    .isEqualTo(new XmlMailResponse(TEST_UUID, xmlMail)))
        .verifyComplete();
    verify(repository).findByUuid(TEST_UUID);
    verify(publishService).publish(eq(DocumentUnit.builder().build()), anyString());
    verify(documentUnitStatusService)
        .updateStatus(
            any(DocumentUnit.class),
            any(DocumentUnitStatus.class),
            any(Instant.class),
            anyString());
  }

  @Test
  void testPublishByEmail_withoutDocumentUnitForUuid() {
    when(repository.findByUuid(TEST_UUID)).thenReturn(Mono.empty());

    StepVerifier.create(service.publishAsEmail(TEST_UUID, ISSUER_ADDRESS)).verifyComplete();
    verify(repository).findByUuid(TEST_UUID);
    verify(publishService, never()).publish(eq(DocumentUnit.builder().build()), anyString());
  }

  @Test
  void testGetLastPublishedXmlMail() {
    XmlMail xmlMail =
        new XmlMail(
            TEST_UUID,
            "receiver address",
            "subject",
            "xml",
            "200",
            List.of("message"),
            "filename",
            null,
            PublishState.UNKNOWN);
    when(publishService.getPublicationMails(TEST_UUID))
        .thenReturn(Flux.just(new XmlMailResponse(TEST_UUID, xmlMail)));
    when(publicationReportRepository.getAllForDocumentUnit(TEST_UUID)).thenReturn(Flux.empty());

    StepVerifier.create(service.getPublicationLog(TEST_UUID))
        .consumeNextWith(
            xmlMailResponse ->
                assertThat(xmlMailResponse)
                    .usingRecursiveComparison()
                    .isEqualTo(new XmlMailResponse(TEST_UUID, xmlMail)))
        .verifyComplete();
    verify(publishService).getPublicationMails(TEST_UUID);
  }

  @Test
  void testGetLastPublicationReport() {
    PublicationReport report =
        new PublicationReport("documentNumber", "<html></html>", Instant.now());
    when(publicationReportRepository.getAllForDocumentUnit(TEST_UUID))
        .thenReturn(Flux.just(report));
    when(publishService.getPublicationMails(TEST_UUID)).thenReturn(Flux.empty());

    StepVerifier.create(service.getPublicationLog(TEST_UUID))
        .consumeNextWith(
            publications -> assertThat(publications).usingRecursiveComparison().isEqualTo(report))
        .verifyComplete();
    verify(publishService).getPublicationMails(TEST_UUID);
  }

  @Test
  void testGetSortedPublicationLog() {
    Instant newest = Instant.now();
    Instant secondNewest = newest.minusSeconds(61);
    Instant thirdNewest = secondNewest.minusSeconds(61);
    Instant fourthNewest = thirdNewest.minusSeconds(61);

    PublicationReport report1 = new PublicationReport("documentNumber", "<html></html>", newest);

    XmlMailResponse xml1 =
        new XmlMailResponse(
            TEST_UUID,
            new XmlMail(
                TEST_UUID,
                "receiver address",
                "subject",
                "xml",
                "200",
                List.of("message"),
                "filename",
                secondNewest,
                PublishState.UNKNOWN));

    PublicationReport report2 =
        new PublicationReport("documentNumber", "<html></html>", thirdNewest);

    XmlMailResponse xml2 =
        new XmlMailResponse(
            TEST_UUID,
            new XmlMail(
                TEST_UUID,
                "receiver address",
                "subject",
                "xml",
                "200",
                List.of("message"),
                "filename",
                fourthNewest,
                PublishState.UNKNOWN));

    when(publicationReportRepository.getAllForDocumentUnit(TEST_UUID))
        .thenReturn(Flux.fromIterable(List.of(report2, report1)));
    when(publishService.getPublicationMails(TEST_UUID))
        .thenReturn(Flux.fromIterable(List.of(xml2, xml1)));

    StepVerifier.create(service.getPublicationLog(TEST_UUID))
        .consumeNextWith(entry -> assertThat(entry).usingRecursiveComparison().isEqualTo(report1))
        .consumeNextWith(entry -> assertThat(entry).usingRecursiveComparison().isEqualTo(xml1))
        .consumeNextWith(entry -> assertThat(entry).usingRecursiveComparison().isEqualTo(report2))
        .consumeNextWith(entry -> assertThat(entry).usingRecursiveComparison().isEqualTo(xml2))
        .verifyComplete();
    verify(publishService).getPublicationMails(TEST_UUID);
  }

  @Test
  void testSearchByProceedingDecision() {
    ProceedingDecision proceedingDecision = ProceedingDecision.builder().build();
    PageRequest pageRequest = PageRequest.of(0, 10);

    when(repository.searchByLinkedDocumentationUnit(proceedingDecision, pageRequest))
        .thenReturn(Flux.just(proceedingDecision));
    when(repository.countByLinkedDocumentationUnit(proceedingDecision)).thenReturn(Mono.just(1L));

    StepVerifier.create(service.searchByLinkedDocumentationUnit(proceedingDecision, pageRequest))
        .consumeNextWith(pd -> assertEquals(pd.getContent().get(0), proceedingDecision))
        .verifyComplete();
    verify(repository).searchByLinkedDocumentationUnit(proceedingDecision, pageRequest);
  }

  private CompletableFuture<DeleteObjectResponse> buildEmptyDeleteObjectResponse() {
    return CompletableFuture.completedFuture(DeleteObjectResponse.builder().build());
  }
}
