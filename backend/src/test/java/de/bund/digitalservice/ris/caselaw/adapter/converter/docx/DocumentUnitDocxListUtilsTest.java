package de.bund.digitalservice.ris.caselaw.adapter.converter.docx;

import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.domain.docx.BorderNumber;
import de.bund.digitalservice.ris.caselaw.domain.docx.DocumentUnitDocx;
import de.bund.digitalservice.ris.caselaw.domain.docx.NumberingList;
import de.bund.digitalservice.ris.caselaw.domain.docx.NumberingList.DocumentUnitNumberingListNumberFormat;
import de.bund.digitalservice.ris.caselaw.domain.docx.NumberingListEntry;
import de.bund.digitalservice.ris.caselaw.domain.docx.NumberingListEntryIndex;
import de.bund.digitalservice.ris.caselaw.domain.docx.ParagraphElement;
import de.bund.digitalservice.ris.caselaw.domain.docx.RunTextElement;
import de.bund.digitalservice.ris.caselaw.domain.docx.Style;
import java.util.ArrayList;
import java.util.List;
import org.docx4j.wml.JcEnumeration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(OutputCaptureExtension.class)
class DocumentUnitDocxListUtilsTest {

  @Test
  void testPostprocessBorderNumbers_withEmptyList_shouldDoNothing() {
    List<DocumentUnitDocx> list = new ArrayList<>();

    DocumentUnitDocxListUtils.postprocessBorderNumbers(list);

    assertThat(list).isEmpty();
  }

  @Test
  void testPostprocessBorderNumbers_withNull_shouldDoNothing() {

    DocumentUnitDocxListUtils.postprocessBorderNumbers(null);
  }

  @Test
  void testPostprocessBorderNumbers_withTwoNumberlessBorderNumbers_shouldAssignNumbers() {
    List<DocumentUnitDocx> list = new ArrayList<>();
    list.add(createNumberlessBorderNumberWithNumId(7));
    list.add(createNumberlessBorderNumberWithNumId(7));

    DocumentUnitDocxListUtils.postprocessBorderNumbers(list);

    assertThat(list).hasSize(2);
    assertThat(list.get(0)).isInstanceOf(BorderNumber.class);
    BorderNumber borderNumber0 = (BorderNumber) list.get(0);
    assertThat(borderNumber0.getNumber()).isEqualTo("1");
    assertThat(borderNumber0.getNumId()).isEqualTo(7);
    assertThat(list.get(0)).isInstanceOf(BorderNumber.class);
    BorderNumber borderNumber1 = (BorderNumber) list.get(1);
    assertThat(borderNumber1.getNumber()).isEqualTo("2");
    assertThat(borderNumber1.getNumId()).isEqualTo(7);
  }

  @Test
  void testPostprocessBorderNumbers_withNumberlessBorderNumbersButDifferentNumIds_shouldLogError(
      CapturedOutput output) {
    List<DocumentUnitDocx> list = new ArrayList<>();
    list.add(createNumberlessBorderNumberWithNumId(7));
    list.add(createNumberlessBorderNumberWithNumId(8));

    DocumentUnitDocxListUtils.postprocessBorderNumbers(list);

    assertThat(list).hasSize(2);
    assertThat(list.get(0)).isInstanceOf(BorderNumber.class);
    BorderNumber borderNumber0 = (BorderNumber) list.get(0);
    assertThat(borderNumber0.getNumber()).isEqualTo("1");
    assertThat(borderNumber0.getNumId()).isEqualTo(7);
    assertThat(list.get(0)).isInstanceOf(BorderNumber.class);
    BorderNumber borderNumber1 = (BorderNumber) list.get(1);
    assertThat(borderNumber1.getNumber()).isEqualTo("2");
    assertThat(borderNumber1.getNumId()).isEqualTo(8);
    assertThat(output).contains("Unexpected case of a new numId");
  }

  @Test
  void testPackList_withEmptyList_shouldReturnEmptyList() {
    List<DocumentUnitDocx> documentUnitDocxList = new ArrayList<>();

    List<DocumentUnitDocx> packedList = DocumentUnitDocxListUtils.packList(documentUnitDocxList);

    assertThat(packedList).isEmpty();
  }

  @Test
  void testPackList_withNull_shouldReturnEmptyList() {

    List<DocumentUnitDocx> packedList = DocumentUnitDocxListUtils.packList(null);

    assertThat(packedList).isEmpty();
  }

