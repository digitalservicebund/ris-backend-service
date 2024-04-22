package de.bund.digitalservice.ris.caselaw.adapter;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;
import reactor.test.StepVerifier;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@TestPropertySource(properties = "otc.obs.bucket-name:testBucket")
@ExtendWith(SpringExtension.class)
@Import({S3AttachmentService.class})
class S3AttachmentServiceTest {
  @SpyBean S3AttachmentService service;

  @MockBean AttachmentRepository repository;
  @MockBean S3AsyncClient s3AsyncClient;
  @MockBean DatabaseDocumentationUnitRepository documentationUnitRepository;

  private DocumentationUnitDTO documentationUnitDTO;

  @BeforeEach
  void setup() {
    documentationUnitDTO = DocumentationUnitDTO.builder().id(UUID.randomUUID()).build();
    when(documentationUnitRepository.findById(documentationUnitDTO.getId()))
        .thenReturn(Optional.of(documentationUnitDTO));

    when(s3AsyncClient.putObject(any(PutObjectRequest.class), any(AsyncRequestBody.class)))
        .thenReturn(CompletableFuture.completedFuture(PutObjectResponse.builder().build()));

    when(repository.save(any(AttachmentDTO.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    doNothing().when(service).checkDocx(any(ByteBuffer.class));
  }

  @Test
  void testAttachFileToDocumentUnit() {
    var byteBuffer = ByteBuffer.wrap(new byte[] {});
    var headerMap = new LinkedMultiValueMap<String, String>();
    headerMap.put("Content-Type", List.of("content/extension"));
    headerMap.put("X-Filename", List.of("testfile.docx"));
    var httpHeaders = HttpHeaders.readOnlyHttpHeaders(headerMap);

    service.attachFileToDocumentationUnit(documentationUnitDTO.getId(), byteBuffer, httpHeaders);

    // s3 interaction
    var putObjectRequestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
    var asyncRequestBodyCaptor = ArgumentCaptor.forClass(AsyncRequestBody.class);
    verify(s3AsyncClient)
        .putObject(putObjectRequestCaptor.capture(), asyncRequestBodyCaptor.capture());
    assertEquals("testBucket", putObjectRequestCaptor.getValue().bucket());
    assertEquals("content/extension", putObjectRequestCaptor.getValue().contentType());
    StepVerifier.create(asyncRequestBodyCaptor.getValue())
        .expectNext(ByteBuffer.wrap(new byte[] {}))
        .verifyComplete();

    // repo interaction
    var attachmentDtoCaptor = ArgumentCaptor.forClass(AttachmentDTO.class);
    verify(repository).save(attachmentDtoCaptor.capture());
    assertEquals("testfile.docx", attachmentDtoCaptor.getValue().getFilename());
    assertEquals("docx", attachmentDtoCaptor.getValue().getFormat());
  }

  @Test
  void testAttachFileToDocumentUnit_withoutFileName() {
    var byteBuffer = ByteBuffer.wrap(new byte[] {});
    var headerMap = new LinkedMultiValueMap<String, String>();
    headerMap.put("Content-Type", List.of("content/extension"));
    var httpHeaders = HttpHeaders.readOnlyHttpHeaders(headerMap);

    service.attachFileToDocumentationUnit(documentationUnitDTO.getId(), byteBuffer, httpHeaders);

    var attachmentDtoCaptor = ArgumentCaptor.forClass(AttachmentDTO.class);
    verify(repository).save(attachmentDtoCaptor.capture());
    assertEquals("Kein Dateiname gefunden", attachmentDtoCaptor.getValue().getFilename());
  }

  @Test
  void testDeleteByS3Path() {
    var testS3Path = UUID.randomUUID().toString();
    service.deleteByS3Path(testS3Path);

    // bucket interaction
    var deleteObjectRequestCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
    verify(s3AsyncClient).deleteObject(deleteObjectRequestCaptor.capture());
    assertEquals("testBucket", deleteObjectRequestCaptor.getValue().bucket());
    assertEquals(testS3Path, deleteObjectRequestCaptor.getValue().key());

    // repo interaction
    verify(repository).deleteByS3ObjectPath(testS3Path);
  }

  @ParameterizedTest()
  @ValueSource(strings = {"", " "})
  @NullSource
  void testDeleteByS3Path_withoutS3Path(String s3Path) {
    assertThrows(AttachmentException.class, () -> service.deleteByS3Path(s3Path));

    verifyNoInteractions(s3AsyncClient);
    verifyNoInteractions(repository);
  }

  @Test
  void testDeleteAllObjectsFromBucketForDocumentationUnit() {
    when(repository.findAllByDocumentationUnitId(documentationUnitDTO.getId()))
        .thenReturn(
            List.of(
                AttachmentDTO.builder().s3ObjectPath("fooS3Path").build(),
                AttachmentDTO.builder().s3ObjectPath("barS3Path").build()));

    service.deleteAllObjectsFromBucketForDocumentationUnit(documentationUnitDTO.getId());

    // repo interaction
    verify(repository).findAllByDocumentationUnitId(documentationUnitDTO.getId());
    verify(repository, never()).deleteByS3ObjectPath(any(String.class));

    // bucket interaction
    var deleteObjectRequestCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
    verify(s3AsyncClient, times(2)).deleteObject(deleteObjectRequestCaptor.capture());
    List<DeleteObjectRequest> capturedRequests = deleteObjectRequestCaptor.getAllValues();
    assertTrue(capturedRequests.stream().anyMatch(request -> request.key().equals("fooS3Path")));
    assertTrue(capturedRequests.stream().anyMatch(request -> request.key().equals("barS3Path")));
  }
}
