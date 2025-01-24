package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.TextRange;
import de.bund.digitalservice.ris.caselaw.domain.languagetool.Match;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
    String htmlTestWithNoindex =
        "text contains no index tags <noindex>this part should be excluded from grammar check</noindex> but this not.";
    Document document = Jsoup.parse(htmlTestWithNoindex);
    String plainText = document.text();

    List<TextRange> noIndexTextRanges =
        LanguageToolService.findNoIndexPositions(Jsoup.parse(htmlTestWithNoindex), plainText);
    Assertions.assertEquals(1, noIndexTextRanges.size());
    Assertions.assertEquals(28, noIndexTextRanges.getFirst().start());
    Assertions.assertEquals(75, noIndexTextRanges.getFirst().end());
    String expectedTest = "this part should be excluded from grammar check";
    Assertions.assertEquals(
        expectedTest,
        plainText.substring(
            noIndexTextRanges.getFirst().start(), noIndexTextRanges.getFirst().end()));
  }

  @Test
  void matchIsBetweenNoIndexPosition() {
    TextRange noIndexTextRange = TextRange.builder().start(3).end(10).build();
    var match = Match.builder().offset(6).length(5).build();
    var result =
        LanguageToolService.matchIsBetweenNoIndexPosition(match, List.of(noIndexTextRange));
    Assertions.assertTrue(result);
  }
}
