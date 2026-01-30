package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentRepository;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
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
  private final DocumentationUnitRepository documentationUnitRepository;
  private final AttachmentRepository attachmentRepository;
  private final S3Bucket attachmentBucket;
  private final S3Client s3Client;

  @Value("${otc.obs.bucket-name}")
  private String bucketName;

  public S3RenamingService(
      DocumentationUnitRepository documentationUnitRepository,
      AttachmentRepository attachmentRepository,
      S3Bucket portalBucket,
      S3Client s3Client) {

    this.documentationUnitRepository = documentationUnitRepository;
    this.attachmentRepository = attachmentRepository;
    this.attachmentBucket = portalBucket;
    this.s3Client = s3Client;
  }

  /** */
  @Scheduled(cron = "20 23 5 * * *", zone = "Europe/Berlin")
  @SchedulerLock(name = "doc-unit-inheritance-consistency", lockAtMostFor = "PT5M")
  @Transactional
  public void adjustExistingS3FilesToNewPattern() {
    // check all files in bucket or in database?
    // all files in bucket --> if exist in database --> move to folder with doc number prefix and
    // add format suffix based on table
    // if not exist in database --> move to folder 'unknown' and add format suffix docx (all files
    // are docx currently)
    // if we do this after merge --> files could already have suffix and refix

    List<AttachmentDTO> attachmentsToMove =
        attachmentRepository.findAll().stream()
            .filter(it -> it.getS3ObjectPath() != null)
            .filter(it -> !it.getS3ObjectPath().contains("/")) // old pattern
            .toList();

    attachmentsToMove.forEach(
        attachment -> {
          String documentNumber = attachment.getDocumentationUnit().getDocumentNumber();
          String format = attachment.getFormat();
          String oldObjectPath = attachment.getS3ObjectPath();
          String newObjectPath = documentNumber + "/" + attachment.getId() + "." + format;

          // todo add try catch?

          // copy file to a new name
          s3Client.copyObject(
              CopyObjectRequest.builder()
                  .sourceBucket(bucketName)
                  .sourceKey(oldObjectPath)
                  .destinationBucket(bucketName)
                  .destinationKey(newObjectPath)
                  .build());

          // delete file at the old location
          s3Client.deleteObject(
              DeleteObjectRequest.builder().bucket(bucketName).key(oldObjectPath).build());

          // update dto object path
          attachment.setS3ObjectPath(newObjectPath);
          attachmentRepository.save(attachment);

          log.atInfo()
              .setMessage(
                  String.format(
                      "Moved attachment for %s from %s to %s",
                      documentNumber, attachment.getId(), newObjectPath))
              .addKeyValue("id", attachment.getId())
              .addKeyValue("documentNumber", documentNumber)
              .addKeyValue("old object path", oldObjectPath)
              .addKeyValue("new object path", newObjectPath)
              .log();
          log.info(
              "Moved attachment for {} from {} to {}",
              documentNumber,
              attachment.getId(),
              newObjectPath);
        });

    // check remaining files without extension in s3 bucket and move to unreferenced folder

    ListObjectsV2Response response =
        s3Client.listObjectsV2(ListObjectsV2Request.builder().bucket(bucketName).build());
    response.contents().stream()
        .map(S3Object::key)
        .filter(it -> !it.contains("/"))
        .forEach(
            it -> {
              String newObjectPath = "unreferenced/" + UUID.randomUUID() + ".docx";
              s3Client.copyObject(
                  CopyObjectRequest.builder()
                      .sourceBucket(bucketName)
                      .sourceKey(it)
                      .destinationBucket(bucketName)
                      .destinationKey(newObjectPath)
                      .build());
              s3Client.deleteObject(
                  DeleteObjectRequest.builder().bucket(bucketName).key(it).build());
            });
  }
}
