package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import de.bund.digitalservice.ris.caselaw.TestMemoryAppender;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.slf4j.event.KeyValuePair;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CopyObjectResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

@ExtendWith(SpringExtension.class)
class S3RenamingServiceTest {

  @MockitoBean private AttachmentRepository attachmentRepository;
  @MockitoBean private S3Client s3Client;
  @MockitoBean private AttachmentService attachmentService;

  private S3RenamingService subject;

  @BeforeEach
  void setUp() {
    subject = new S3RenamingService(attachmentRepository, s3Client, attachmentService);
  }

  @Test
  void moveExistingFilesToNewPaths_shouldShouldOnlyMoveAttachmentsWithOldS3PathPattern() {
    UUID attachmentId = UUID.randomUUID();
    when(attachmentRepository.findAll())
        .thenReturn(
            List.of(
                AttachmentDTO.builder()
                    .id(attachmentId)
                    .s3ObjectPath(attachmentId.toString())
                    .format("docx")
                    .documentationUnit(
                        DecisionDTO.builder().documentNumber("XXRE123456789").build())
                    .build(),
                AttachmentDTO.builder()
                    .id(attachmentId)
                    .s3ObjectPath("XXRE111111111/" + attachmentId + ".docx")
                    .format("docx")
                    .documentationUnit(
                        DecisionDTO.builder().documentNumber("XXRE123456789").build())
                    .build()));
    TestMemoryAppender memoryAppender = new TestMemoryAppender(S3RenamingService.class);

    subject.moveExistingFilesToNewPaths();

    ArgumentCaptor<CopyObjectRequest> copyCaptor = ArgumentCaptor.forClass(CopyObjectRequest.class);
    ArgumentCaptor<DeleteObjectRequest> deleteCaptor =
        ArgumentCaptor.forClass(DeleteObjectRequest.class);
    ArgumentCaptor<AttachmentDTO> attachmentCaptor = ArgumentCaptor.forClass(AttachmentDTO.class);

    verify(s3Client).copyObject(copyCaptor.capture());
    assertThat(copyCaptor.getAllValues()).hasSize(1);
    assertThat(copyCaptor.getValue().sourceKey()).isEqualTo(attachmentId.toString());
    assertThat(copyCaptor.getValue().destinationKey())
        .isEqualTo("XXRE123456789/" + attachmentId + ".docx");
    verify(s3Client).deleteObject(deleteCaptor.capture());
    assertThat(deleteCaptor.getAllValues()).hasSize(1);
    assertThat(deleteCaptor.getValue().key()).isEqualTo(attachmentId.toString());
    verify(attachmentService).saveAttachment(attachmentCaptor.capture());
    assertThat(attachmentCaptor.getValue().getS3ObjectPath())
        .isEqualTo("XXRE123456789/" + attachmentId + ".docx");

    assertThat(memoryAppender.count(Level.ERROR)).isZero();
    assertThat(memoryAppender.count(Level.INFO)).isEqualTo(1);
    assertThat(memoryAppender.getMessage(Level.INFO, 0))
        .isEqualTo(
            "Moved attachment for 'XXRE123456789' from '"
                + attachmentId
                + "' to '"
                + "XXRE123456789/"
                + attachmentId
                + ".docx'");
    List<KeyValuePair> infoKVPairs = memoryAppender.getKeyValuePairs(Level.INFO, 0);
    assertThat(infoKVPairs)
        .containsExactlyInAnyOrder(
            new KeyValuePair("id", attachmentId),
            new KeyValuePair("document number", "XXRE123456789"),
            new KeyValuePair("old object path", attachmentId.toString()),
            new KeyValuePair("new object path", "XXRE123456789/" + attachmentId + ".docx"));
  }

