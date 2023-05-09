package de.bund.digitalservice.ris.caselaw.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
@Import(DocumentUnitService.class)
@TestPropertySource(properties = "otc.obs.bucket-name:testBucket")
class DocumentUnitServiceTest {
  private static final UUID TEST_UUID = UUID.fromString("88888888-4444-4444-4444-121212121212");
  private static final String RECEIVER_ADDRESS = "test@exporter.neuris";
  @SpyBean private DocumentUnitService service;

  @MockBean private DocumentUnitRepository repository;

  @MockBean private DocumentNumberService documentNumberService;

  @MockBean private S3AsyncClient s3AsyncClient;

  @MockBean private EmailPublishService publishService;

  @Test
  void testGenerateNewDocumentUnit() {
    when(repository.createNewDocumentUnit("nextDocumentNumber"))
        .thenReturn(Mono.just(DocumentUnit.builder().build()));
    when(documentNumberService.generateNextDocumentNumber(DocumentUnitCreationInfo.EMPTY))
        .thenReturn(Mono.just("nextDocumentNumber"));
    // Can we use a captor to check if the document number was correctly created?
    // The chicken-egg-problem is, that we are dictating what happens when
    // repository.save(), so we can't just use a captor at the same time

    StepVerifier.create(service.generateNewDocumentUnit(DocumentUnitCreationInfo.EMPTY))
        .expectNextCount(1) // That it's a DocumentUnit is given by the generic type..
        .verifyComplete();
    verify(documentNumberService).generateNextDocumentNumber(DocumentUnitCreationInfo.EMPTY);
    verify(repository).createNewDocumentUnit("nextDocumentNumber");
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

    var toSave =
        DocumentUnit.builder()
            .uuid(TEST_UUID)
            .s3path(TEST_UUID.toString())
            .filetype("docx")
            .filename("testfile.docx")
            .build();

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

    List<DocumentUnitListEntry> entries =
        Arrays.asList(
            DocumentUnitListEntry.builder().build(), DocumentUnitListEntry.builder().build());
    when(repository.findAll(pageRequest)).thenReturn(Flux.fromIterable(entries));
    when(repository.count(DataSource.NEURIS)).thenReturn(Mono.just((long) entries.size()));

    StepVerifier.create(service.getAll(pageRequest))
        .assertNext(
            page -> {
              assertEquals(entries.size(), page.getNumberOfElements());
              assertTrue(entries.containsAll(page.getContent()));
            })
        .verifyComplete();

    verify(repository).findAll(pageRequest);
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
    when(repository.save(documentUnit)).thenReturn(Mono.just(documentUnit));
    StepVerifier.create(service.updateDocumentUnit(documentUnit))
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
            null,
            PublishState.UNKNOWN);
    when(publishService.publish(DocumentUnit.builder().build(), RECEIVER_ADDRESS))
        .thenReturn(Mono.just(new XmlMailResponse(TEST_UUID, xmlMail)));
    StepVerifier.create(service.publishAsEmail(TEST_UUID, RECEIVER_ADDRESS))
        .consumeNextWith(
            mailResponse ->
                assertThat(mailResponse)
                    .usingRecursiveComparison()
                    .isEqualTo(new XmlMailResponse(TEST_UUID, xmlMail)))
        .verifyComplete();
    verify(repository).findByUuid(TEST_UUID);
    verify(publishService).publish(DocumentUnit.builder().build(), RECEIVER_ADDRESS);
  }

  @Test
  void testPublishByEmail_withoutDocumentUnitForUuid() {
    when(repository.findByUuid(TEST_UUID)).thenReturn(Mono.empty());

    StepVerifier.create(service.publishAsEmail(TEST_UUID, RECEIVER_ADDRESS)).verifyComplete();
    verify(repository).findByUuid(TEST_UUID);
    verify(publishService, never()).publish(DocumentUnit.builder().build(), RECEIVER_ADDRESS);
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
    when(publishService.getLastPublishedXml(TEST_UUID))
        .thenReturn(Mono.just(new XmlMailResponse(TEST_UUID, xmlMail)));

    StepVerifier.create(service.getLastPublishedXmlMail(TEST_UUID))
        .consumeNextWith(
            xmlMailResponse ->
                assertThat(xmlMailResponse)
                    .usingRecursiveComparison()
                    .isEqualTo(new XmlMailResponse(TEST_UUID, xmlMail)))
        .verifyComplete();
    verify(publishService).getLastPublishedXml(TEST_UUID);
  }

  @Test
  void testSearchForDocumentUnitsByProceedingDecisionInput() {
    ProceedingDecision proceedingDecision = ProceedingDecision.builder().build();

    when(repository.searchForDocumentUnitsByProceedingDecisionInput(proceedingDecision))
        .thenReturn(Flux.just(proceedingDecision));

    StepVerifier.create(service.searchForDocumentUnitsByProceedingDecisionInput(proceedingDecision))
        .consumeNextWith(pd -> assertEquals(pd, proceedingDecision))
        .verifyComplete();
    verify(repository).searchForDocumentUnitsByProceedingDecisionInput(proceedingDecision);
  }

  private CompletableFuture<DeleteObjectResponse> buildEmptyDeleteObjectResponse() {
    return CompletableFuture.completedFuture(DeleteObjectResponse.builder().build());
  }
}
