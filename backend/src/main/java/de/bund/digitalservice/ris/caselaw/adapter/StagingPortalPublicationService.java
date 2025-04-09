package de.bund.digitalservice.ris.caselaw.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StagingPortalPublicationService extends CommonPortalPublicationService {

  public StagingPortalPublicationService(
      DocumentationUnitRepository documentationUnitRepository,
      XmlUtilService xmlUtilService,
      DocumentBuilderFactory documentBuilderFactory,
      InternalPortalBucket internalPortalBucket,
      ObjectMapper objectMapper) {

    super(
        documentationUnitRepository,
        xmlUtilService,
        documentBuilderFactory,
        internalPortalBucket,
        objectMapper);
  }

  @Override
  public void uploadFullReindexChangelog() {
    // no-op in staging (it's only needed for prototype)
  }
}
