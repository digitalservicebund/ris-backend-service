package de.bund.digitalservice.ris.caselaw.adapter;

import static org.junit.jupiter.api.Assertions.*;

import de.bund.digitalservice.ris.caselaw.config.LanguageToolConfig;
import de.bund.digitalservice.ris.caselaw.domain.TextRange;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({LanguageToolService.class, LanguageToolConfig.class})
class LanguageToolServiceTest {

  @Autowired LanguageToolService service;

  @Test
  void testGetNoIndex_shouldReturnNoIndexTextRangesPositions() {
    String testWithNoindex = "Investmentfonds SEB<noindex>Optimix</noindex>Ertrag (WKN: 974891)";
    List<TextRange> noIndexTextRanges = service.getNoIndexTextRanges(testWithNoindex);
    Assertions.assertEquals(1, noIndexTextRanges.size());
    assertEquals(19, noIndexTextRanges.getFirst().start());
    assertEquals(45, noIndexTextRanges.getFirst().end());
  }
}
