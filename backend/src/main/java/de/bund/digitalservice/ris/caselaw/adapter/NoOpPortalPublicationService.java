package de.bund.digitalservice.ris.caselaw.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoOpPortalPublicationService implements PortalPublicationService {

  @Override
  public void publishDocumentationUnitWithChangelog(UUID documentationUnitId) {
    // no-op
  }

  @Override
  public PortalPublicationResult publishDocumentationUnit(String documentNumber) {
    // no-op
    return new PortalPublicationResult(List.of(), List.of());
  }

  @Override
  public PortalPublicationResult deleteDocumentationUnit(String documentNumber) {
    // no-op
    return new PortalPublicationResult(List.of(), List.of());
  }

  @Override
  public void uploadChangelog(
      List<String> publishedDocumentNumbers, List<String> deletedDocumentNumbers) {
    // no-op
  }

  @Override
  public void uploadFullReindexChangelog() throws JsonProcessingException {
    // no-op
  }
}
