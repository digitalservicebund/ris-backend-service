package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
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
  private final TempAttachmentService attachmentService;
  private final Environment env;

  private static final String ID = "id";
  private static final String DOC_NUMBER = "document number";
  private static final String OLD_PATH = "old object path";
  private static final String NEW_PATH = "new object path";

  @Value("${otc.obs.bucket-name}")
  private String bucketName;

  public S3RenamingService(
      AttachmentRepository attachmentRepository,
      @Qualifier("docxS3Client") S3Client s3Client,
      TempAttachmentService attachmentService,
      Environment env) {
    this.attachmentRepository = attachmentRepository;
    this.s3Client = s3Client;
    this.attachmentService = attachmentService;
    this.env = env;
  }

  /**
   * Adds a document number path prefix and .docx file extension to existing files in the old s3
   * path pattern
   */
  @Scheduled(cron = "0 30 16 4 2 *", zone = "Europe/Berlin")
  @SchedulerLock(name = "adjust-s3-paths", lockAtMostFor = "PT12H")
  public void moveExistingFilesToNewPaths() {
    if (!env.matchesProfiles("staging")) {
      return;
    }
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
                .addKeyValue(ID, attachment.getId())
                .addKeyValue(DOC_NUMBER, documentNumber)
                .addKeyValue(OLD_PATH, oldObjectPath)
                .addKeyValue(NEW_PATH, newObjectPath)
                .log();
            return;
          }

          try {
            attachment.setS3ObjectPath(newObjectPath);
            attachmentService.saveAttachment(attachment);
          } catch (Exception e) {
            log.atError()
                .setMessage(
                    "Error while updating s3ObjectPath for moved attachment, trying to delete new file")
                .setCause(e)
                .addKeyValue(ID, attachment.getId())
                .addKeyValue(DOC_NUMBER, documentNumber)
                .addKeyValue(OLD_PATH, oldObjectPath)
                .addKeyValue(NEW_PATH, newObjectPath)
                .log();
            // roll back s3 change
            s3Client.deleteObject(
                DeleteObjectRequest.builder().bucket(bucketName).key(newObjectPath).build());
            log.atInfo()
                .setMessage("Deleted new file after failed update of s3ObjectPath")
                .addKeyValue(ID, attachment.getId())
                .addKeyValue(DOC_NUMBER, documentNumber)
                .addKeyValue(OLD_PATH, oldObjectPath)
                .addKeyValue(NEW_PATH, newObjectPath)
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
                .addKeyValue(ID, attachment.getId())
                .addKeyValue(DOC_NUMBER, documentNumber)
                .addKeyValue(OLD_PATH, oldObjectPath)
                .addKeyValue(NEW_PATH, newObjectPath)
                .log();
            return;
          }

          log.atInfo()
              .setMessage(
                  String.format(
                      "Moved attachment for '%s' from '%s' to '%s'",
                      documentNumber, oldObjectPath, newObjectPath))
              .addKeyValue(ID, attachment.getId())
              .addKeyValue(DOC_NUMBER, documentNumber)
              .addKeyValue(OLD_PATH, oldObjectPath)
              .addKeyValue(NEW_PATH, newObjectPath)
              .log();
        });
  }

  /**
   * Moves files in the old s3 path pattern that were not moved in the previous step into the
   * 'unreferenced' prefix
   */
  @Scheduled(cron = "0 0 17 * * *", zone = "Europe/Berlin")
  @SchedulerLock(name = "move-unreferenced", lockAtMostFor = "PT1H")
  public void moveRemainingFilesToUnreferenced() {
    if (!env.matchesProfiles("staging")) {
      return;
    }
    log.atInfo().setMessage("Starting to move unreferenced files").log();
    getAllFilenames().stream()
        .filter(it -> !it.contains("/"))
        .forEach(
            oldObjectPath -> {
              Optional<AttachmentDTO> existing =
                  attachmentRepository.findByS3ObjectPath(oldObjectPath);
              if (existing.isPresent()) {
                log.atInfo()
                    .setMessage("Attachment with matching object path exists in database, skipping")
                    .addKeyValue(OLD_PATH, oldObjectPath)
                    .log();
                return;
              }
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
                    .addKeyValue(OLD_PATH, oldObjectPath)
                    .addKeyValue(NEW_PATH, newObjectPath)
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
                    .addKeyValue(OLD_PATH, oldObjectPath)
                    .addKeyValue(NEW_PATH, newObjectPath)
                    .log();
                return;
              }

              log.atInfo()
                  .setMessage(
                      String.format(
                          "Moved unreferenced attachment from '%s' to '%s'",
                          oldObjectPath, newObjectPath))
                  .addKeyValue(OLD_PATH, oldObjectPath)
                  .addKeyValue(NEW_PATH, newObjectPath)
                  .log();
            });
    log.atInfo().setMessage("Finished moving unreferenced files").log();
  }

  public List<String> getAllFilenames() {
    List<String> keys = new ArrayList<>();
    ListObjectsV2Response response;
    ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(bucketName).build();
    do {
      response = s3Client.listObjectsV2(request);
      if (response == null) {
        return Collections.emptyList();
      }
      keys.addAll(response.contents().stream().map(S3Object::key).toList());
      String token = response.nextContinuationToken();
      request = ListObjectsV2Request.builder().bucket(bucketName).continuationToken(token).build();
    } while (Boolean.TRUE.equals(response.isTruncated()));

    return keys;
  }
}
