package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.TextCheckService;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.Match;
import java.util.Collections;
import java.util.List;

public class TextCheckMockService extends TextCheckService {

  public TextCheckMockService(DocumentationUnitService documentationUnitService) {
    super(documentationUnitService);
  }

  @Override
  public List<Match> requestTool(String text) {
    return Collections.emptyList();
  }
}
