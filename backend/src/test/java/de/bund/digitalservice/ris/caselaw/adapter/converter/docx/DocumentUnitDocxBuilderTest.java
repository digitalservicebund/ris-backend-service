package de.bund.digitalservice.ris.caselaw.adapter.converter.docx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import de.bund.digitalservice.ris.caselaw.TestMemoryAppender;
import de.bund.digitalservice.ris.caselaw.domain.docx.AnchorImageElement;
import de.bund.digitalservice.ris.caselaw.domain.docx.BorderNumber;
import de.bund.digitalservice.ris.caselaw.domain.docx.DocxImagePart;
import de.bund.digitalservice.ris.caselaw.domain.docx.ErrorRunElement;
import de.bund.digitalservice.ris.caselaw.domain.docx.InlineImageElement;
import de.bund.digitalservice.ris.caselaw.domain.docx.NumberingListEntry;
import de.bund.digitalservice.ris.caselaw.domain.docx.ParagraphElement;
import de.bund.digitalservice.ris.caselaw.domain.docx.RunTextElement;
import jakarta.xml.bind.JAXBElement;
import java.awt.Dimension;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import org.docx4j.dml.CTBlip;
import org.docx4j.dml.CTBlipFillProperties;
import org.docx4j.dml.CTNonVisualDrawingProps;
import org.docx4j.dml.CTPositiveSize2D;
import org.docx4j.dml.Graphic;
import org.docx4j.dml.GraphicData;
import org.docx4j.dml.picture.Pic;
import org.docx4j.dml.wordprocessingDrawing.Anchor;
import org.docx4j.dml.wordprocessingDrawing.CTPosH;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.dml.wordprocessingDrawing.STAlignH;
import org.docx4j.model.listnumbering.AbstractListNumberingDefinition;
import org.docx4j.model.listnumbering.ListLevel;
import org.docx4j.model.listnumbering.ListNumberingDefinition;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTFramePr;
import org.docx4j.wml.CTVerticalAlignRun;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.Jc;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.NumberFormat;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.PPrBase.NumPr;
import org.docx4j.wml.PPrBase.NumPr.Ilvl;
import org.docx4j.wml.PPrBase.NumPr.NumId;
import org.docx4j.wml.PPrBase.Spacing;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STVerticalAlignRun;
import org.docx4j.wml.Style;
import org.docx4j.wml.Text;
import org.docx4j.wml.U;
import org.docx4j.wml.UnderlineEnumeration;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class DocumentUnitDocxBuilderTest {
  @Test
  void test_withoutConvertableElements() {
    DocumentUnitDocxBuilder builder = DocumentUnitDocxBuilder.newInstance();
    var result = builder.build();

    assertNull(result);
  }

  @Test
  void testSetParagraph() {
    DocumentUnitDocxBuilder builder = DocumentUnitDocxBuilder.newInstance();
    P paragraph = new P();

    var returnedBuilder = builder.setParagraph(paragraph);

    assertEquals(builder, returnedBuilder);
    assertEquals(paragraph, returnedBuilder.paragraph);
  }

  @Test
  void testBuild_withBorderNumber() {
    DocumentUnitDocxBuilder builder = DocumentUnitDocxBuilder.newInstance();
    P paragraph = new P();
    PPr pPr = new PPr();
    PPrBase.PStyle pStyle = new PPrBase.PStyle();
    pStyle.setVal("RandNummer");
    pPr.setPStyle(pStyle);
    paragraph.setPPr(pPr);
    R run = new R();
    Text text = new Text();
    text.setValue("1");
    JAXBElement<Text> element = new JAXBElement<>(new QName("text"), Text.class, text);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    var result = builder.setParagraph(paragraph).build();

    assertTrue(result instanceof BorderNumber);
    var borderNumberElement = (BorderNumber) result;
    assertEquals("1", borderNumberElement.getNumber());

    var htmlString = borderNumberElement.toHtmlString();
    assertEquals("<border-number><number>1</number></border-number>", htmlString);
  }

  @Test
  void testBuild_withListParagraphBorderNumber_shouldSucceed() {
    DocumentUnitDocxBuilder builder = DocumentUnitDocxBuilder.newInstance();
    P paragraph = new P();
    PPr pPr = new PPr();
    PPrBase.PStyle pStyle = new PPrBase.PStyle();
    pStyle.setVal("ListParagraph");
    pPr.setPStyle(pStyle);
    paragraph.setPPr(pPr);

    var result = builder.setParagraph(paragraph).build();

    assertTrue(result instanceof BorderNumber);
    // it won't have a number assigned because postprocessing is not done, see the
    // DocumentUnitDocxListUtilsTest for testing the postprocessing of border numbers
  }

  @Test
  void testBuild_withListenabsatzBorderNumber_shouldSucceed() {
    DocumentUnitDocxBuilder builder = DocumentUnitDocxBuilder.newInstance();
    P paragraph = new P();
    PPr pPr = new PPr();
    PPrBase.PStyle pStyle = new PPrBase.PStyle();
    pStyle.setVal("Listenabsatz");
    pPr.setPStyle(pStyle);
    paragraph.setPPr(pPr);
    CTFramePr framePr = new CTFramePr();
    pPr.setFramePr(framePr);

    var result = builder.setParagraph(paragraph).build();

    assertTrue(result instanceof BorderNumber);
  }

  @Test
  void testBuild_withBorderNumberThatHasNoBorderNumberTemplateStyle_shouldSucceed() {
    DocumentUnitDocxBuilder builder = DocumentUnitDocxBuilder.newInstance();
    P paragraph = new P();
    PPr pPr = new PPr();
    BooleanDefaultTrue keepNext = new BooleanDefaultTrue();
    keepNext.setVal(true);
    pPr.setKeepNext(keepNext); // indicator 1
    Spacing spacing = new Spacing();
    spacing.setLine(BigInteger.valueOf(240L)); // indicator 2
    pPr.setSpacing(spacing);
    paragraph.setPPr(pPr);
    R run = new R();
    Text text = new Text();
    text.setValue("1"); // indicator 3: just one integer
    run.getContent().add(new JAXBElement<>(new QName("text"), Text.class, text));
    paragraph.getContent().add(run);

    var result = builder.setParagraph(paragraph).build();

    assertTrue(result instanceof BorderNumber);
    var borderNumberElement = (BorderNumber) result;
    assertEquals("1", borderNumberElement.getNumber());
    assertEquals(
        "<border-number><number>1</number></border-number>", borderNumberElement.toHtmlString());
  }

  @Test
  void testBuild_withBorderNumberThatHasNoBorderNumberTemplateStyle_wrongTextShouldFail() {
    DocumentUnitDocxBuilder builder = DocumentUnitDocxBuilder.newInstance();
    P paragraph = new P();
    PPr pPr = new PPr();
    BooleanDefaultTrue keepNext = new BooleanDefaultTrue();
    keepNext.setVal(true);
    pPr.setKeepNext(keepNext);
    Spacing spacing = new Spacing();
    spacing.setLine(BigInteger.valueOf(240L));
    pPr.setSpacing(spacing);
    paragraph.setPPr(pPr);
    R run = new R();
    Text text = new Text();
    text.setValue("1."); // can't be parsed as integer
    run.getContent().add(new JAXBElement<>(new QName("text"), Text.class, text));
    paragraph.getContent().add(run);

    var result = builder.setParagraph(paragraph).build();

    assertTrue(result instanceof ParagraphElement);
    var paragraphElement = (ParagraphElement) result;
    assertEquals("<p>1.</p>", paragraphElement.toHtmlString());
  }

  @Test
  void testBuild_withText() {
    DocumentUnitDocxBuilder builder = DocumentUnitDocxBuilder.newInstance();
    P paragraph = new P();

    R wrongRun = new R();
    wrongRun.getContent().add(new Object());
    paragraph.getContent().add(wrongRun);

    R run = new R();
    Text text = new Text();
    text.setValue("text");
    JAXBElement<Text> element = new JAXBElement<>(new QName("text"), Text.class, text);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    var result = builder.setParagraph(paragraph).build();

    assertTrue(result instanceof ParagraphElement);
    var paragraphElement = (ParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(RunTextElement.class, runElement.getClass());
    assertEquals("text", ((RunTextElement) runElement).getText());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p>text</p>", htmlString);
  }

  @Test
  void testBuild_withParagraphWithoutText() {
    DocumentUnitDocxBuilder builder = DocumentUnitDocxBuilder.newInstance();
    P paragraph = new P();
    R run = new R();
    paragraph.getContent().add(run);
    paragraph.getContent().add(new P.Hyperlink());

    var result = builder.setParagraph(paragraph).build();

    assertTrue(result instanceof ParagraphElement);
    var paragraphElement = (ParagraphElement) result;
    assertTrue(paragraphElement.getRunElements().isEmpty());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p></p>", htmlString);
  }

  @Test
  void testBuild_withTextAndParagraphAlignmentRight() {
    DocumentUnitDocxBuilder builder = DocumentUnitDocxBuilder.newInstance();
    P paragraph = new P();
    PPr pPr = new PPr();
    Jc jc = new Jc();
    jc.setVal(JcEnumeration.RIGHT);
    pPr.setJc(jc);
    paragraph.setPPr(pPr);
    R run = new R();
    Text text = new Text();
    text.setValue("text");
    JAXBElement<Text> element = new JAXBElement<>(new QName("text"), Text.class, text);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    var result = builder.setParagraph(paragraph).build();

    assertTrue(result instanceof ParagraphElement);
    ParagraphElement paragraphElement = (ParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(RunTextElement.class, runElement.getClass());
    assertEquals("text", ((RunTextElement) runElement).getText());
    assertTrue(paragraphElement.getStyleString().contains("text-align: right"));

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p style=\"text-align: right;\">text</p>", htmlString);
  }

  @Test
  void testBuild_withTextAndParagraphAlignmentLeft() {
    DocumentUnitDocxBuilder builder = DocumentUnitDocxBuilder.newInstance();
    P paragraph = new P();
    PPr pPr = new PPr();
    Jc jc = new Jc();
    jc.setVal(JcEnumeration.LEFT);
    pPr.setJc(jc);
    paragraph.setPPr(pPr);
    R run = new R();
    Text text = new Text();
    text.setValue("text");
    JAXBElement<Text> element = new JAXBElement<>(new QName("text"), Text.class, text);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    var result = builder.setParagraph(paragraph).build();

    assertTrue(result instanceof ParagraphElement);
    ParagraphElement paragraphElement = (ParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(RunTextElement.class, runElement.getClass());
    assertEquals("text", ((RunTextElement) runElement).getText());
    assertTrue(paragraphElement.getStyleString().contains("text-align: left"));

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p style=\"text-align: left;\">text</p>", htmlString);
  }

  @Test
  void testBuild_withTextAndParagraphAlignmentJustify() {
    DocumentUnitDocxBuilder builder = DocumentUnitDocxBuilder.newInstance();
    P paragraph = new P();
    PPr pPr = new PPr();
    Jc jc = new Jc();
    jc.setVal(JcEnumeration.BOTH);
    pPr.setJc(jc);
    paragraph.setPPr(pPr);
    R run = new R();
    Text text = new Text();
    text.setValue("text");
    JAXBElement<Text> element = new JAXBElement<>(new QName("text"), Text.class, text);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    var result = builder.setParagraph(paragraph).build();

    assertTrue(result instanceof ParagraphElement);
    ParagraphElement paragraphElement = (ParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(RunTextElement.class, runElement.getClass());
    assertEquals("text", ((RunTextElement) runElement).getText());
    assertTrue(paragraphElement.getStyleString().contains("text-align: justify"));

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p style=\"text-align: justify;\">text</p>", htmlString);
  }

  @Test
  void testBuild_withTextAndParagraphAlignmentCenter() {
    DocumentUnitDocxBuilder builder = DocumentUnitDocxBuilder.newInstance();
    P paragraph = new P();
    PPr pPr = new PPr();
    Jc jc = new Jc();
    jc.setVal(JcEnumeration.CENTER);
    pPr.setJc(jc);
    paragraph.setPPr(pPr);
    R run = new R();
    Text text = new Text();
    text.setValue("text");
    JAXBElement<Text> element = new JAXBElement<>(new QName("text"), Text.class, text);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    var result = builder.setParagraph(paragraph).build();

    assertTrue(result instanceof ParagraphElement);
    ParagraphElement paragraphElement = (ParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(RunTextElement.class, runElement.getClass());
    assertEquals("text", ((RunTextElement) runElement).getText());
    assertTrue(paragraphElement.getStyleString().contains("text-align: center"));

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p style=\"text-align: center;\">text</p>", htmlString);
  }

  @Test
  void testBuild_withTextAndSize() {
    DocumentUnitDocxBuilder builder = DocumentUnitDocxBuilder.newInstance();
    P paragraph = new P();

    RPr rPr = new RPr();
    HpsMeasure size = new HpsMeasure();
    size.setVal(new BigInteger("48"));
    rPr.setSz(size);
    R run = new R();
    run.setRPr(rPr);
    Text text = new Text();
    text.setValue("text");
    JAXBElement<Text> element = new JAXBElement<>(new QName("text"), Text.class, text);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    rPr = new RPr();
    size = new HpsMeasure();
    size.setVal(new BigInteger("21"));
    rPr.setSz(size);
    run = new R();
    run.setRPr(rPr);
    text = new Text();
    text.setValue("text2");
    element = new JAXBElement<>(new QName("text"), Text.class, text);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    var result = builder.setParagraph(paragraph).build();

    assertTrue(result instanceof ParagraphElement);
    ParagraphElement paragraphElement = (ParagraphElement) result;
    assertEquals(2, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(RunTextElement.class, runElement.getClass());
    var runTextElement = ((RunTextElement) runElement);
    assertEquals("text", runTextElement.getText());
    assertTrue(runTextElement.getStyleString().contains("font-size: 24pt"));

    var htmlString = paragraphElement.toHtmlString();
    assertEquals(
        "<p><span style=\"font-size: 24pt;\">text</span><span style=\"font-size: 10.5pt;\">text2</span></p>",
        htmlString);
  }

  @Test
  void testBuild_withTextAndWeight() {
    DocumentUnitDocxBuilder builder = DocumentUnitDocxBuilder.newInstance();
    P paragraph = new P();
    RPr rPr = new RPr();
    BooleanDefaultTrue bold = new BooleanDefaultTrue();
    bold.setVal(true);
    rPr.setB(bold);
    R run = new R();
    run.setRPr(rPr);
    Text text = new Text();
    text.setValue("text");
    JAXBElement<Text> element = new JAXBElement<>(new QName("text"), Text.class, text);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    var result = builder.setParagraph(paragraph).build();

    assertTrue(result instanceof ParagraphElement);
    ParagraphElement paragraphElement = (ParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(RunTextElement.class, runElement.getClass());
    var runTextElement = (RunTextElement) runElement;
    assertEquals("text", runTextElement.getText());
    assertTrue(runTextElement.getStyleString().contains("font-weight: bold"));

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p><span style=\"font-weight: bold;\">text</span></p>", htmlString);
  }

  @Test
  void testBuild_withTextAndItalic() {
    DocumentUnitDocxBuilder builder = DocumentUnitDocxBuilder.newInstance();
    P paragraph = new P();
    RPr rPr = new RPr();
    BooleanDefaultTrue italic = new BooleanDefaultTrue();
    italic.setVal(true);
    rPr.setI(italic);
    R run = new R();
    run.setRPr(rPr);
    Text text = new Text();
    text.setValue("text");
    JAXBElement<Text> element = new JAXBElement<>(new QName("text"), Text.class, text);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    var result = builder.setParagraph(paragraph).build();

    assertTrue(result instanceof ParagraphElement);
    ParagraphElement paragraphElement = (ParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(RunTextElement.class, runElement.getClass());
    var runTextElement = (RunTextElement) runElement;
    assertEquals("text", runTextElement.getText());
    assertTrue(runTextElement.getStyleString().contains("font-style: italic"));

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p><span style=\"font-style: italic;\">text</span></p>", htmlString);
  }

  @Test
  void testBuild_withTextAndStrike() {
    DocumentUnitDocxBuilder builder = DocumentUnitDocxBuilder.newInstance();
    P paragraph = new P();
    RPr rPr = new RPr();
    BooleanDefaultTrue strike = new BooleanDefaultTrue();
    strike.setVal(true);
    rPr.setStrike(strike);
    R run = new R();
    run.setRPr(rPr);
    Text text = new Text();
    text.setValue("text");
    JAXBElement<Text> element = new JAXBElement<>(new QName("text"), Text.class, text);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    var result = builder.setParagraph(paragraph).build();

    assertTrue(result instanceof ParagraphElement);
    ParagraphElement paragraphElement = (ParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(RunTextElement.class, runElement.getClass());
    var runTextElement = (RunTextElement) runElement;
    assertEquals("text", runTextElement.getText());
    assertTrue(runTextElement.getStyleString().contains("text-decoration: line-through"));

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p><span style=\"text-decoration: line-through;\">text</span></p>", htmlString);
  }

  @Test
  void testBuild_withTextAndUnderline() {
    DocumentUnitDocxBuilder builder = DocumentUnitDocxBuilder.newInstance();
    P paragraph = new P();
    RPr rPr = new RPr();
    U underline = new U();
    underline.setVal(UnderlineEnumeration.SINGLE);
    rPr.setU(underline);
    R run = new R();
    run.setRPr(rPr);
    Text text = new Text();
    text.setValue("text");
    JAXBElement<Text> element = new JAXBElement<>(new QName("text"), Text.class, text);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    var result = builder.setParagraph(paragraph).build();

    assertTrue(result instanceof ParagraphElement);
    ParagraphElement paragraphElement = (ParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(RunTextElement.class, runElement.getClass());
    var runTextElement = (RunTextElement) runElement;
    assertEquals("text", runTextElement.getText());
    assertTrue(runTextElement.getStyleString().contains("text-decoration: underline"));

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p><span style=\"text-decoration: underline;\">text</span></p>", htmlString);
  }

  @Test
  void testBuild_withTextAndSubscript() {
    DocumentUnitDocxBuilder builder = DocumentUnitDocxBuilder.newInstance();
    P paragraph = new P();

    RPr rPr = new RPr();
    CTVerticalAlignRun vertAlign = new CTVerticalAlignRun();
    vertAlign.setVal(STVerticalAlignRun.SUBSCRIPT);
    rPr.setVertAlign(vertAlign);
    R run = new R();
    run.setRPr(rPr);
    Text text = new Text();
    text.setValue("text");
    JAXBElement<Text> element = new JAXBElement<>(new QName("text"), Text.class, text);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    var result = builder.setParagraph(paragraph).build();

    assertTrue(result instanceof ParagraphElement);
    ParagraphElement paragraphElement = (ParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(RunTextElement.class, runElement.getClass());
    var runTextElement = ((RunTextElement) runElement);
    assertEquals("text", runTextElement.getText());
    assertTrue(runTextElement.getStyleString().contains("vertical-align: sub"));

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p><span style=\"vertical-align: sub;\">text</span></p>", htmlString);
  }

  @Test
  void testBuild_withTextAndSuperscript() {
    DocumentUnitDocxBuilder builder = DocumentUnitDocxBuilder.newInstance();
    P paragraph = new P();

    RPr rPr = new RPr();
    CTVerticalAlignRun vertAlign = new CTVerticalAlignRun();
    vertAlign.setVal(STVerticalAlignRun.SUPERSCRIPT);
    rPr.setVertAlign(vertAlign);
    R run = new R();
    run.setRPr(rPr);
    Text text = new Text();
    text.setValue("text");
    JAXBElement<Text> element = new JAXBElement<>(new QName("text"), Text.class, text);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    var result = builder.setParagraph(paragraph).build();

    assertTrue(result instanceof ParagraphElement);
    ParagraphElement paragraphElement = (ParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(RunTextElement.class, runElement.getClass());
    var runTextElement = ((RunTextElement) runElement);
    assertEquals("text", runTextElement.getText());
    assertTrue(runTextElement.getStyleString().contains("vertical-align: super"));

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p><span style=\"vertical-align: super;\">text</span></p>", htmlString);
  }

  @Test
  void testBuild_withMultipleTextDecoration() {

    RPr rPr = new RPr();
    BooleanDefaultTrue strike = new BooleanDefaultTrue();
    strike.setVal(true);
    rPr.setStrike(strike);
    U underline = new U();
    underline.setVal(UnderlineEnumeration.SINGLE);
    rPr.setU(underline);

    P paragraph =
        TestDocxBuilder.newParagraphBuilder()
            .setRunElementStyle(rPr)
            .addRunElement(TestDocxBuilder.buildTextRunElement("text"))
            .build();

    var result = DocumentUnitDocxBuilder.newInstance().setParagraph(paragraph).build();

    assertTrue(result instanceof ParagraphElement);
    ParagraphElement paragraphElement = (ParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(RunTextElement.class, runElement.getClass());
    assertEquals("text", ((RunTextElement) runElement).getText());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals(
        "<p><span style=\"text-decoration: line-through underline;\">text</span></p>", htmlString);
  }

  @Test
  void testBuild_withRunElementStyles() {

    RPr rPr = new RPr();
    BooleanDefaultTrue strike = new BooleanDefaultTrue();
    strike.setVal(true);
    rPr.setStrike(strike);

    P paragraph =
        TestDocxBuilder.newParagraphBuilder()
            .setRunElementStyle(rPr)
            .addRunElement(TestDocxBuilder.buildTextRunElement("text"))
            .build();

    var result = DocumentUnitDocxBuilder.newInstance().setParagraph(paragraph).build();

    assertTrue(result instanceof ParagraphElement);
    ParagraphElement paragraphElement = (ParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(RunTextElement.class, runElement.getClass());
    var runTextElement = (RunTextElement) runElement;
    assertEquals("text", runTextElement.getText());
    assertTrue(runTextElement.getStyleString().contains("text-decoration: line-through"));

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p><span style=\"text-decoration: line-through;\">text</span></p>", htmlString);
  }

  @Test
  void testBuild_withExternalStyles() {
    Map<String, Style> styles = new HashMap<>();
    Style style = new Style();

    PPr pPr = new PPr();
    RPr rPr = new RPr();
    U underline = new U();
    underline.setVal(UnderlineEnumeration.SINGLE);
    rPr.setU(underline);

    style.setRPr(rPr);
    styles.put("external", style);
    PPrBase.PStyle pStyle = new PPrBase.PStyle();
    pStyle.setVal("external");
    pPr.setPStyle(pStyle);

    P paragraph =
        TestDocxBuilder.newParagraphBuilder()
            .addRunElement(TestDocxBuilder.buildTextRunElement("text"))
            .setParagraphStyle(pPr)
            .build();

    var result =
        DocumentUnitDocxBuilder.newInstance().setParagraph(paragraph).useStyles(styles).build();

    assertTrue(result instanceof ParagraphElement);
    ParagraphElement paragraphElement = (ParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(RunTextElement.class, runElement.getClass());
    var runTextElement = (RunTextElement) runElement;
    assertEquals("text", runTextElement.getText());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p><span style=\"text-decoration: underline;\">text</span></p>", htmlString);
  }

  @Test
  void testBuild_withMultipleTextBlocks() {
    P paragraph =
        TestDocxBuilder.newParagraphBuilder()
            .addRunElement(TestDocxBuilder.buildTextRunElement("run text 1"))
            .addRunElement(TestDocxBuilder.buildTextRunElement("run text 2"))
            .build();

    var result = DocumentUnitDocxBuilder.newInstance().setParagraph(paragraph).build();

    assertTrue(result instanceof ParagraphElement);
    ParagraphElement paragraphElement = (ParagraphElement) result;
    assertEquals(2, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(RunTextElement.class, runElement.getClass());
    var runTextElement = (RunTextElement) runElement;
    assertEquals("run text 1", runTextElement.getText());
    runElement = paragraphElement.getRunElements().get(1);
    assertEquals(RunTextElement.class, runElement.getClass());
    runTextElement = (RunTextElement) runElement;
    assertEquals("run text 2", runTextElement.getText());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p>run text 1run text 2</p>", htmlString);
  }

  @Test
  void testBuild_withInlineImage() {
    DocumentUnitDocxBuilder builder = DocumentUnitDocxBuilder.newInstance();
    P paragraph = new P();
    R run = new R();
    Drawing drawing = new Drawing();
    drawing.getAnchorOrInline().add(generateInline(null, null, null));
    JAXBElement<Drawing> element = new JAXBElement<>(new QName("drawing"), Drawing.class, drawing);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    HashMap<String, DocxImagePart> images = new HashMap<>();
    DocxImagePart image = new DocxImagePart("content-type", new byte[] {1, 2});
    images.put("image-ref", image);

    var result = builder.setParagraph(paragraph).useImages(images).build();

    assertTrue(result instanceof ParagraphElement);
    ParagraphElement paragraphElement = (ParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(InlineImageElement.class, runElement.getClass());
    var runImageElement = (InlineImageElement) runElement;
    assertEquals("content-type", runImageElement.getContentType());
    assertEquals("AQI=", runImageElement.getBase64Representation());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p><img src=\"data:content-type;base64, AQI=\" /></p>", htmlString);
  }

  @Test
  void testBuild_withInlineImage_withAlternateText() {
    DocumentUnitDocxBuilder builder = DocumentUnitDocxBuilder.newInstance();
    P paragraph = new P();
    R run = new R();
    Drawing drawing = new Drawing();
    drawing.getAnchorOrInline().add(generateInline("name", "description", null));
    JAXBElement<Drawing> element = new JAXBElement<>(new QName("drawing"), Drawing.class, drawing);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    HashMap<String, DocxImagePart> images = new HashMap<>();
    DocxImagePart image = new DocxImagePart("content-type", new byte[] {1, 2});
    images.put("image-ref", image);

    var result = builder.setParagraph(paragraph).useImages(images).build();

    assertTrue(result instanceof ParagraphElement);
    ParagraphElement paragraphElement = (ParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(InlineImageElement.class, runElement.getClass());
    var runImageElement = (InlineImageElement) runElement;
    assertEquals("content-type", runImageElement.getContentType());
    assertEquals("AQI=", runImageElement.getBase64Representation());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals(
        "<p><img src=\"data:content-type;base64, AQI=\" alt=\"namedescription\" /></p>",
        htmlString);
  }

  @Test
  void testBuild_withInlineImage_withSize() {
    DocumentUnitDocxBuilder builder = DocumentUnitDocxBuilder.newInstance();
    P paragraph = new P();
    R run = new R();
    Drawing drawing = new Drawing();
    drawing.getAnchorOrInline().add(generateInline(null, null, new Dimension(10, 10)));
    JAXBElement<Drawing> element = new JAXBElement<>(new QName("drawing"), Drawing.class, drawing);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    HashMap<String, DocxImagePart> images = new HashMap<>();
    DocxImagePart image = new DocxImagePart("content-type", new byte[] {1, 2});
    images.put("image-ref", image);

    var result = builder.setParagraph(paragraph).useImages(images).build();

    assertTrue(result instanceof ParagraphElement);
    ParagraphElement paragraphElement = (ParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(InlineImageElement.class, runElement.getClass());
    var runImageElement = (InlineImageElement) runElement;
    assertEquals("content-type", runImageElement.getContentType());
    assertEquals("AQI=", runImageElement.getBase64Representation());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals(
        "<p><img src=\"data:content-type;base64, AQI=\" width=\"10\" height=\"10\" /></p>",
        htmlString);
  }

  @Test
  void testBuild_withInlineImage_withAlternateTextAndSize() {
    DocumentUnitDocxBuilder builder = DocumentUnitDocxBuilder.newInstance();
    P paragraph = new P();
    R run = new R();
    Drawing drawing = new Drawing();
    drawing.getAnchorOrInline().add(generateInline("name", "description", new Dimension(10, 10)));
    JAXBElement<Drawing> element = new JAXBElement<>(new QName("drawing"), Drawing.class, drawing);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    HashMap<String, DocxImagePart> images = new HashMap<>();
    DocxImagePart image = new DocxImagePart("content-type", new byte[] {1, 2});
    images.put("image-ref", image);

    var result = builder.setParagraph(paragraph).useImages(images).build();

    assertTrue(result instanceof ParagraphElement);
    ParagraphElement paragraphElement = (ParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(InlineImageElement.class, runElement.getClass());
    var runImageElement = (InlineImageElement) runElement;
    assertEquals("content-type", runImageElement.getContentType());
    assertEquals("AQI=", runImageElement.getBase64Representation());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals(
        "<p><img src=\"data:content-type;base64, AQI=\" alt=\"namedescription\" width=\"10\" height=\"10\" /></p>",
        htmlString);
  }

  @Test
  void testBuild_withAnchorGraphic() {
    DocumentUnitDocxBuilder builder = DocumentUnitDocxBuilder.newInstance();
    P paragraph = new P();
    R run = new R();
    Drawing drawing = new Drawing();
    drawing.getAnchorOrInline().add(generateAnchor(null, null, null, null));
    JAXBElement<Drawing> element = new JAXBElement<>(new QName("drawing"), Drawing.class, drawing);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    HashMap<String, DocxImagePart> images = new HashMap<>();
    DocxImagePart image = new DocxImagePart("content-type", new byte[] {1, 2});
    images.put("image-ref", image);

    var result = builder.setParagraph(paragraph).useImages(images).build();

    assertTrue(result instanceof ParagraphElement);
    ParagraphElement paragraphElement = (ParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(AnchorImageElement.class, runElement.getClass());
    var runImageElement = (AnchorImageElement) runElement;
    assertEquals("content-type", runImageElement.getContentType());
    assertEquals("AQI=", runImageElement.getBase64Representation());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p><img src=\"data:content-type;base64, AQI=\" /></p>", htmlString);
  }

  @Test
  void testBuild_withAnchorGraphic_withLeftFloating() {
    DocumentUnitDocxBuilder builder = DocumentUnitDocxBuilder.newInstance();
    P paragraph = new P();
    R run = new R();
    Drawing drawing = new Drawing();
    drawing.getAnchorOrInline().add(generateAnchor(null, null, null, STAlignH.LEFT));
    JAXBElement<Drawing> element = new JAXBElement<>(new QName("drawing"), Drawing.class, drawing);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    HashMap<String, DocxImagePart> images = new HashMap<>();
    DocxImagePart image = new DocxImagePart("content-type", new byte[] {1, 2});
    images.put("image-ref", image);

    var result = builder.setParagraph(paragraph).useImages(images).build();

    assertTrue(result instanceof ParagraphElement);
    ParagraphElement paragraphElement = (ParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(AnchorImageElement.class, runElement.getClass());
    var runImageElement = (AnchorImageElement) runElement;
    assertEquals("content-type", runImageElement.getContentType());
    assertEquals("AQI=", runImageElement.getBase64Representation());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals(
        "<p class=\"clearfix\"><img src=\"data:content-type;base64, AQI=\" style=\"float: left;\" /></p>",
        htmlString);
  }

  @Test
  void testBuild_withAnchorGraphic_withRightFloating() {
    DocumentUnitDocxBuilder builder = DocumentUnitDocxBuilder.newInstance();
    P paragraph = new P();
    R run = new R();
    Drawing drawing = new Drawing();
    drawing.getAnchorOrInline().add(generateAnchor(null, null, null, STAlignH.RIGHT));
    JAXBElement<Drawing> element = new JAXBElement<>(new QName("drawing"), Drawing.class, drawing);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    HashMap<String, DocxImagePart> images = new HashMap<>();
    DocxImagePart image = new DocxImagePart("content-type", new byte[] {1, 2});
    images.put("image-ref", image);

    var result = builder.setParagraph(paragraph).useImages(images).build();

    assertTrue(result instanceof ParagraphElement);
    ParagraphElement paragraphElement = (ParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(AnchorImageElement.class, runElement.getClass());
    var runImageElement = (AnchorImageElement) runElement;
    assertEquals("content-type", runImageElement.getContentType());
    assertEquals("AQI=", runImageElement.getBase64Representation());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals(
        "<p class=\"clearfix\"><img src=\"data:content-type;base64, AQI=\" style=\"float: right;\" /></p>",
        htmlString);
  }

  @Test
  void testBuild_withAnchorGraphic_withUnknownFloating() {
    DocumentUnitDocxBuilder builder = DocumentUnitDocxBuilder.newInstance();
    P paragraph = new P();
    R run = new R();
    Drawing drawing = new Drawing();
    drawing.getAnchorOrInline().add(generateAnchor(null, null, null, STAlignH.CENTER));
    JAXBElement<Drawing> element = new JAXBElement<>(new QName("drawing"), Drawing.class, drawing);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    HashMap<String, DocxImagePart> images = new HashMap<>();
    DocxImagePart image = new DocxImagePart("content-type", new byte[] {1, 2});
    images.put("image-ref", image);

    var result = builder.setParagraph(paragraph).useImages(images).build();

    assertTrue(result instanceof ParagraphElement);
    ParagraphElement paragraphElement = (ParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(ErrorRunElement.class, runElement.getClass());
    var errorElement = (ErrorRunElement) runElement;

    assertEquals(
        "unknown run element: anchor image with unknown alignment: center",
        errorElement.toString());
  }

  @Test
  void testBuild_withMultipleGraphicObjects() {
    DocumentUnitDocxBuilder builder = DocumentUnitDocxBuilder.newInstance();
    P paragraph = new P();
    R run = new R();
    Drawing drawing = new Drawing();
    drawing.getAnchorOrInline().add(generateAnchor(null, null, null, null));
    drawing.getAnchorOrInline().add(generateInline(null, null, null));
    JAXBElement<Drawing> element = new JAXBElement<>(new QName("drawing"), Drawing.class, drawing);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    builder = builder.setParagraph(paragraph);
    Exception exception = assertThrows(DocxConverterException.class, builder::build);

    assertEquals("more than one graphic data in a drawing", exception.getMessage());
  }

  @Test
  void testBuild_withInlineImageWithoutGraphicData() {
    DocumentUnitDocxBuilder builder = DocumentUnitDocxBuilder.newInstance();
    P paragraph = new P();
    R run = new R();
    Drawing drawing = new Drawing();
    drawing.getAnchorOrInline().add(new Inline());
    JAXBElement<Drawing> element = new JAXBElement<>(new QName("drawing"), Drawing.class, drawing);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    builder = builder.setParagraph(paragraph);
    Exception exception = assertThrows(DocxConverterException.class, builder::build);

    assertEquals("no graphic data", exception.getMessage());
  }

  @Test
  void testBuild_withNumberingList() {
    DocumentUnitDocxBuilder builder = DocumentUnitDocxBuilder.newInstance();
    PPr pPr = new PPr();
    NumPr numPr = new NumPr();
    NumId numId = new NumId();
    numId.setVal(new BigInteger("0"));
    numPr.setNumId(numId);
    Ilvl ilvl = new Ilvl();
    ilvl.setVal(new BigInteger("0"));
    numPr.setIlvl(ilvl);
    pPr.setNumPr(numPr);
    P paragraph = new P();
    paragraph.setPPr(pPr);
    R run = new R();
    Text text = new Text();
    text.setValue("test text");
    JAXBElement<Text> element = new JAXBElement<>(new QName("text"), Text.class, text);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    var listNumberingDefinitions = new HashMap<String, ListNumberingDefinition>();
    var listNumberingDefinition = mock(ListNumberingDefinition.class);
    var abstractListDefinition = mock(AbstractListNumberingDefinition.class);
    var listLevel = mock(ListLevel.class);
    when(listLevel.getNumFmt()).thenReturn(NumberFormat.DECIMAL);
    when(listNumberingDefinition.getAbstractListDefinition()).thenReturn(abstractListDefinition);
    HashMap<String, ListLevel> listLevels = new HashMap<>();
    listLevels.put("0", listLevel);
    when(abstractListDefinition.getListLevels()).thenReturn(listLevels);
    listNumberingDefinitions.put("0", listNumberingDefinition);

    var result =
        builder
            .setParagraph(paragraph)
            .useListNumberingDefinitions(listNumberingDefinitions)
            .build();

    assertTrue(result instanceof NumberingListEntry);
    var numberingListEntry = (NumberingListEntry) result;
    assertNotNull(numberingListEntry.paragraphElement());
    ParagraphElement paragraphElement = (ParagraphElement) numberingListEntry.paragraphElement();
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(RunTextElement.class, runElement.getClass());

    var htmlString = numberingListEntry.toHtmlString();
    assertEquals("<p>test text</p>", htmlString);
  }

  @Test
  void testBuild_hasTheRightSorting() {
    P paragraph =
        TestDocxBuilder.newParagraphBuilder()
            .addRunElement(TestDocxBuilder.buildTextRunElement("test"))
            .addRunElement(TestDocxBuilder.buildAnchorImageElement())
            .addRunElement(TestDocxBuilder.buildTextRunElement("test 2"))
            .addRunElement(TestDocxBuilder.buildInlineImageElement())
            .addRunElement(TestDocxBuilder.buildTextRunElement("test 3"))
            .build();

    var result =
        DocumentUnitDocxBuilder.newInstance()
            .setParagraph(paragraph)
            .useImages(TestDocxBuilder.getImageMap())
            .build();

    assertThat(result).isInstanceOf(ParagraphElement.class);
    ParagraphElement paragraphElement = (ParagraphElement) result;
    assertThat(paragraphElement.getRunElements()).hasSize(5);
    assertThat(paragraphElement.getRunElements().get(0)).isInstanceOf(AnchorImageElement.class);
    assertThat(paragraphElement.getRunElements().get(1))
        .hasFieldOrPropertyWithValue("text", "test");
    assertThat(paragraphElement.getRunElements().get(2))
        .hasFieldOrPropertyWithValue("text", "test 2");
    assertThat(paragraphElement.getRunElements().get(3)).isInstanceOf(InlineImageElement.class);
    assertThat(paragraphElement.getRunElements().get(4))
        .hasFieldOrPropertyWithValue("text", "test 3");
  }

  @Test
  void testBuild_withVmlShape() {
    P paragraph =
        TestDocxBuilder.newParagraphBuilder()
            .addRunElement(TestDocxBuilder.buildVmlImage())
            .build();

    var result =
        DocumentUnitDocxBuilder.newInstance()
            .setParagraph(paragraph)
            .useImages(TestDocxBuilder.getImageMap())
            .build();

    assertThat(result).isInstanceOf(ParagraphElement.class);
    ParagraphElement paragraphElement = (ParagraphElement) result;
    assertThat(paragraphElement.getRunElements()).hasSize(1);
    assertThat(paragraphElement.getRunElements().get(0)).isInstanceOf(AnchorImageElement.class);
    AnchorImageElement imageElement = (AnchorImageElement) paragraphElement.getRunElements().get(0);
    assertThat(imageElement.getBase64Representation()).isEqualTo("dm1s");
    assertThat(imageElement.getContentType()).isEqualTo("vml-content-type");
  }

  @Test
  void testBuild_withUnknownRunElement_shouldHaveAnErrorRunElement() {
    R runElement = new R();
    runElement.getContent().add(new JAXBElement<>(new QName("error run"), String.class, "error"));
    P paragraph = TestDocxBuilder.newParagraphBuilder().addRunElement(runElement).build();

    var result = DocumentUnitDocxBuilder.newInstance().setParagraph(paragraph).build();

    assertThat(result).isInstanceOf(ParagraphElement.class);
    ParagraphElement paragraphElement = (ParagraphElement) result;
    assertThat(paragraphElement.getRunElements()).hasSize(1);
    assertThat(paragraphElement.getRunElements().get(0)).isInstanceOf(ErrorRunElement.class);
  }

  @Test
  void testBuild_withNumberingList_withNotAllowedNumberingFormat() {
    TestMemoryAppender memoryAppender = addLoggingTestAppender();
    DocumentUnitDocxBuilder builder = DocumentUnitDocxBuilder.newInstance();
    PPr pPr = new PPr();
    NumPr numPr = new NumPr();
    NumId numId = new NumId();
    numId.setVal(new BigInteger("0"));
    numPr.setNumId(numId);
    Ilvl ilvl = new Ilvl();
    ilvl.setVal(new BigInteger("0"));
    numPr.setIlvl(ilvl);
    pPr.setNumPr(numPr);
    P paragraph = new P();
    paragraph.setPPr(pPr);
    R run = new R();
    Text text = new Text();
    text.setValue("test text");
    JAXBElement<Text> element = new JAXBElement<>(new QName("text"), Text.class, text);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    var listNumberingDefinitions = new HashMap<String, ListNumberingDefinition>();
    var listNumberingDefinition = mock(ListNumberingDefinition.class);
    var abstractListDefinition = mock(AbstractListNumberingDefinition.class);
    var listLevel = mock(ListLevel.class);
    when(listLevel.getNumFmt()).thenReturn(NumberFormat.CHICAGO);
    when(listNumberingDefinition.getAbstractListDefinition()).thenReturn(abstractListDefinition);
    HashMap<String, ListLevel> listLevels = new HashMap<>();
    listLevels.put("0", listLevel);
    when(abstractListDefinition.getListLevels()).thenReturn(listLevels);
    listNumberingDefinitions.put("0", listNumberingDefinition);

    var result =
        builder
            .setParagraph(paragraph)
            .useListNumberingDefinitions(listNumberingDefinitions)
            .build();

    assertTrue(result instanceof NumberingListEntry);
    var numberingListEntry = (NumberingListEntry) result;
    assertNotNull(numberingListEntry.paragraphElement());
    ParagraphElement paragraphElement = (ParagraphElement) numberingListEntry.paragraphElement();
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(RunTextElement.class, runElement.getClass());

    var htmlString = numberingListEntry.toHtmlString();
    assertEquals("<p>test text</p>", htmlString);

    assertEquals(1, memoryAppender.count(Level.ERROR));
    assertEquals(
        "not implemented number format (CHICAGO) in list. use default bullet list",
        memoryAppender.getMessage(Level.ERROR, 0));

    detachLoggingTestAppender(memoryAppender);
  }

  private TestMemoryAppender addLoggingTestAppender() {
    TestMemoryAppender memoryAppender = new TestMemoryAppender();
    memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());

    Logger logger = (Logger) LoggerFactory.getLogger(DocumentUnitDocxBuilder.class);
    logger.addAppender(memoryAppender);

    memoryAppender.start();

    return memoryAppender;
  }

  private void detachLoggingTestAppender(TestMemoryAppender memoryAppender) {
    memoryAppender.stop();

    Logger logger = (Logger) LoggerFactory.getLogger(DocumentUnitDocxBuilder.class);
    logger.detachAppender(memoryAppender);
  }

  private Inline generateInline(String name, String description, Dimension size) {
    Inline inline = new Inline();

    if (name != null || description != null) {
      CTNonVisualDrawingProps nvDrawingProps = new CTNonVisualDrawingProps();
      nvDrawingProps.setName(name);
      nvDrawingProps.setDescr(description);
      inline.setDocPr(nvDrawingProps);
    }

    if (size != null) {
      CTPositiveSize2D positionSize2D = new CTPositiveSize2D();
      positionSize2D.setCx(size.width * 9525L);
      positionSize2D.setCy(size.height * 9525L);
      inline.setExtent(positionSize2D);
    }

    Graphic graphic = new Graphic();
    GraphicData graphicData = new GraphicData();
    Pic pic = new Pic();
    CTBlipFillProperties blibFill = new CTBlipFillProperties();
    CTBlip blib = new CTBlip();
    blib.setEmbed("image-ref");
    blibFill.setBlip(blib);
    pic.setBlipFill(blibFill);
    graphicData.getAny().add(pic);
    graphic.setGraphicData(graphicData);
    inline.setGraphic(graphic);

    return inline;
  }

  private Anchor generateAnchor(String name, String description, Dimension size, STAlignH alignH) {
    Anchor anchor = new Anchor();

    if (name != null || description != null) {
      CTNonVisualDrawingProps nvDrawingProps = new CTNonVisualDrawingProps();
      nvDrawingProps.setName(name);
      nvDrawingProps.setDescr(description);
      anchor.setDocPr(nvDrawingProps);
    }

    if (size != null) {
      CTPositiveSize2D positionSize2D = new CTPositiveSize2D();
      positionSize2D.setCx(size.width * 9525L);
      positionSize2D.setCy(size.height * 9525L);
      anchor.setExtent(positionSize2D);
    }

    if (alignH != null) {
      CTPosH posH = new CTPosH();
      posH.setAlign(alignH);
      anchor.setPositionH(posH);
    }

    Graphic graphic = new Graphic();
    GraphicData graphicData = new GraphicData();
    Pic pic = new Pic();
    CTBlipFillProperties blibFill = new CTBlipFillProperties();
    CTBlip blib = new CTBlip();
    blib.setEmbed("image-ref");
    blibFill.setBlip(blib);
    pic.setBlipFill(blibFill);
    graphicData.getAny().add(pic);
    graphic.setGraphicData(graphicData);
    anchor.setGraphic(graphic);

    return anchor;
  }
}
