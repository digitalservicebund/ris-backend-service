package de.bund.digitalservice.ris.caselaw.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StagingPortalPublicationService extends CommonPortalPublicationService {

  public StagingPortalPublicationService(
      DocumentationUnitRepository documentationUnitRepository,
      XmlUtilService xmlUtilService,
      PortalBucket portalBucket,
      ObjectMapper objectMapper,
      PortalTransformer portalTransformer) {

    super(
        documentationUnitRepository, xmlUtilService, portalBucket, objectMapper, portalTransformer);
  }

  @Override
  public void uploadFullReindexChangelog() {
    // no-op in staging (it's only needed for prototype)
  }
}