  @Test
  void moveExistingFilesToNewPaths_errorWhileCopying_shouldLogErrorAndContinue() {
    UUID attachmentId1 = UUID.randomUUID();
    UUID attachmentId2 = UUID.randomUUID();
    when(attachmentRepository.findAll())
        .thenReturn(
            List.of(
                AttachmentDTO.builder()
                    .id(attachmentId1)
                    .s3ObjectPath(attachmentId1.toString())
                    .format("docx")
                    .documentationUnit(
                        DecisionDTO.builder().documentNumber("XXRE111111111").build())
                    .build(),
                AttachmentDTO.builder()
                    .id(attachmentId2)
                    .s3ObjectPath(attachmentId2.toString())
                    .format("docx")
                    .documentationUnit(
                        DecisionDTO.builder().documentNumber("XXRE123456789").build())
                    .build()));

    when(s3Client.copyObject(any(CopyObjectRequest.class)))
        .thenThrow(S3Exception.class)
        .thenReturn(CopyObjectResponse.builder().build());

    TestMemoryAppender memoryAppender = new TestMemoryAppender(S3RenamingService.class);

    subject.moveExistingFilesToNewPaths();

    assertThat(memoryAppender.count(Level.ERROR)).isEqualTo(1);
    assertThat(memoryAppender.count(Level.INFO)).isEqualTo(1);
    assertThat(memoryAppender.getMessage(Level.ERROR, 0))
        .isEqualTo("Error while copying attachment to new name");
    List<KeyValuePair> errorKVPairs = memoryAppender.getKeyValuePairs(Level.ERROR, 0);
    assertThat(errorKVPairs)
        .containsExactlyInAnyOrder(
            new KeyValuePair("id", attachmentId1),
            new KeyValuePair("document number", "XXRE111111111"),
            new KeyValuePair("old object path", attachmentId1.toString()),
            new KeyValuePair("new object path", "XXRE111111111/" + attachmentId1 + ".docx"));

    ArgumentCaptor<CopyObjectRequest> copyCaptor = ArgumentCaptor.forClass(CopyObjectRequest.class);
    ArgumentCaptor<DeleteObjectRequest> deleteCaptor =
        ArgumentCaptor.forClass(DeleteObjectRequest.class);
    ArgumentCaptor<AttachmentDTO> attachmentCaptor = ArgumentCaptor.forClass(AttachmentDTO.class);

    verify(s3Client, times(2)).copyObject(copyCaptor.capture());
    assertThat(copyCaptor.getAllValues().get(1).sourceKey()).isEqualTo(attachmentId2.toString());
    assertThat(copyCaptor.getAllValues().get(1).destinationKey())
        .isEqualTo("XXRE123456789/" + attachmentId2 + ".docx");
    verify(s3Client).deleteObject(deleteCaptor.capture());
    assertThat(deleteCaptor.getValue().key()).isEqualTo(attachmentId2.toString());
    verify(attachmentService).saveAttachment(attachmentCaptor.capture());
    assertThat(attachmentCaptor.getValue().getS3ObjectPath())
        .isEqualTo("XXRE123456789/" + attachmentId2 + ".docx");
  }

