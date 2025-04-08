package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.TextCheckService;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.Match;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckWordRepository;
import java.util.Collections;
import java.util.List;

public class TextCheckMockService extends TextCheckService {

  public TextCheckMockService(
      DocumentationUnitRepository documentationUnitRepository,
      IgnoredTextCheckWordRepository ignoredTextCheckWordRepository) {
    super(documentationUnitRepository, ignoredTextCheckWordRepository);
  }

  @Override
  public List<Match> requestTool(String text) {
    return Collections.emptyList();
  }
}
