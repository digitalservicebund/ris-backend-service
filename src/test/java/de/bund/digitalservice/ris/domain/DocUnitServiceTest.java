package de.bund.digitalservice.ris.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@Import(DocUnitService.class)
@TestPropertySource(properties = "otc.obs.bucket-name:testBucket")
class DocUnitServiceTest {
  @Autowired private DocUnitService service;

  @MockBean private DocUnitRepository repository;

  @MockBean private DocumentNumberCounterRepository counterRepository;

  @MockBean private DocumentUnitPublishService publishService;

  @MockBean private S3AsyncClient s3AsyncClient;

  private final UUID testUuid = UUID.fromString("88888888-4444-4444-4444-121212121212");

  @Test
  void testGenerateNewDocUnit() {
    when(repository.save(any(DocUnit.class))).thenReturn(Mono.just(DocUnit.EMPTY));
    when(counterRepository.getDocumentNumberCounterEntry())
        .thenReturn(Mono.just(DocumentNumberCounter.buildInitial()));
    when(counterRepository.save(any(DocumentNumberCounter.class)))
        .thenReturn(Mono.just(DocumentNumberCounter.buildInitial()));
    // Can we use a captor to check if the document number was correctly created?
    // The chicken-egg-problem is, that we are dictating what happens when
    // repository.save(), so we can't just use a captor at the same time

    StepVerifier.create(service.generateNewDocUnit(DocUnitCreationInfo.EMPTY))
        .expectNextCount(1) // That it's a DocUnit is given by the generic type..
        .verifyComplete();
    verify(repository).save(any(DocUnit.class));
  }

  // @Test public void testGenerateNewDocUnit_withException() {}

