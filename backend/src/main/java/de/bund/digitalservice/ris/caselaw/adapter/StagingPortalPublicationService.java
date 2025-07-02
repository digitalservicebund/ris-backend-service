package de.bund.digitalservice.ris.caselaw.adapter;

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
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StagingPortalPublicationService extends CommonPortalPublicationService {

  PortalBucket portalBucket;
  AttachmentRepository attachmentRepository;
  PortalTransformer ldmlTransformer;
  XmlUtilService xmlUtilService;

  public StagingPortalPublicationService(
      DocumentationUnitRepository documentationUnitRepository,
      AttachmentRepository attachmentRepository,
      XmlUtilService xmlUtilService,
      PortalBucket portalBucket,
      ObjectMapper objectMapper,
      PortalTransformer portalTransformer) {
    super(
        documentationUnitRepository,
        attachmentRepository,
        xmlUtilService,
        portalBucket,
        objectMapper,
        portalTransformer);
    this.portalBucket = portalBucket;
    this.attachmentRepository = attachmentRepository;
    this.ldmlTransformer = portalTransformer;
    this.xmlUtilService = xmlUtilService;
  }

  @Override
  public void uploadFullReindexChangelog() {
    // no-op in staging (it's only needed for prototype)
  }

  @Override
  public void uploadChangelog(
      List<String> publishedDocumentNumbers, List<String> deletedDocumentNumbers) {
    // no-op - all changelogs currently disabled
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
      List<String> existingFiles = portalBucket.getAllFilenamesByPath(path);
      List<String> addedFiles = new ArrayList<>();

      portalBucket.save(path + fileName, fileContent);
      addedFiles.add(path + fileName);

      if (!attachments.isEmpty()) {
        attachments.forEach(
            attachment -> {
              portalBucket.saveBytes(path + attachment.getFilename(), attachment.getContent());
              addedFiles.add(path + attachment.getFilename());
            });
      }

      // Check for files that are not part of this update and remove them (e.g. removed images)
      existingFiles.removeAll(addedFiles);
      existingFiles.forEach(portalBucket::delete);

      return new PortalPublicationResult(addedFiles, existingFiles);
    } catch (BucketException e) {
      throw new PublishException("Could not save LDML to bucket.", e);
    }
  }

  @Override
  public PortalPublicationResult deleteDocumentationUnit(String documentNumber) {
    try {
      var deletableFiles = portalBucket.getAllFilenamesByPath(documentNumber + "/");
      deletableFiles.forEach(portalBucket::delete);
      return new PortalPublicationResult(List.of(), deletableFiles);
    } catch (BucketException e) {
      throw new PublishException("Could not delete LDML from bucket.", e);
    }
  }
}
