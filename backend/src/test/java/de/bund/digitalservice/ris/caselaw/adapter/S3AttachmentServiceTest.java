package de.bund.digitalservice.ris.caselaw.adapter;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentInlineDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseAttachmentInlineRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentException;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentType;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitHistoryLogService;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLogEventType;
import de.bund.digitalservice.ris.caselaw.domain.User;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

@TestPropertySource(properties = "otc.obs.bucket-name:testBucket")
@ExtendWith(SpringExtension.class)
@Import({S3AttachmentService.class})
class S3AttachmentServiceTest {
  @MockitoSpyBean S3AttachmentService service;

  @MockitoBean AttachmentRepository repository;
  @MockitoBean DatabaseAttachmentInlineRepository attachmentInlineRepository;
  @MockitoBean private DocumentationUnitHistoryLogService historyLogService;

  @MockitoBean
  @Qualifier("docxS3Client")
  S3Client s3Client;

  @MockitoBean S3Client streamS3Client;

  @MockitoBean DatabaseDocumentationUnitRepository documentationUnitRepository;

  private DocumentationUnitDTO documentationUnitDTO;

  private static final String DOCX_MEDIA_TYPE =
      "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

  @BeforeEach
  void setup() {
    documentationUnitDTO = DecisionDTO.builder().id(UUID.randomUUID()).build();
    when(documentationUnitRepository.findById(documentationUnitDTO.getId()))
        .thenReturn(Optional.of(documentationUnitDTO));

    when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
        .thenReturn(PutObjectResponse.builder().build());

    when(repository.save(any(AttachmentDTO.class)))
        .thenAnswer(
            invocation -> {
              AttachmentDTO unsavedAttachmentDTO = invocation.getArgument(0);
              unsavedAttachmentDTO.setId(UUID.randomUUID());
              return unsavedAttachmentDTO;
            });
    when(attachmentInlineRepository.save(any(AttachmentInlineDTO.class)))
        .thenAnswer(
            invocation -> {
              AttachmentInlineDTO unsavedAttachmentDTO = invocation.getArgument(0);
              unsavedAttachmentDTO.setId(UUID.randomUUID());
              return unsavedAttachmentDTO;
            });
  }

  @Nested
  class DocxTest {
    @Test
    void testAttachFileToDocumentationUnit() {
      var byteBuffer = ByteBuffer.wrap(new byte[] {});
      var headerMap = new LinkedMultiValueMap<String, String>();
      headerMap.put("Content-Type", List.of(DOCX_MEDIA_TYPE));
      headerMap.put("X-Filename", List.of("testfile.docx"));
      var httpHeaders = HttpHeaders.readOnlyHttpHeaders(headerMap);
      doNothing().when(service).checkDocx(any(ByteBuffer.class));

      service.attachFileToDocumentationUnit(
          documentationUnitDTO.getId(), byteBuffer, httpHeaders, User.builder().build());

      // s3 interaction
      var putObjectRequestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
      var requestBodyCaptor = ArgumentCaptor.forClass(RequestBody.class);
      verify(s3Client).putObject(putObjectRequestCaptor.capture(), requestBodyCaptor.capture());
      assertEquals("testBucket", putObjectRequestCaptor.getValue().bucket());
      assertEquals(DOCX_MEDIA_TYPE, putObjectRequestCaptor.getValue().contentType());
      var value = requestBodyCaptor.getValue();
      var expected = RequestBody.fromByteBuffer(ByteBuffer.wrap(new byte[] {}));
      assertEquals(
          expected.optionalContentLength().orElse(0L), value.optionalContentLength().orElse(0L));
      assertEquals(expected.contentType(), value.contentType());

      // repo interaction
      var attachmentDtoCaptor = ArgumentCaptor.forClass(AttachmentDTO.class);
      verify(repository, times(2)).save(attachmentDtoCaptor.capture());
      assertEquals("testfile.docx", attachmentDtoCaptor.getValue().getFilename());
      assertEquals("docx", attachmentDtoCaptor.getValue().getFormat());
    }

    @Test
    void testCheckDocx_withValidDocument() {
      ByteBuffer byteBuffer = buildBuffer("word/document.xml");
      assertDoesNotThrow(() -> service.checkDocx(byteBuffer));
    }

    @Test
    void testCheckDocx_withCorruptedDocx() {
      byte[] corruptedData = new byte[1024];
      new Random().nextBytes(corruptedData);
      ByteBuffer byteBuffer = ByteBuffer.wrap(corruptedData);

      assertThrows(ResponseStatusException.class, () -> service.checkDocx(byteBuffer));
    }

