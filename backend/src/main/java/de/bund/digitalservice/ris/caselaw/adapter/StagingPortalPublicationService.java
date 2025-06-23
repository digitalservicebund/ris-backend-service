package de.bund.digitalservice.ris.caselaw.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bund.digitalservice.ris.caselaw.adapter.exception.BucketException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.PublishException;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StagingPortalPublicationService extends CommonPortalPublicationService {

  private final PortalBucket portalBucket;

  public StagingPortalPublicationService(
      DocumentationUnitRepository documentationUnitRepository,
      XmlUtilService xmlUtilService,
      PortalBucket portalBucket,
      ObjectMapper objectMapper,
      PortalTransformer portalTransformer) {
    super(
        documentationUnitRepository, xmlUtilService, portalBucket, objectMapper, portalTransformer);
    this.portalBucket = portalBucket;
  }

  @Override
  public void uploadFullReindexChangelog() {
    // no-op in staging (it's only needed for prototype)
  }

  @Override
  public void uploadChangelog(
      List<String> publishedDocumentNumbers, List<String> deletedDocumentNumbers) {
    // no-op until new ldml bucket structure has been adapted by the portal team
  }

  @Override
  protected void saveToBucket(String uniqueId, String filename, String filecontent) {
    try {
      portalBucket.save(uniqueId + "/" + filename, filecontent);
    } catch (BucketException e) {
      throw new PublishException("Could not save LDML to bucket.", e);
    }
  }
}