  @Test
  void moveExistingFilesToNewPaths_errorWhileDeleting_shouldLogErrorAndContinue() {
    UUID attachmentId1 = UUID.randomUUID();
    UUID attachmentId2 = UUID.randomUUID();
    when(attachmentRepository.findAll())
        .thenReturn(
            List.of(
                AttachmentDTO.builder()
                    .id(attachmentId1)
                    .s3ObjectPath(attachmentId1.toString())
                    .format("docx")
                    .documentationUnit(
                        DecisionDTO.builder().documentNumber("XXRE111111111").build())
                    .build(),
                AttachmentDTO.builder()
                    .id(attachmentId2)
                    .s3ObjectPath(attachmentId2.toString())
                    .format("docx")
                    .documentationUnit(
                        DecisionDTO.builder().documentNumber("XXRE123456789").build())
                    .build()));

    when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
        .thenThrow(S3Exception.class)
        .thenReturn(DeleteObjectResponse.builder().build());
    TestMemoryAppender memoryAppender = new TestMemoryAppender(S3RenamingService.class);

    subject.moveExistingFilesToNewPaths();

    assertThat(memoryAppender.count(Level.ERROR)).isEqualTo(1);
    assertThat(memoryAppender.count(Level.INFO)).isEqualTo(1);
    assertThat(memoryAppender.getMessage(Level.ERROR, 0))
        .isEqualTo(
            "Error while deleting attachment at old location (it was already copied successfully)");
    List<KeyValuePair> errorKVPairs = memoryAppender.getKeyValuePairs(Level.ERROR, 0);
    assertThat(errorKVPairs)
        .containsExactlyInAnyOrder(
            new KeyValuePair("id", attachmentId1),
            new KeyValuePair("document number", "XXRE111111111"),
            new KeyValuePair("old object path", attachmentId1.toString()),
            new KeyValuePair("new object path", "XXRE111111111/" + attachmentId1 + ".docx"));

    ArgumentCaptor<CopyObjectRequest> copyCaptor = ArgumentCaptor.forClass(CopyObjectRequest.class);
    ArgumentCaptor<DeleteObjectRequest> deleteCaptor =
        ArgumentCaptor.forClass(DeleteObjectRequest.class);
    ArgumentCaptor<AttachmentDTO> attachmentCaptor = ArgumentCaptor.forClass(AttachmentDTO.class);

    verify(s3Client, times(2)).copyObject(copyCaptor.capture());
    assertThat(copyCaptor.getAllValues().get(1).sourceKey()).isEqualTo(attachmentId2.toString());
    assertThat(copyCaptor.getAllValues().get(1).destinationKey())
        .isEqualTo("XXRE123456789/" + attachmentId2 + ".docx");
    verify(s3Client, times(2)).deleteObject(deleteCaptor.capture());
    assertThat(deleteCaptor.getAllValues().get(1).key()).isEqualTo(attachmentId2.toString());
    verify(attachmentService, times(2)).saveAttachment(attachmentCaptor.capture());
    assertThat(attachmentCaptor.getAllValues().get(1).getS3ObjectPath())
        .isEqualTo("XXRE123456789/" + attachmentId2 + ".docx");
  }

  @Test
  void moveExistingFilesToNewPaths_errorWhileUpdatingDTO_shouldLogErrorAndContinue() {
    UUID attachmentId1 = UUID.randomUUID();
    UUID attachmentId2 = UUID.randomUUID();
    when(attachmentRepository.findAll())
        .thenReturn(
            List.of(
                AttachmentDTO.builder()
                    .id(attachmentId1)
                    .s3ObjectPath(attachmentId1.toString())
                    .format("docx")
                    .documentationUnit(
                        DecisionDTO.builder().documentNumber("XXRE111111111").build())
                    .build(),
                AttachmentDTO.builder()
                    .id(attachmentId2)
                    .s3ObjectPath(attachmentId2.toString())
                    .format("docx")
                    .documentationUnit(
                        DecisionDTO.builder().documentNumber("XXRE123456789").build())
                    .build()));

    doThrow(RuntimeException.class)
        .doNothing()
        .when(attachmentService)
        .saveAttachment(any(AttachmentDTO.class));

    TestMemoryAppender memoryAppender = new TestMemoryAppender(S3RenamingService.class);

    subject.moveExistingFilesToNewPaths();

    assertThat(memoryAppender.count(Level.ERROR)).isEqualTo(1);
    assertThat(memoryAppender.count(Level.INFO)).isEqualTo(2);
    assertThat(memoryAppender.getMessage(Level.ERROR, 0))
        .isEqualTo(
            "Error while updating s3ObjectPath for moved attachment, trying to delete new file");
    assertThat(memoryAppender.getMessage(Level.INFO, 0))
        .isEqualTo("Deleted new file after failed update of s3ObjectPath");
    List<KeyValuePair> errorKVPairs = memoryAppender.getKeyValuePairs(Level.ERROR, 0);
    assertThat(errorKVPairs)
        .containsExactlyInAnyOrder(
            new KeyValuePair("id", attachmentId1),
            new KeyValuePair("document number", "XXRE111111111"),
            new KeyValuePair("old object path", attachmentId1.toString()),
            new KeyValuePair("new object path", "XXRE111111111/" + attachmentId1 + ".docx"));

    ArgumentCaptor<CopyObjectRequest> copyCaptor = ArgumentCaptor.forClass(CopyObjectRequest.class);
    ArgumentCaptor<DeleteObjectRequest> deleteCaptor =
        ArgumentCaptor.forClass(DeleteObjectRequest.class);
    ArgumentCaptor<AttachmentDTO> attachmentCaptor = ArgumentCaptor.forClass(AttachmentDTO.class);

    verify(s3Client, times(2)).copyObject(copyCaptor.capture());
    assertThat(copyCaptor.getAllValues().get(1).sourceKey()).isEqualTo(attachmentId2.toString());
    assertThat(copyCaptor.getAllValues().get(1).destinationKey())
        .isEqualTo("XXRE123456789/" + attachmentId2 + ".docx");
    verify(s3Client, times(2)).deleteObject(deleteCaptor.capture());
    assertThat(deleteCaptor.getAllValues().get(1).key()).isEqualTo(attachmentId2.toString());
    verify(attachmentService, times(2)).saveAttachment(attachmentCaptor.capture());
    assertThat(attachmentCaptor.getAllValues().get(1).getS3ObjectPath())
        .isEqualTo("XXRE123456789/" + attachmentId2 + ".docx");
  }