  @Test
  void testPackList_withOneParagraph_shouldNotChangeList() {
    List<DocumentUnitDocx> documentUnitDocxList = new ArrayList<>();
    documentUnitDocxList.add(createParagraphWithTextElement("paragraph text"));

    List<DocumentUnitDocx> packedList = DocumentUnitDocxListUtils.packList(documentUnitDocxList);

    assertThat(packedList).hasSize(1);
    assertThat(packedList.get(0)).isInstanceOf(ParagraphElement.class);
    assertThat(packedList.get(0).toHtmlString()).isEqualTo("<p>paragraph text</p>");
  }

  @Test
  void testPackList_withNumberingListEntries_shouldMergeThemIntoOneNumberingList() {
    List<DocumentUnitDocx> documentUnitDocxList = new ArrayList<>();
    documentUnitDocxList.add(createNumberingListEntry("entry 1", false, 0));
    documentUnitDocxList.add(createNumberingListEntry("entry 2", false, 0));

    List<DocumentUnitDocx> packedList = DocumentUnitDocxListUtils.packList(documentUnitDocxList);

    assertThat(packedList).hasSize(1);
    assertThat(packedList.get(0)).isInstanceOf(NumberingList.class);
    NumberingList numberingList = (NumberingList) packedList.get(0);
    assertThat(numberingList.getEntries()).hasSize(2);
    assertThat(numberingList.getEntries().get(0).toHtmlString()).isEqualTo("<p>entry 1</p>");
    assertThat(numberingList.getEntries().get(1).toHtmlString()).isEqualTo("<p>entry 2</p>");
  }

  @Test
  void
      testPackList_withOneRandnummerAndTwoParagraphs_shouldOnlyAddFirstParagraphToFinalRandnummer() {
    List<DocumentUnitDocx> documentUnitDocxList = new ArrayList<>();
    documentUnitDocxList.add(createBorderNumber(1)); // <-- final (and only) one in the document
    documentUnitDocxList.add(createParagraphWithTextElement("paragraph1 text"));
    documentUnitDocxList.add(createParagraphWithTextElement("paragraph2 text"));

    List<DocumentUnitDocx> packedList = DocumentUnitDocxListUtils.packList(documentUnitDocxList);

    assertThat(packedList).hasSize(2);
    assertThat(packedList.get(0)).isInstanceOf(BorderNumber.class);
    BorderNumber borderNumberBlock = (BorderNumber) packedList.get(0);
    assertThat(borderNumberBlock.getChildrenSize()).isEqualTo(1);
    assertThat(borderNumberBlock.getChildren().get(0)).isInstanceOf(ParagraphElement.class);
    assertThat(borderNumberBlock.getChildren().get(0).toHtmlString())
        .isEqualTo("<p>paragraph1 text</p>");
    assertThat(packedList.get(1)).isInstanceOf(ParagraphElement.class);
    ParagraphElement paragraphElement = (ParagraphElement) packedList.get(1);
    assertThat(paragraphElement.toHtmlString()).isEqualTo("<p>paragraph2 text</p>");
  }

  @Test
  void testPackList_withTwoRandnummernAndParagraphs_headlineShouldResetParentRandnummer() {
    List<DocumentUnitDocx> documentUnitDocxList = new ArrayList<>();
    documentUnitDocxList.add(createBorderNumber(1));
    documentUnitDocxList.add(createParagraphWithTextElement("paragraph before headline"));
    documentUnitDocxList.add(createCenteredParagraphWithTextElement("||."));
    documentUnitDocxList.add(createParagraphWithTextElement("paragraph after headline"));
    // just so that the first Randnummer is not the final one in the document:
    documentUnitDocxList.add(createBorderNumber(2));

    List<DocumentUnitDocx> packedList = DocumentUnitDocxListUtils.packList(documentUnitDocxList);

    assertThat(packedList).hasSize(4);
    assertThat(packedList.get(0)).isInstanceOf(BorderNumber.class);
    BorderNumber borderNumberBlock = (BorderNumber) packedList.get(0);
    assertThat(borderNumberBlock.getChildrenSize()).isEqualTo(1);
    assertThat(borderNumberBlock.getChildren().get(0)).isInstanceOf(ParagraphElement.class);
    assertThat(borderNumberBlock.getChildren().get(0).toHtmlString())
        .isEqualTo("<p>paragraph before headline</p>");
    assertThat(packedList.get(1)).isInstanceOf(ParagraphElement.class);
    ParagraphElement headlineParagraphElement = (ParagraphElement) packedList.get(1);
    assertThat(headlineParagraphElement.toHtmlString())
        .isEqualTo("<p style=\"text-align: center;\">||.</p>");
    assertThat(packedList.get(2)).isInstanceOf(ParagraphElement.class);
    ParagraphElement paragraphElement = (ParagraphElement) packedList.get(2);
    assertThat(paragraphElement.toHtmlString()).isEqualTo("<p>paragraph after headline</p>");
    assertThat(packedList.get(3)).isInstanceOf(BorderNumber.class);
  }

