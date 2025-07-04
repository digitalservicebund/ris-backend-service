package de.bund.digitalservice.ris.caselaw.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentRepository;
import de.bund.digitalservice.ris.caselaw.adapter.exception.BucketException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.LdmlTransformationException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.PublishException;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
public class PrototypePortalPublicationService extends CommonPortalPublicationService {

  private final RiiService riiService;
  private final DocumentationUnitRepository documentationUnitRepository;
  private final AttachmentRepository attachmentRepository;
  private final PrototypePortalBucket prototypePortalBucket;
  private final PortalTransformer ldmlTransformer;
  private final XmlUtilService xmlUtilService;

  public PrototypePortalPublicationService(
      DocumentationUnitRepository documentationUnitRepository,
      AttachmentRepository attachmentRepository,
      XmlUtilService xmlUtilService,
      PrototypePortalBucket prototypePortalBucket,
      ObjectMapper objectMapper,
      PortalTransformer portalTransformer,
      RiiService riiService) {
    super(
        documentationUnitRepository,
        xmlUtilService,
        prototypePortalBucket,
        objectMapper,
        portalTransformer);
    this.riiService = riiService;
    this.documentationUnitRepository = documentationUnitRepository;
    this.attachmentRepository = attachmentRepository;
    this.prototypePortalBucket = prototypePortalBucket;
    this.ldmlTransformer = portalTransformer;
    this.xmlUtilService = xmlUtilService;
  }

  @Override
  public void publishDocumentationUnitWithChangelog(UUID documentationUnitId) {
    // no-op in prototype
  }

  @Override
  public void uploadChangelog(
      List<String> publishedDocumentNumbers, List<String> deletedDocumentNumbers) {
    // no-op in prototype - delta changelog uploads currently disabled
  }

  @Override
  protected PortalPublicationResult publishToBucket(DocumentationUnit documentationUnit) {
    if (!(documentationUnit instanceof Decision decision)) {
      // for now pending proceedings can not be transformed to LDML, so they are ignored.
      return null;
    }
    List<AttachmentDTO> attachments =
        attachmentRepository.findAllByDocumentationUnitId(documentationUnit.uuid());
    CaseLawLdml ldml = ldmlTransformer.transformToLdml(decision);
    Optional<String> fileContent = xmlUtilService.ldmlToString(ldml);
    if (fileContent.isEmpty()) {
      throw new LdmlTransformationException("Could not parse transformed LDML as string.", null);
    }

    return saveToBucket(
        ldml.getUniqueId() + "/", ldml.getFileName(), fileContent.get(), attachments);
  }

  protected PortalPublicationResult saveToBucket(
      String path, String fileName, String fileContent, List<AttachmentDTO> attachments) {
    try {
      List<String> existingFiles = prototypePortalBucket.getAllFilenamesByPath(path);
      List<String> addedFiles = new ArrayList<>();

      prototypePortalBucket.save(path + fileName, fileContent);
      prototypePortalBucket.save(
          fileName, fileContent); // save in root as well to not break the live portal
      addedFiles.add(path + fileName);

      if (!attachments.isEmpty()) {
        attachments.forEach(
            attachment -> {
              prototypePortalBucket.saveBytes(
                  path + attachment.getFilename(), attachment.getContent());
              addedFiles.add(path + attachment.getFilename());
            });
      }

      // Check for files that are not part of this update and remove them (e.g. removed images)
      existingFiles.removeAll(addedFiles);
      existingFiles.forEach(prototypePortalBucket::delete);

      return new PortalPublicationResult(addedFiles, existingFiles);
    } catch (BucketException e) {
      throw new PublishException("Could not save LDML to bucket.", e);
    }
  }

  @Override
  public PortalPublicationResult deleteDocumentationUnit(String documentNumber) {
    try {
      var deletableFiles = prototypePortalBucket.getAllFilenamesByPath(documentNumber + "/");
      deletableFiles.forEach(prototypePortalBucket::delete);
      prototypePortalBucket.delete(
          documentNumber + ".xml"); // delete in root as well to not break the live portal
      return new PortalPublicationResult(List.of(), deletableFiles);
    } catch (BucketException e) {
      throw new PublishException("Could not delete LDML from bucket.", e);
    }
  }

