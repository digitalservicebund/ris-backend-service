package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

@Service
@Slf4j
public class S3RenamingService {
  private final AttachmentRepository attachmentRepository;
  private final S3Client s3Client;

  @Value("${otc.obs.bucket-name}")
  private String bucketName;

  public S3RenamingService(
      AttachmentRepository attachmentRepository, @Qualifier("docxS3Client") S3Client s3Client) {
    this.attachmentRepository = attachmentRepository;
    this.s3Client = s3Client;
  }

  /**
   * Adds a document number path prefix and .docx file extension to existing files in the old s3
   * path pattern
   */
  @Scheduled(cron = "-", zone = "Europe/Berlin")
  @SchedulerLock(name = "adjust-s3-paths", lockAtMostFor = "PT12H")
  @Transactional
  public void moveExistingFilesToNewPaths() {
    List<AttachmentDTO> attachmentsToMove =
        attachmentRepository.findAll().stream()
            .filter(it -> it.getS3ObjectPath() != null)
            .filter(it -> !it.getS3ObjectPath().contains("/")) // old pattern without path prefix
            .toList();

    attachmentsToMove.forEach(
        attachment -> {
          String documentNumber = "";
          String oldObjectPath = "";
          String newObjectPath = "";
          String format;

          try {
            documentNumber = attachment.getDocumentationUnit().getDocumentNumber();
            format = attachment.getFormat();
            oldObjectPath = attachment.getS3ObjectPath();
            newObjectPath = String.format("%s/%s.%s", documentNumber, attachment.getId(), format);

            // copy file to a new name
            s3Client.copyObject(
                CopyObjectRequest.builder()
                    .sourceBucket(bucketName)
                    .sourceKey(oldObjectPath)
                    .destinationBucket(bucketName)
                    .destinationKey(newObjectPath)
                    .build());

          } catch (Exception e) {
            log.atError()
                .setMessage("Error while copying attachment to new name")
                .setCause(e)
                .addKeyValue("id", attachment.getId())
                .addKeyValue("documentNumber", documentNumber)
                .addKeyValue("old object path", oldObjectPath)
                .addKeyValue("new object path", newObjectPath)
                .log();
            return;
          }

          try {
            // delete file at the old location
            s3Client.deleteObject(
                DeleteObjectRequest.builder().bucket(bucketName).key(oldObjectPath).build());
          } catch (Exception e) {
            log.atError()
                .setMessage(
                    "Error while deleting attachment at old location (it was already copied successfully)")
                .setCause(e)
                .addKeyValue("id", attachment.getId())
                .addKeyValue("documentNumber", documentNumber)
                .addKeyValue("old object path", oldObjectPath)
                .addKeyValue("new object path", newObjectPath)
                .log();
            return;
          }

          try {
            // update dto object path
            attachment.setS3ObjectPath(newObjectPath);
            attachmentRepository.save(attachment);
          } catch (Exception e) {
            log.atError()
                .setMessage("Error while while updating s3ObjectPath for moved attachment")
                .setCause(e)
                .addKeyValue("id", attachment.getId())
                .addKeyValue("documentNumber", documentNumber)
                .addKeyValue("old object path", oldObjectPath)
                .addKeyValue("new object path", newObjectPath)
                .log();
            return;
          }

          log.atInfo()
              .setMessage(
                  String.format(
                      "Moved attachment for '%s' from '%s' to '%s'",
                      documentNumber, oldObjectPath, newObjectPath))
              .addKeyValue("id", attachment.getId())
              .addKeyValue("documentNumber", documentNumber)
              .addKeyValue("old object path", oldObjectPath)
              .addKeyValue("new object path", newObjectPath)
              .log();
        });
  }

  /**
   * Moves files in the old s3 path pattern that were not moved in the previous step into the
   * 'unreferenced' prefix
   */
  @Scheduled(cron = "-", zone = "Europe/Berlin")
  @SchedulerLock(name = "move-unreferenced", lockAtMostFor = "PT12H")
  public void moveRemainingFilesToUnreferenced() {
    ListObjectsV2Response response =
        s3Client.listObjectsV2(ListObjectsV2Request.builder().bucket(bucketName).build());
    response.contents().stream()
        .map(S3Object::key)
        .filter(it -> !it.contains("/"))
        .forEach(
            oldObjectPath -> {
              String newObjectPath = String.format("unreferenced/%s.docx", oldObjectPath);
              try {
                s3Client.copyObject(
                    CopyObjectRequest.builder()
                        .sourceBucket(bucketName)
                        .sourceKey(oldObjectPath)
                        .destinationBucket(bucketName)
                        .destinationKey(newObjectPath)
                        .build());
              } catch (Exception e) {
                log.atError()
                    .setMessage("Error while copying attachment to unreferenced prefix")
                    .setCause(e)
                    .addKeyValue("old object path", oldObjectPath)
                    .addKeyValue("new object path", newObjectPath)
                    .log();
                return;
              }

              try {
                s3Client.deleteObject(
                    DeleteObjectRequest.builder().bucket(bucketName).key(oldObjectPath).build());
              } catch (Exception e) {
                log.atError()
                    .setMessage(
                        "Error deleting attachment from old location (it was already moved to unreferenced)")
                    .setCause(e)
                    .addKeyValue("old object path", oldObjectPath)
                    .addKeyValue("new object path", newObjectPath)
                    .log();
                return;
              }

              log.atInfo()
                  .setMessage(
                      String.format(
                          "Moved unreferenced attachment from '%s' to '%s'",
                          oldObjectPath, newObjectPath))
                  .addKeyValue("old object path", oldObjectPath)
                  .addKeyValue("new object path", newObjectPath)
                  .log();
            });
  }
}
