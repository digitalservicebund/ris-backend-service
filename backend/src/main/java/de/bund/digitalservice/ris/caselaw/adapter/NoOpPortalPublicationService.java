package de.bund.digitalservice.ris.caselaw.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoOpPortalPublicationService extends CommonPortalPublicationService {

  public NoOpPortalPublicationService() {
    super(null, null, null, null, null);
  }

  @Override
  public void publishDocumentationUnit(String documentNumber) {
    // no-op
  }

  @Override
  public void deleteDocumentationUnit(String documentNumber) {
    // no-op
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