  //                        ↓ day of month (1-31)
  //                      ↓ hour (0-23)
  //                    ↓ minute (0-59)
  //                 ↓ second (0-59)
  // Default:        0 30 5 * * * (After migration: 5:30)
  @Scheduled(cron = "0 30 5 * * *", zone = "Europe/Berlin")
  @SchedulerLock(name = "portal-publication-diff-job", lockAtMostFor = "PT15M")
  public void logPortalPublicationSanityCheck() {
    List<String> portalBucketDocumentNumbers =
        prototypePortalBucket.getAllFilenames().stream()
            .filter(fileName -> fileName.contains(".xml"))
            .map(fileName -> fileName.substring(0, fileName.lastIndexOf('.')))
            //            .map(fileName -> fileName.substring(0, fileName.lastIndexOf("/")))
            .toList();

    logDatabaseToBucketDiff(portalBucketDocumentNumbers);
    logBucketToRechtsprechungImInternetDiff(portalBucketDocumentNumbers);
  }

  private void logBucketToRechtsprechungImInternetDiff(List<String> portalBucketDocumentNumbers) {
    log.info(
        "Checking for discrepancies between published doc units and Rechtsprechung im Internet...");

    var riiDocumentNumbers = riiService.fetchRiiDocumentNumbers();
    log.info("Number of documents in Rechtsprechung im Internet: {}", riiDocumentNumbers.size());

    if (!riiDocumentNumbers.isEmpty()) {
      List<String> inRiiNotInPortal =
          riiDocumentNumbers.stream()
              .filter(documentNumber -> !portalBucketDocumentNumbers.contains(documentNumber))
              .toList();

      List<String> inPortalNotInRii =
          portalBucketDocumentNumbers.stream()
              .filter(documentNumber -> !riiDocumentNumbers.contains(documentNumber))
              .toList();

      log.info(
          "Found {} doc units in Portal but not in Rechtsprechung im Internet.",
          inPortalNotInRii.size());
      log.info(
          "Found {} doc units in Rechtsprechung im Internet but not in Portal.",
          inRiiNotInPortal.size());
      if (!inRiiNotInPortal.isEmpty()) {
        log.info(
            "Document numbers found in Rechtsprechung im Internet but not in Portal: {}",
            inRiiNotInPortal.stream().map(Object::toString).collect(Collectors.joining(", ")));
      }
      if (!inPortalNotInRii.isEmpty()) {
        log.info(
            "Document numbers found in Portal but not in Rechtsprechung im Internet: {}",
            inPortalNotInRii.stream().map(Object::toString).collect(Collectors.joining(", ")));
        log.info("Deleting documents not in Rechtsprechung im Internet...");
        try {
          inPortalNotInRii.forEach(this::deleteDocumentationUnit);
          uploadDeletionChangelog(
              inPortalNotInRii.stream().map(documentNumber -> documentNumber + ".xml").toList());

        } catch (JsonProcessingException | PublishException e) {
          log.error(
              "Deleting documents not in Rechtsprechung im Internet failed with exception: {}",
              e.getMessage());
        }
      }
    }
  }

  private void logDatabaseToBucketDiff(List<String> portalBucketDocumentNumbers) {
    List<String> databaseDocumentNumbers =
        documentationUnitRepository.findAllDocumentNumbersByMatchingPublishCriteria();
    List<String> inBucketNotInDatabase =
        portalBucketDocumentNumbers.stream()
            .filter(documentNumber -> !databaseDocumentNumbers.contains(documentNumber))
            .toList();

    List<String> inDatabaseNotInBucket =
        databaseDocumentNumbers.stream()
            .filter(documentNumber -> !portalBucketDocumentNumbers.contains(documentNumber))
            .toList();

    log.info("Number of LDML files in bucket: {}", portalBucketDocumentNumbers.size());
    log.info(
        "Found {} publishable doc units by database query but not in bucket.",
        inDatabaseNotInBucket.size());
    if (!inDatabaseNotInBucket.isEmpty()) {
      log.info(
          "Document numbers found in database but not in bucket: {}",
          inDatabaseNotInBucket.stream().map(Object::toString).collect(Collectors.joining(", ")));
    }
    log.info(
        "Found {} doc units in bucket but not by database query.", inBucketNotInDatabase.size());
    if (!inBucketNotInDatabase.isEmpty()) {
      log.info(
          "Document numbers found in bucket but not in database: {}",
          inBucketNotInDatabase.stream().map(Object::toString).collect(Collectors.joining(", ")));
    }
  }
}