  @Test
  void
      testPackList_withTwoRandnummernAndAParagraphAndNumberingListEntries_shouldCreateCorrectHierarchy() {
    List<DocumentUnitDocx> documentUnitDocxList = new ArrayList<>();
    documentUnitDocxList.add(createBorderNumber(1));
    documentUnitDocxList.add(createParagraphWithTextElement("paragraph text"));
    documentUnitDocxList.add(createNumberingListEntry("entry 1", false, 0));
    documentUnitDocxList.add(createNumberingListEntry("entry 2", false, 0));
    documentUnitDocxList.add(createBorderNumber(2)); // <-- final one in the document

    List<DocumentUnitDocx> packedList = DocumentUnitDocxListUtils.packList(documentUnitDocxList);

    assertThat(packedList).hasSize(2);
    assertThat(packedList.get(0)).isInstanceOf(BorderNumber.class);
    assertThat(packedList.get(1)).isInstanceOf(BorderNumber.class);
    BorderNumber borderNumberBlock1 = (BorderNumber) packedList.get(0);
    assertThat(borderNumberBlock1.getNumber()).isEqualTo("1");
    assertThat(borderNumberBlock1.getChildrenSize()).isEqualTo(2);
    assertThat(borderNumberBlock1.getChildren().get(0)).isInstanceOf(ParagraphElement.class);
    assertThat(borderNumberBlock1.getChildren().get(0).toHtmlString())
        .isEqualTo("<p>paragraph text</p>");
    assertThat(borderNumberBlock1.getChildren().get(1)).isInstanceOf(NumberingList.class);
    NumberingList numberingList = (NumberingList) borderNumberBlock1.getChildren().get(1);
    assertThat(numberingList.getEntries()).hasSize(2);
    assertThat(numberingList.getEntries().get(0).toHtmlString()).isEqualTo("<p>entry 1</p>");
    assertThat(numberingList.getEntries().get(1).toHtmlString()).isEqualTo("<p>entry 2</p>");
  }

  private ParagraphElement createParagraphWithTextElement(String text) {
    ParagraphElement paragraph = new ParagraphElement();
    RunTextElement textElement = new RunTextElement();
    textElement.setText(text);
    paragraph.setRunElements(List.of(textElement));
    return paragraph;
  }

  private ParagraphElement createCenteredParagraphWithTextElement(String text) {
    ParagraphElement paragraph = new ParagraphElement();
    paragraph.addStyle(new Style("text-align", List.of("center")));
    RunTextElement textElement = new RunTextElement();
    textElement.setText(text);
    paragraph.setRunElements(List.of(textElement));
    return paragraph;
  }

  private BorderNumber createNumberlessBorderNumberWithNumId(int numId) {
    BorderNumber borderNumber = new BorderNumber();
    borderNumber.setNumId(numId);
    return borderNumber;
  }

  private BorderNumber createBorderNumber(int number) {
    BorderNumber borderNumber = new BorderNumber();
    borderNumber.addNumberText(String.valueOf(number));
    return borderNumber;
  }

  private NumberingListEntry createNumberingListEntry(String text, boolean isOrdered, int level) {
    String lvlText = "";
    String startVal = "1";
    String restartNumberingAfterBreak = "";
    String color = "";
    String fontStyle = "";
    String fontSize = "";
    boolean lvlPicBullet = false;
    boolean isLgl = false;
    DocumentUnitNumberingListNumberFormat numberFormat =
        isOrdered
            ? DocumentUnitNumberingListNumberFormat.DECIMAL
            : DocumentUnitNumberingListNumberFormat.BULLET;
    String iLvl = String.valueOf(level);
    JcEnumeration lvlJc = JcEnumeration.RIGHT;
    String suff = "space";
    return new NumberingListEntry(
        createParagraphWithTextElement(text),
        new NumberingListEntryIndex(
            lvlText,
            startVal,
            restartNumberingAfterBreak,
            color,
            fontStyle,
            fontSize,
            lvlPicBullet,
            isLgl,
            numberFormat,
            iLvl,
            lvlJc,
            suff));
  }
}