    @Test
    void testCheckDocx_withEmptyBuffer() {
      byte[] emptyData = new byte[] {};
      ByteBuffer byteBuffer = ByteBuffer.wrap(emptyData);

      assertThrows(ResponseStatusException.class, () -> service.checkDocx(byteBuffer));
    }
  }

  @Test
  void testAttachFileToDocumentationUnit_withoutFileName() {
    var byteBuffer = ByteBuffer.wrap(new byte[] {});
    var headerMap = new LinkedMultiValueMap<String, String>();
    headerMap.put("Content-Type", List.of(DOCX_MEDIA_TYPE));
    var httpHeaders = HttpHeaders.readOnlyHttpHeaders(headerMap);
    doNothing().when(service).checkDocx(any(ByteBuffer.class));

    service.attachFileToDocumentationUnit(
        documentationUnitDTO.getId(), byteBuffer, httpHeaders, User.builder().build());

    var attachmentDtoCaptor = ArgumentCaptor.forClass(AttachmentDTO.class);
    verify(repository, times(2)).save(attachmentDtoCaptor.capture());
    assertEquals("Kein Dateiname gefunden", attachmentDtoCaptor.getValue().getFilename());
  }

  @Test
  void testDeleteByS3Path() {
    var testS3Path = UUID.randomUUID().toString();
    service.deleteByS3Path(testS3Path, UUID.randomUUID(), User.builder().build());

    // bucket interaction
    var deleteObjectRequestCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
    verify(s3Client).deleteObject(deleteObjectRequestCaptor.capture());
    assertEquals("testBucket", deleteObjectRequestCaptor.getValue().bucket());
    assertEquals(testS3Path, deleteObjectRequestCaptor.getValue().key());

    // repo interaction
    verify(repository).deleteByS3ObjectPath(testS3Path);
  }

  @ParameterizedTest()
  @ValueSource(strings = {"", " "})
  @NullSource
  void testDeleteByS3Path_withoutS3Path(String s3Path) {
    var user = User.builder().build();
    var docUnitId = UUID.randomUUID();
    assertThrows(AttachmentException.class, () -> service.deleteByS3Path(s3Path, docUnitId, user));

    verifyNoInteractions(s3Client);
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
    verify(s3Client, times(2)).deleteObject(deleteObjectRequestCaptor.capture());
    List<DeleteObjectRequest> capturedRequests = deleteObjectRequestCaptor.getAllValues();
    assertTrue(capturedRequests.stream().anyMatch(request -> request.key().equals("fooS3Path")));
    assertTrue(capturedRequests.stream().anyMatch(request -> request.key().equals("barS3Path")));
  }

  @Test
  void testCheckDocx_withInvalidFormat() {
    ByteBuffer byteBuffer = buildBuffer("word/document.csv");
    assertThrows(ResponseStatusException.class, () -> service.checkDocx(byteBuffer));
  }

  @Test
  void testGenerateNewDocumentationUnitAndAttachFile_withExceptionFromBucket() throws S3Exception {
    var byteBuffer = ByteBuffer.wrap(new byte[] {});
    var headerMap = new LinkedMultiValueMap<String, String>();
    headerMap.put("Content-Type", List.of(DOCX_MEDIA_TYPE));

    var headers = HttpHeaders.readOnlyHttpHeaders(headerMap);
    var user = User.builder().build();
    var documentationUnitDTOId = documentationUnitDTO.getId();

    doNothing().when(service).checkDocx(any(ByteBuffer.class));
    when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
        .thenThrow(SdkException.create("exception", null));

    assertThrows(
        SdkException.class,
        () ->
            service.attachFileToDocumentationUnit(
                documentationUnitDTOId, byteBuffer, headers, user));

    verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  @Nested
  class ImageTest {

    @Test
    void testAttachImageToDocumentationUnit_withUnsupportedType() {
      var byteBuffer = ByteBuffer.wrap(new byte[] {0x01});
      var headers = new HttpHeaders();
      var user = User.builder().build();
      headers.setContentType(MediaType.APPLICATION_JSON);
      var documentationUnitDTOId = documentationUnitDTO.getId();

      ResponseStatusException exception =
          assertThrows(
              ResponseStatusException.class,
              () ->
                  service.attachFileToDocumentationUnit(
                      documentationUnitDTOId, byteBuffer, headers, user));

      assertEquals("Only images and docx are supported", exception.getReason());

      verify(repository, never()).save(any());
    }

    @Test
    void testAttachImageToDocumentationUnit_withSupportedType() {
      byte[] imageBytes = new byte[] {1, 2, 3, 4}; // Mock image data
      ByteBuffer byteBuffer = ByteBuffer.wrap(imageBytes);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.IMAGE_PNG);
      headers.set("X-Filename", "test-image.png");

      UUID id = UUID.randomUUID();
      AttachmentInlineDTO savedDto =
          AttachmentInlineDTO.builder()
              .id(id)
              .content(imageBytes)
              .documentationUnit(documentationUnitDTO)
              .format("png")
              .filename("test-image.png")
              .build();

      when(attachmentInlineRepository.save(any(AttachmentInlineDTO.class))).thenReturn(savedDto);

      // Run
      var result =
          service.attachFileToDocumentationUnit(
              documentationUnitDTO.getId(), byteBuffer, headers, User.builder().build());

      verify(attachmentInlineRepository, times(2)).save(any()); // initial + filename update

      assertEquals("png", result.format());
      assertEquals(id + ".png", result.name());
    }
  }