  @Test
  void moveRemainingFilesToUnreferenced_shouldOnlyMoveFilesWithOldS3PathPattern() {
    S3Object file = S3Object.builder().key("some-id").build();
    S3Object fileWithNewPattern =
        S3Object.builder().key("XXRE111111111/new-pattern-id.docx").build();
    when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
        .thenReturn(
            ListObjectsV2Response.builder().contents(List.of(fileWithNewPattern, file)).build());
    TestMemoryAppender memoryAppender = new TestMemoryAppender(S3RenamingService.class);

    subject.moveRemainingFilesToUnreferenced();

    ArgumentCaptor<CopyObjectRequest> copyCaptor = ArgumentCaptor.forClass(CopyObjectRequest.class);
    ArgumentCaptor<DeleteObjectRequest> deleteCaptor =
        ArgumentCaptor.forClass(DeleteObjectRequest.class);

    verify(s3Client).copyObject(copyCaptor.capture());
    assertThat(copyCaptor.getAllValues()).hasSize(1);
    assertThat(copyCaptor.getValue().sourceKey()).isEqualTo("some-id");
    assertThat(copyCaptor.getValue().destinationKey()).isEqualTo("unreferenced/some-id.docx");
    verify(s3Client).deleteObject(deleteCaptor.capture());
    assertThat(deleteCaptor.getAllValues()).hasSize(1);
    assertThat(deleteCaptor.getValue().key()).isEqualTo("some-id");

    assertThat(memoryAppender.count(Level.ERROR)).isZero();
    assertThat(memoryAppender.count(Level.INFO)).isEqualTo(1);
    assertThat(memoryAppender.getMessage(Level.INFO, 0))
        .isEqualTo("Moved unreferenced attachment from 'some-id' to 'unreferenced/some-id.docx'");
    List<KeyValuePair> infoKVPairs = memoryAppender.getKeyValuePairs(Level.INFO, 0);
    assertThat(infoKVPairs)
        .containsExactlyInAnyOrder(
            new KeyValuePair("old object path", "some-id"),
            new KeyValuePair("new object path", "unreferenced/some-id.docx"));
  }

