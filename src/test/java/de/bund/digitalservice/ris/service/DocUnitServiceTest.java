package de.bund.digitalservice.ris.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import de.bund.digitalservice.ris.datamodel.DocUnit;
import de.bund.digitalservice.ris.repository.DocUnitRepository;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.verification.VerificationMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@SpringBootTest(properties = { "otc.obs.bucket-name=testBucket" })
@Tag("test")
class DocUnitServiceTest {
  @Autowired private DocUnitService service;

  @MockBean private DocUnitRepository repository;

  @MockBean private S3AsyncClient s3AsyncClient;

  @Test
  public void testGenerateNewDocUnit() {
    // given
    var toSave = new DocUnit();
    toSave.setS3path("88888888-4444-4444-4444-121212121212");
    toSave.setFiletype("docx");

    var savedDocUnit = new DocUnit();
    savedDocUnit.setId(1);
    savedDocUnit.setS3path("88888888-4444-4444-4444-121212121212");
    savedDocUnit.setFiletype("docx");
    when(repository.save(any(DocUnit.class))).thenReturn(Mono.just(savedDocUnit));

    when(s3AsyncClient.putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class))).thenReturn(
        CompletableFuture.completedFuture(PutObjectResponse.builder().build()));

    var filePart = mock(FilePart.class);
    doReturn("filename").when(filePart).filename();
    doReturn(Flux.empty()).when(filePart).content();

    var testUuid = UUID.fromString("88888888-4444-4444-4444-121212121212");
    try (MockedStatic<UUID> mockedUUIDStatic = mockStatic(UUID.class)) {
      mockedUUIDStatic.when(UUID::randomUUID).thenReturn(testUuid);

      // when and then
      StepVerifier.create(service.generateNewDocUnit(Mono.just(filePart)))
          .consumeNextWith(
              docUnit -> {
                assertNotNull(docUnit);
                assertEquals(ResponseEntity.status(HttpStatus.CREATED).body(savedDocUnit), docUnit);
              })
          .verifyComplete();

      verify(s3AsyncClient).putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class));
      verify(repository).save(eq(toSave));
    }
  }

  @Test
  public void testGenerateNewDocUnit_withExceptionFromBucket() {
    // given
    when(s3AsyncClient.putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class)))
        .thenThrow(SdkException.create("exception", null));

    var filePart = mock(FilePart.class);
    doReturn("filename").when(filePart).filename();
    doReturn(Flux.empty()).when(filePart).content();

    var testUuid = UUID.fromString("88888888-4444-4444-4444-121212121212");
    try (MockedStatic<UUID> mockedUUIDStatic = mockStatic(UUID.class)) {
      mockedUUIDStatic.when(UUID::randomUUID).thenReturn(testUuid);

      // when and then
      StepVerifier.create(service.generateNewDocUnit(Mono.just(filePart)))
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
  }

  @Test
  public void testGenerateNewDocUnit_withExceptionFromRepository() {
    // given
    when(s3AsyncClient.putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class))).thenReturn(
        CompletableFuture.completedFuture(PutObjectResponse.builder().build()));
    doThrow(new IllegalArgumentException()).when(repository).save(any(DocUnit.class));

    var filePart = mock(FilePart.class);
    doReturn("filename").when(filePart).filename();
    doReturn(Flux.empty()).when(filePart).content();

    var testUuid = UUID.fromString("88888888-4444-4444-4444-121212121212");
    try (MockedStatic<UUID> mockedUUIDStatic = mockStatic(UUID.class)) {
      mockedUUIDStatic.when(UUID::randomUUID).thenReturn(testUuid);

      // when and then
      StepVerifier.create(service.generateNewDocUnit(Mono.just(filePart)))
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
  }

  @Test
  public void testGetAll() {
    StepVerifier.create(service.getAll())
        .consumeNextWith(Assertions::assertNotNull)
        .verifyComplete();

    verify(repository).findAll();
  }
}