  @Test
  void testAttachFileToDocUnit() {
    // given
    var byteBufferFlux = Flux.just(ByteBuffer.wrap(new byte[] {}));
    var headerMap = new LinkedMultiValueMap<String, String>();
    headerMap.put("Content-Type", List.of("content/type"));
    headerMap.put("X-Filename", List.of("testfile.docx"));
    var httpHeaders = HttpHeaders.readOnlyHttpHeaders(headerMap);

    var toSave = new DocUnit();
    toSave.setUuid(testUuid);
    toSave.setS3path(testUuid.toString());
    toSave.setFiletype("docx");
    toSave.setFilename("testfile.docx");

    var savedDocUnit = new DocUnit();
    savedDocUnit.setUuid(testUuid);
    savedDocUnit.setS3path(testUuid.toString());
    savedDocUnit.setFiletype("docx");
    when(repository.save(any(DocUnit.class))).thenReturn(Mono.just(savedDocUnit));
    when(repository.findByUuid(testUuid)).thenReturn(Mono.just(savedDocUnit));

    when(s3AsyncClient.putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class)))
        .thenReturn(CompletableFuture.completedFuture(PutObjectResponse.builder().build()));

    var putObjectRequestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
    var asyncRequestBodyCaptor = ArgumentCaptor.forClass(AsyncRequestBody.class);

    try (MockedStatic<UUID> mockedUUIDStatic = mockStatic(UUID.class)) {
      mockedUUIDStatic.when(UUID::randomUUID).thenReturn(testUuid);

      // when and then
      StepVerifier.create(service.attachFileToDocUnit(testUuid, byteBufferFlux, httpHeaders))
          .consumeNextWith(
              docUnit -> {
                assertNotNull(docUnit);
                assertEquals(ResponseEntity.status(HttpStatus.CREATED).body(savedDocUnit), docUnit);
              })
          .verifyComplete();

      verify(s3AsyncClient)
          .putObject(putObjectRequestCaptor.capture(), asyncRequestBodyCaptor.capture());
      assertEquals("testBucket", putObjectRequestCaptor.getValue().bucket());
      assertEquals(testUuid.toString(), putObjectRequestCaptor.getValue().key());
      assertEquals("content/type", putObjectRequestCaptor.getValue().contentType());
      StepVerifier.create(asyncRequestBodyCaptor.getValue())
          .expectNext(ByteBuffer.wrap(new byte[] {}))
          .verifyComplete();
      toSave.setFileuploadtimestamp(savedDocUnit.getFileuploadtimestamp());
      verify(repository).save(toSave);
    }
  }

  @Test
  void testRemoveFileFromDocUnit() {
    var docUnitBefore = new DocUnit();
    docUnitBefore.setUuid(testUuid);
    docUnitBefore.setS3path(testUuid.toString());
    docUnitBefore.setFilename("testfile.docx");

    var docUnitAfter = new DocUnit();
    docUnitAfter.setUuid(testUuid);

    when(repository.findByUuid(testUuid)).thenReturn(Mono.just(docUnitBefore));
    // is the thenReturn ok? Or am I bypassing the actual functionality-test?
    when(repository.save(any(DocUnit.class))).thenReturn(Mono.just(docUnitAfter));
    when(s3AsyncClient.deleteObject(any(DeleteObjectRequest.class)))
        .thenReturn(buildEmptyDeleteObjectResponse());

    StepVerifier.create(service.removeFileFromDocUnit(testUuid))
        .consumeNextWith(
            docUnitResponseEntity -> {
              assertNotNull(docUnitResponseEntity);
              assertEquals(HttpStatus.OK, docUnitResponseEntity.getStatusCode());
              assertEquals(docUnitAfter, docUnitResponseEntity.getBody());
            })
        .verifyComplete();

    ArgumentCaptor<DocUnit> docUnitCaptor = ArgumentCaptor.forClass(DocUnit.class);
    verify(repository).save(docUnitCaptor.capture());
    assertEquals(docUnitCaptor.getValue(), docUnitAfter);
  }

  @Test
  void testGenerateNewDocUnitAndAttachFile_withExceptionFromBucket() throws S3Exception {
    // given
    var byteBufferFlux = Flux.just(ByteBuffer.wrap(new byte[] {}));

    when(s3AsyncClient.putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class)))
        .thenThrow(SdkException.create("exception", null));

    // when and then
    StepVerifier.create(service.attachFileToDocUnit(testUuid, byteBufferFlux, HttpHeaders.EMPTY))
        .consumeNextWith(
            responseEntity -> {
              assertNotNull(responseEntity);
              assertNotNull(responseEntity.getBody());
              assertEquals(DocUnit.EMPTY, responseEntity.getBody());
              assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
            })
        .verifyComplete();

    verify(s3AsyncClient).putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class));
    verify(repository, times(0)).save(any(DocUnit.class));
  }

  @Test
  void testGenerateNewDocUnitAndAttachFile_withExceptionFromRepository() {
    // given
    var byteBufferFlux = Flux.just(ByteBuffer.wrap(new byte[] {}));

    when(s3AsyncClient.putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class)))
        .thenReturn(CompletableFuture.completedFuture(PutObjectResponse.builder().build()));
    doThrow(new IllegalArgumentException()).when(repository).save(any(DocUnit.class));
    when(repository.findByUuid(testUuid)).thenReturn(Mono.just(DocUnit.EMPTY));

    // when and then
    StepVerifier.create(service.attachFileToDocUnit(testUuid, byteBufferFlux, HttpHeaders.EMPTY))
        .consumeNextWith(
            responseEntity -> {
              assertNotNull(responseEntity);
              assertNotNull(responseEntity.getBody());
              assertEquals(DocUnit.EMPTY, responseEntity.getBody());
              assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
            })
        .verifyComplete();

    verify(s3AsyncClient).putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class));
    verify(repository).save(any(DocUnit.class));
  }

  @Test
  void testGetAll() {
    StepVerifier.create(service.getAll())
        .consumeNextWith(Assertions::assertNotNull)
        .verifyComplete();

    verify(repository).findAll(Sort.by(Order.desc("documentnumber")));
  }

  @Test
  void testGetByDocumentnumber() {
    when(repository.findByDocumentnumber("ABCDE2022000001")).thenReturn(Mono.just(DocUnit.EMPTY));
    StepVerifier.create(service.getByDocumentnumber("ABCDE2022000001"))
        .consumeNextWith(
            monoResponse -> assertEquals(monoResponse.getBody().getClass(), DocUnit.class))
        .verifyComplete();
    verify(repository).findByDocumentnumber("ABCDE2022000001");
  }

  @Test
  void testDeleteByUuid_withoutFileAttached() {
    // I think I shouldn't have to insert a specific DocUnit object here?
    // But if I don't, the test by itself succeeds, but fails if all tests in this class run
    // something flaky with the repository mock? Investigate this later
    DocUnit docUnit = new DocUnit();
    docUnit.setUuid(testUuid);
    // can we also test that the fileUuid from the DocUnit is used? with a captor somehow?
    when(repository.findByUuid(testUuid)).thenReturn(Mono.just(docUnit));
    when(repository.delete(any(DocUnit.class))).thenReturn(Mono.just(mock(Void.class)));

    StepVerifier.create(service.deleteByUuid(testUuid))
        .consumeNextWith(
            stringResponseEntity -> {
              System.out.println(stringResponseEntity);
              assertNotNull(stringResponseEntity);
              assertEquals(HttpStatus.OK, stringResponseEntity.getStatusCode());
              assertEquals("done", stringResponseEntity.getBody());
            })
        .verifyComplete();

    verify(s3AsyncClient, times(0)).deleteObject(any(DeleteObjectRequest.class));
  }

  @Test
  void testDeleteByUuid_withFileAttached() {
    DocUnit docUnit = new DocUnit();
    docUnit.setUuid(testUuid);
    docUnit.setS3path(testUuid.toString());
    when(repository.findByUuid(testUuid)).thenReturn(Mono.just(docUnit));
    when(repository.delete(any(DocUnit.class))).thenReturn(Mono.just(mock(Void.class)));
    when(s3AsyncClient.deleteObject(any(DeleteObjectRequest.class)))
        .thenReturn(buildEmptyDeleteObjectResponse());

    StepVerifier.create(service.deleteByUuid(testUuid))
        .consumeNextWith(
            stringResponseEntity -> {
              assertNotNull(stringResponseEntity);
              assertEquals(HttpStatus.OK, stringResponseEntity.getStatusCode());
              assertEquals("done", stringResponseEntity.getBody());
            })
        .verifyComplete();

    verify(s3AsyncClient, times(1)).deleteObject(any(DeleteObjectRequest.class));
  }

  @Test
  void testDeleteByUuid_withoutFileAttached_withExceptionFromBucket() {
    when(repository.findByUuid(testUuid)).thenReturn(Mono.just(DocUnit.EMPTY));
    when(s3AsyncClient.deleteObject(any(DeleteObjectRequest.class)))
        .thenThrow(SdkException.create("exception", null));

    StepVerifier.create(service.deleteByUuid(testUuid))
        .consumeNextWith(
            stringResponseEntity -> {
              assertNotNull(stringResponseEntity);
              assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, stringResponseEntity.getStatusCode());
              assertEquals("Couldn't delete the DocUnit", stringResponseEntity.getBody());
            })
        .verifyComplete();
  }

  @Test
  void testDeleteByUuid_withoutFileAttached_withExceptionFromRepository() {
    when(repository.findByUuid(testUuid)).thenReturn(Mono.just(DocUnit.EMPTY));
    doThrow(new IllegalArgumentException()).when(repository).delete(DocUnit.EMPTY);

    StepVerifier.create(service.deleteByUuid(testUuid))
        .consumeNextWith(
            stringResponseEntity -> {
              assertNotNull(stringResponseEntity);
              assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, stringResponseEntity.getStatusCode());
              assertEquals("Couldn't delete the DocUnit", stringResponseEntity.getBody());
            })
        .verifyComplete();
  }

  @Test
  void testUpdateDocUnit() {
    var docUnit = DocUnit.EMPTY;
    when(repository.save(docUnit)).thenReturn(Mono.just(docUnit));
    StepVerifier.create(service.updateDocUnit(docUnit))
        .consumeNextWith(monoResponse -> assertEquals(monoResponse.getBody(), docUnit))
        .verifyComplete();
    verify(repository).save(docUnit);
  }

  @Test
  void testPublish() {
    var documentUnit = DocUnit.EMPTY;
    when(repository.findByUuid(testUuid)).thenReturn(Mono.just(documentUnit));
    var xmlMail = new XmlMail(1L, 123L, "mailSubject", "xml");
    doReturn(Mono.just(xmlMail)).when(publishService).publish(documentUnit);

    StepVerifier.create(service.publish(testUuid))
        .consumeNextWith(exportObject -> assertThat(exportObject).isEqualTo(xmlMail))
        .verifyComplete();

    verify(publishService).publish(documentUnit);
  }

  @Test
  void testPublish_withExceptionFromPublishService() {
    var documentUnit = DocUnit.EMPTY;
    when(repository.findByUuid(testUuid)).thenReturn(Mono.just(documentUnit));
    var xmlMail = new XmlMail(1L, 123L, "mailSubject", "xml");
    doThrow(DocumentUnitPublishException.class).when(publishService).publish(documentUnit);

    StepVerifier.create(service.publish(testUuid))
        .expectError(DocumentUnitPublishException.class)
        .verify();

    verify(publishService).publish(documentUnit);
  }

  private CompletableFuture<DeleteObjectResponse> buildEmptyDeleteObjectResponse() {
    return CompletableFuture.completedFuture(DeleteObjectResponse.builder().build());
  }
}