  @Test
  void moveRemainingFilesToUnreferenced_errorWhileCopying_shouldLogErrorAndContinue() {
    S3Object errorFile = S3Object.builder().key("some-other-id").build();
    S3Object file = S3Object.builder().key("some-id").build();
    when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
        .thenReturn(ListObjectsV2Response.builder().contents(List.of(errorFile, file)).build());
    when(s3Client.copyObject(any(CopyObjectRequest.class)))
        .thenThrow(S3Exception.class)
        .thenReturn(CopyObjectResponse.builder().build());

    TestMemoryAppender memoryAppender = new TestMemoryAppender(S3RenamingService.class);

    subject.moveRemainingFilesToUnreferenced();

    ArgumentCaptor<CopyObjectRequest> copyCaptor = ArgumentCaptor.forClass(CopyObjectRequest.class);
    ArgumentCaptor<DeleteObjectRequest> deleteCaptor =
        ArgumentCaptor.forClass(DeleteObjectRequest.class);

    assertThat(memoryAppender.count(Level.ERROR)).isEqualTo(1);
    assertThat(memoryAppender.count(Level.INFO)).isEqualTo(1);
    assertThat(memoryAppender.getMessage(Level.ERROR, 0))
        .isEqualTo("Error while copying attachment to unreferenced prefix");
    List<KeyValuePair> errorKVPairs = memoryAppender.getKeyValuePairs(Level.ERROR, 0);
    assertThat(errorKVPairs)
        .containsExactlyInAnyOrder(
            new KeyValuePair("old object path", "some-other-id"),
            new KeyValuePair("new object path", "unreferenced/some-other-id.docx"));

    verify(s3Client, times(2)).copyObject(copyCaptor.capture());

    assertThat(copyCaptor.getAllValues().get(1).sourceKey()).isEqualTo("some-id");
    assertThat(copyCaptor.getAllValues().get(1).destinationKey())
        .isEqualTo("unreferenced/some-id.docx");
    verify(s3Client).deleteObject(deleteCaptor.capture());
    assertThat(deleteCaptor.getValue().key()).isEqualTo("some-id");
  }

  @Test
  void moveRemainingFilesToUnreferenced_errorWhileDeleting_shouldLogErrorAndContinue() {
    S3Object errorFile = S3Object.builder().key("some-other-id").build();
    S3Object file = S3Object.builder().key("some-id").build();
    when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
        .thenReturn(ListObjectsV2Response.builder().contents(List.of(errorFile, file)).build());
    when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
        .thenThrow(S3Exception.class)
        .thenReturn(DeleteObjectResponse.builder().build());

    TestMemoryAppender memoryAppender = new TestMemoryAppender(S3RenamingService.class);

    subject.moveRemainingFilesToUnreferenced();

    ArgumentCaptor<CopyObjectRequest> copyCaptor = ArgumentCaptor.forClass(CopyObjectRequest.class);
    ArgumentCaptor<DeleteObjectRequest> deleteCaptor =
        ArgumentCaptor.forClass(DeleteObjectRequest.class);

    assertThat(memoryAppender.count(Level.ERROR)).isEqualTo(1);
    assertThat(memoryAppender.count(Level.INFO)).isEqualTo(1);
    assertThat(memoryAppender.getMessage(Level.ERROR, 0))
        .isEqualTo(
            "Error deleting attachment from old location (it was already moved to unreferenced)");
    List<KeyValuePair> errorKVPairs = memoryAppender.getKeyValuePairs(Level.ERROR, 0);
    assertThat(errorKVPairs)
        .containsExactlyInAnyOrder(
            new KeyValuePair("old object path", "some-other-id"),
            new KeyValuePair("new object path", "unreferenced/some-other-id.docx"));

    verify(s3Client, times(2)).copyObject(copyCaptor.capture());

    assertThat(copyCaptor.getAllValues().get(1).sourceKey()).isEqualTo("some-id");
    assertThat(copyCaptor.getAllValues().get(1).destinationKey())
        .isEqualTo("unreferenced/some-id.docx");
    verify(s3Client, times(2)).deleteObject(deleteCaptor.capture());
    assertThat(deleteCaptor.getAllValues().get(1).key()).isEqualTo("some-id");
  }
}
