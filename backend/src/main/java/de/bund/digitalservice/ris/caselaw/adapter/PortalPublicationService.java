package de.bund.digitalservice.ris.caselaw.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import java.util.List;
import java.util.UUID;

public interface PortalPublicationService {

  void publishDocumentationUnitWithChangelog(UUID documentationUnitId)
      throws DocumentationUnitNotExistsException;

  PortalPublicationResult publishDocumentationUnit(String documentNumber)
      throws DocumentationUnitNotExistsException;

  PortalPublicationResult deleteDocumentationUnit(String documentNumber);

  void uploadChangelog(List<String> publishedDocumentNumbers, List<String> deletedDocumentNumbers)
      throws JsonProcessingException;

  void uploadFullReindexChangelog() throws JsonProcessingException;
}