  @Test
  void testStreamUploadToS3_multipartIsUsed_andDomainIsPersisted() throws Exception {
    // given
    byte[] data = new byte[6 * 1024 * 1024];
    var in = new java.io.ByteArrayInputStream(data);
    var user = User.builder().build();
    var filename = "test.zip";

    when(s3Client.createMultipartUpload(any(CreateMultipartUploadRequest.class)))
        .thenReturn(CreateMultipartUploadResponse.builder().uploadId("upload-123").build());
    when(s3Client.createMultipartUpload(any(Consumer.class)))
        .thenReturn(CreateMultipartUploadResponse.builder().uploadId("upload-123").build());
    when(s3Client.uploadPart(any(UploadPartRequest.class), any(RequestBody.class)))
        .thenReturn(UploadPartResponse.builder().eTag("etag").build());
    when(s3Client.completeMultipartUpload(any(CompleteMultipartUploadRequest.class)))
        .thenReturn(CompleteMultipartUploadResponse.builder().build());

    // when
    service.streamFileToDocumentationUnit(
        documentationUnitDTO.getId(), in, filename, user, AttachmentType.OTHER);

    // then
    verify(repository, times(2)).save(any());
    verify(s3Client, times(2)).uploadPart(any(UploadPartRequest.class), any(RequestBody.class));
    verify(s3Client).completeMultipartUpload(any(CompleteMultipartUploadRequest.class));
    verify(historyLogService)
        .saveHistoryLog(
            eq(documentationUnitDTO.getId()),
            any(User.class),
            eq(HistoryLogEventType.FILES),
            eq("File uploaded"));

    var uploadPartRequestCaptor = ArgumentCaptor.forClass(UploadPartRequest.class);
    var requestBodyCaptor = ArgumentCaptor.forClass(RequestBody.class);
    verify(s3Client, times(2))
        .uploadPart(uploadPartRequestCaptor.capture(), requestBodyCaptor.capture());
    List<UploadPartRequest> capturedUploadRequests = uploadPartRequestCaptor.getAllValues();
    List<RequestBody> capturedRequestBodies = requestBodyCaptor.getAllValues();
    assertEquals(2, capturedUploadRequests.size());
    assertEquals(2, capturedRequestBodies.size());
    var first = capturedUploadRequests.get(0);
    var second = capturedUploadRequests.get(1);
    assertEquals(1, first.partNumber());
    assertEquals(2, second.partNumber());
    assertEquals("upload-123", first.uploadId());
    assertEquals("upload-123", second.uploadId());
    assertEquals(5L * 1024 * 1024, first.contentLength().longValue());
    assertEquals((6L * 1024 * 1024) - (5L * 1024 * 1024), second.contentLength().longValue());
  }

  @Test
  void testGetFileStream_returnsStreamedFileResponse() throws Exception {
    UUID fileUuid = UUID.randomUUID();
    String s3Path = UUID.randomUUID().toString();

    when(repository.findById(fileUuid))
        .thenReturn(Optional.of(AttachmentDTO.builder().s3ObjectPath(s3Path).build()));

    byte[] data = "hello world".getBytes();

    var getObjectResponse =
        GetObjectResponse.builder()
            .contentType("application/octet-stream")
            .contentLength((long) data.length)
            .build();

    var responseInputStream =
        new ResponseInputStream<GetObjectResponse>(
            getObjectResponse, new ByteArrayInputStream(data));

    when(s3Client.getObject(any(GetObjectRequest.class), any(ResponseTransformer.class)))
        .thenReturn(responseInputStream);

    var streamed = service.getFileStream(documentationUnitDTO.getId(), fileUuid);

    assertEquals(getObjectResponse.contentType(), streamed.response().contentType());
    assertEquals(getObjectResponse.contentLength(), streamed.response().contentLength());

    var streamOutput = new java.io.ByteArrayOutputStream();
    streamed.body().writeTo(streamOutput);
    assertEquals(new String(data), streamOutput.toString());
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
