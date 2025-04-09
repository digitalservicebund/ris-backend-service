package de.bund.digitalservice.ris.caselaw.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import java.util.List;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class InternalPortalPublicationService {

  private final PortalPublicationService portalPublicationService;

  @Autowired
  public InternalPortalPublicationService(
      DocumentationUnitRepository documentationUnitRepository,
      XmlUtilService xmlUtilService,
      DocumentBuilderFactory documentBuilderFactory,
      InternalPortalBucket internalPortalBucket,
      ObjectMapper objectMapper) {

    this.portalPublicationService =
        new PortalPublicationService(
            documentationUnitRepository,
            xmlUtilService,
            documentBuilderFactory,
            internalPortalBucket,
            objectMapper);
  }

  public void publishDocumentationUnitWithChangelog(UUID documentationUnitId)
      throws DocumentationUnitNotExistsException {
    portalPublicationService.publishDocumentationUnitWithChangelog(documentationUnitId);
  }

  public void publishDocumentationUnit(String documentNumber)
      throws DocumentationUnitNotExistsException {
    portalPublicationService.publishDocumentationUnit(documentNumber);
  }

  public void deleteDocumentationUnit(String documentNumber) {
    portalPublicationService.deleteDocumentationUnit(documentNumber);
  }

  public void uploadChangelog(
      List<String> publishedDocumentNumbers, List<String> deletedDocumentNumbers)
      throws JsonProcessingException {
    portalPublicationService.uploadChangelog(publishedDocumentNumbers, deletedDocumentNumbers);
  }

  public void uploadFullReindexChangelog() throws JsonProcessingException {
    portalPublicationService.uploadFullReindexChangelog();
  }
}
