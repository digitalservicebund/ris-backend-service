package de.bund.digitalservice.ris.caselaw.adapter.converter.docx;

import static org.junit.jupiter.api.Assertions.assertTrue;

import de.bund.digitalservice.ris.caselaw.domain.docx.NumberingList;
import de.bund.digitalservice.ris.caselaw.domain.docx.NumberingList.DocumentationUnitNumberingListNumberFormat;
import de.bund.digitalservice.ris.caselaw.domain.docx.NumberingListEntry;
import de.bund.digitalservice.ris.caselaw.domain.docx.NumberingListEntryIndex;
import de.bund.digitalservice.ris.caselaw.domain.docx.ParagraphElement;
import de.bund.digitalservice.ris.caselaw.domain.docx.RunTextElement;
import org.docx4j.wml.JcEnumeration;
import org.junit.jupiter.api.Test;

class NumberingListEntryIndexGeneratorTest {

  private ParagraphElement paragraphWithText(String text) {
    ParagraphElement p = new ParagraphElement();
    RunTextElement r = new RunTextElement();
    r.setText(text);
    p.addRunElement(r);
    return p;
  }

  @Test
  void givenOrderedListWithCustomStart_whenGeneratingList_shouldIncludeStartAttribute() {
    NumberingList list = new NumberingList();

    NumberingListEntryIndex index =
        new NumberingListEntryIndex(
            "", // lvlText
            "5", // startVal
            "", // restartNumberingAfterBreak
            "", // color
            "", // fontStyle
            "", // fontSize
            false, // lvlPicBullet
            false, // isLgl
            DocumentationUnitNumberingListNumberFormat.DECIMAL, // numberFormat
            "0", // iLvl
            JcEnumeration.RIGHT, // lvlJc
            "space" // suff
            );

    list.addNumberingListEntry(new NumberingListEntry(paragraphWithText("Four"), index));
    list.addNumberingListEntry(new NumberingListEntry(paragraphWithText("Three"), index));

    String html = list.toHtmlString();

    assertTrue(html.contains("<ol"));
    assertTrue(html.contains("start=\"5\""));
    assertTrue(html.contains("<p>Four</p>") && html.contains("<p>Three</p>"));
  }
}
