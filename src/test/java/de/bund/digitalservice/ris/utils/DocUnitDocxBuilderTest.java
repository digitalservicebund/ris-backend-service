package de.bund.digitalservice.ris.utils;

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
import de.bund.digitalservice.ris.TestMemoryAppender;
import de.bund.digitalservice.ris.domain.docx.DocUnitAnchorImageElement;
import de.bund.digitalservice.ris.domain.docx.DocUnitBorderNumber;
import de.bund.digitalservice.ris.domain.docx.DocUnitErrorRunElement;
import de.bund.digitalservice.ris.domain.docx.DocUnitInlineImageElement;
import de.bund.digitalservice.ris.domain.docx.DocUnitNumberingList.DocUnitNumberingListNumberFormat;
import de.bund.digitalservice.ris.domain.docx.DocUnitNumberingListEntry;
import de.bund.digitalservice.ris.domain.docx.DocUnitParagraphElement;
import de.bund.digitalservice.ris.domain.docx.DocUnitRunTextElement;
import de.bund.digitalservice.ris.domain.docx.DocUnitTable;
import de.bund.digitalservice.ris.domain.docx.DocxImagePart;
import jakarta.xml.bind.JAXBElement;
import java.awt.Dimension;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
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
import org.docx4j.wml.ParaRPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STVerticalAlignRun;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;
import org.docx4j.wml.U;
import org.docx4j.wml.UnderlineEnumeration;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class DocUnitDocxBuilderTest {
  @Test
  void test_withoutConvertableElements() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
    var result = builder.build();

    assertNull(result);
  }

  @Test
  void testSetParagraph() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
    P paragraph = new P();

    var returnedBuilder = builder.setParagraph(paragraph);

    assertEquals(builder, returnedBuilder);
    assertEquals(paragraph, returnedBuilder.paragraph);
  }

  @Test
  void testSetTable() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
    Tbl table = new Tbl();

    var returnedBuilder = builder.setTable(table);

    assertEquals(builder, returnedBuilder);
    assertEquals(table, returnedBuilder.table);
  }

  @Test
  void testBuild_withTable() {
    // check every possible field because correct converting is not ready yet
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
    Tbl table =
        generateTable(
            List.of(
                List.of("cell r1c1", "cell r1c2", "cell r1c3"),
                List.of("cell r2c1", "cell r2c2", "cell r2c3"),
                List.of("cell r3c1", "cell r3c2", "cell r3c3")));

    var result = builder.setTable(table).build();

    assertTrue(result instanceof DocUnitTable);
    DocUnitTable tableElement = (DocUnitTable) result;
    assertEquals(3, tableElement.rows().size());
    var columns = tableElement.rows().get(0).columns();
    assertEquals(3, columns.size());
    assertEquals(1, columns.get(0).paragraphElements().size());
    assertEquals("<p>cell r1c1</p>", columns.get(0).paragraphElements().get(0).toHtmlString());
    assertEquals(1, columns.get(1).paragraphElements().size());
    assertEquals("<p>cell r1c2</p>", columns.get(1).paragraphElements().get(0).toHtmlString());
    assertEquals(1, columns.get(2).paragraphElements().size());
    assertEquals("<p>cell r1c3</p>", columns.get(2).paragraphElements().get(0).toHtmlString());
    columns = tableElement.rows().get(1).columns();
    assertEquals(3, columns.size());
    assertEquals(1, columns.get(0).paragraphElements().size());
    assertEquals("<p>cell r2c1</p>", columns.get(0).paragraphElements().get(0).toHtmlString());
    assertEquals(1, columns.get(1).paragraphElements().size());
    assertEquals("<p>cell r2c2</p>", columns.get(1).paragraphElements().get(0).toHtmlString());
    assertEquals(1, columns.get(2).paragraphElements().size());
    assertEquals("<p>cell r2c3</p>", columns.get(2).paragraphElements().get(0).toHtmlString());
    columns = tableElement.rows().get(2).columns();
    assertEquals(3, columns.size());
    assertEquals(1, columns.get(0).paragraphElements().size());
    assertEquals("<p>cell r3c1</p>", columns.get(0).paragraphElements().get(0).toHtmlString());
    assertEquals(1, columns.get(1).paragraphElements().size());
    assertEquals("<p>cell r3c2</p>", columns.get(1).paragraphElements().get(0).toHtmlString());
    assertEquals(1, columns.get(2).paragraphElements().size());
    assertEquals("<p>cell r3c3</p>", columns.get(2).paragraphElements().get(0).toHtmlString());
  }

  @Test
  void testBuild_withEmptyTable() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
    Tbl table = new Tbl();
    table.getContent().add(new Object());

    var result = builder.setTable(table).build();

    assertTrue(result instanceof DocUnitTable);
    assertTrue(((DocUnitTable) result).rows().isEmpty());
  }

  @Test
  void testBuild_withBorderNumber() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
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

    assertTrue(result instanceof DocUnitBorderNumber);
    var borderNumberElement = (DocUnitBorderNumber) result;
    assertEquals("1", borderNumberElement.getNumber());

    var htmlString = borderNumberElement.toHtmlString();
    assertEquals("<border-number number=\"1\"></border-number>", htmlString);
  }

  @Test
  void testBuild_withText() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
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

    assertTrue(result instanceof DocUnitParagraphElement);
    var paragraphElement = (DocUnitParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(DocUnitRunTextElement.class, runElement.getClass());
    assertEquals("text", ((DocUnitRunTextElement) runElement).getText());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p>text</p>", htmlString);
  }

  @Test
  void testBuild_withParagraphWithoutText() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
    P paragraph = new P();
    R run = new R();
    paragraph.getContent().add(run);
    paragraph.getContent().add(new P.Hyperlink());

    var result = builder.setParagraph(paragraph).build();

    assertTrue(result instanceof DocUnitParagraphElement);
    var paragraphElement = (DocUnitParagraphElement) result;
    assertTrue(paragraphElement.getRunElements().isEmpty());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p></p>", htmlString);
  }

  @Test
  void testBuild_withTextAndParagraphAlignmentRight() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
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

    assertTrue(result instanceof DocUnitParagraphElement);
    DocUnitParagraphElement paragraphElement = (DocUnitParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(DocUnitRunTextElement.class, runElement.getClass());
    assertEquals("text", ((DocUnitRunTextElement) runElement).getText());
    assertEquals("right", paragraphElement.getAlignment());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p style=\"text-align: right;\">text</p>", htmlString);
  }

  @Test
  void testBuild_withTextAndParagraphAlignmentLeft() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
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

    assertTrue(result instanceof DocUnitParagraphElement);
    DocUnitParagraphElement paragraphElement = (DocUnitParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(DocUnitRunTextElement.class, runElement.getClass());
    assertEquals("text", ((DocUnitRunTextElement) runElement).getText());
    assertEquals("left", paragraphElement.getAlignment());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p style=\"text-align: left;\">text</p>", htmlString);
  }

  @Test
  void testBuild_withTextAndParagraphAlignmentJustify() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
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

    assertTrue(result instanceof DocUnitParagraphElement);
    DocUnitParagraphElement paragraphElement = (DocUnitParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(DocUnitRunTextElement.class, runElement.getClass());
    assertEquals("text", ((DocUnitRunTextElement) runElement).getText());
    assertEquals("justify", paragraphElement.getAlignment());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p style=\"text-align: justify;\">text</p>", htmlString);
  }

  @Test
  void testBuild_withTextAndParagraphAlignmentCenter() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
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

    assertTrue(result instanceof DocUnitParagraphElement);
    DocUnitParagraphElement paragraphElement = (DocUnitParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(DocUnitRunTextElement.class, runElement.getClass());
    assertEquals("text", ((DocUnitRunTextElement) runElement).getText());
    assertEquals("center", paragraphElement.getAlignment());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p style=\"text-align: center;\">text</p>", htmlString);
  }

  @Test
  void testBuild_withTextAndParagraphSize() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
    P paragraph = new P();
    PPr pPr = new PPr();
    ParaRPr rPr = new ParaRPr();
    HpsMeasure size = new HpsMeasure();
    size.setVal(new BigInteger("48"));
    rPr.setSz(size);
    pPr.setRPr(rPr);
    paragraph.setPPr(pPr);
    R run = new R();
    Text text = new Text();
    text.setValue("text");
    JAXBElement<Text> element = new JAXBElement<>(new QName("text"), Text.class, text);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    var result = builder.setParagraph(paragraph).build();

    assertTrue(result instanceof DocUnitParagraphElement);
    DocUnitParagraphElement paragraphElement = (DocUnitParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(DocUnitRunTextElement.class, runElement.getClass());
    assertEquals("text", ((DocUnitRunTextElement) runElement).getText());
    assertEquals("48", paragraphElement.getSize().toString());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p style=\"font-size: 24pt;\">text</p>", htmlString);
  }

  @Test
  void testBuild_withTextAndRunSize() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
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

    assertTrue(result instanceof DocUnitParagraphElement);
    DocUnitParagraphElement paragraphElement = (DocUnitParagraphElement) result;
    assertEquals(2, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(DocUnitRunTextElement.class, runElement.getClass());
    var runTextElement = ((DocUnitRunTextElement) runElement);
    assertEquals("text", runTextElement.getText());
    assertEquals("48", runTextElement.getSize().toString());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals(
        "<p><span style=\"font-size: 24pt;\">text</span><span style=\"font-size: 10.5pt;\">text2</span></p>",
        htmlString);
  }

  @Test
  void testBuild_withTextAndParagraphWeight() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
    P paragraph = new P();
    PPr pPr = new PPr();
    ParaRPr rPr = new ParaRPr();
    BooleanDefaultTrue bold = new BooleanDefaultTrue();
    bold.setVal(true);
    rPr.setB(bold);
    pPr.setRPr(rPr);
    paragraph.setPPr(pPr);
    R run = new R();
    Text text = new Text();
    text.setValue("text");
    JAXBElement<Text> element = new JAXBElement<>(new QName("text"), Text.class, text);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    var result = builder.setParagraph(paragraph).build();

    assertTrue(result instanceof DocUnitParagraphElement);
    DocUnitParagraphElement paragraphElement = (DocUnitParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(DocUnitRunTextElement.class, runElement.getClass());
    assertEquals("text", ((DocUnitRunTextElement) runElement).getText());
    assertTrue(paragraphElement.getBold());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p style=\"font-weight: bold;\">text</p>", htmlString);
  }

  @Test
  void testBuild_withTextAndRunWeight() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
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

    assertTrue(result instanceof DocUnitParagraphElement);
    DocUnitParagraphElement paragraphElement = (DocUnitParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(DocUnitRunTextElement.class, runElement.getClass());
    var runTextElement = (DocUnitRunTextElement) runElement;
    assertEquals("text", runTextElement.getText());
    assertTrue(runTextElement.getBold());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p><span style=\"font-weight: bold;\">text</span></p>", htmlString);
  }

  @Test
  void testBuild_withTextAndParagraphItalic() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
    P paragraph = new P();
    PPr pPr = new PPr();
    ParaRPr rPr = new ParaRPr();
    BooleanDefaultTrue italic = new BooleanDefaultTrue();
    italic.setVal(true);
    rPr.setI(italic);
    pPr.setRPr(rPr);
    paragraph.setPPr(pPr);
    R run = new R();
    Text text = new Text();
    text.setValue("text");
    JAXBElement<Text> element = new JAXBElement<>(new QName("text"), Text.class, text);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    var result = builder.setParagraph(paragraph).build();

    assertTrue(result instanceof DocUnitParagraphElement);
    DocUnitParagraphElement paragraphElement = (DocUnitParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(DocUnitRunTextElement.class, runElement.getClass());
    assertEquals("text", ((DocUnitRunTextElement) runElement).getText());
    assertTrue(paragraphElement.getItalic());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p style=\"font-style: italic;\">text</p>", htmlString);
  }

  @Test
  void testBuild_withTextAndRunItalic() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
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

    assertTrue(result instanceof DocUnitParagraphElement);
    DocUnitParagraphElement paragraphElement = (DocUnitParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(DocUnitRunTextElement.class, runElement.getClass());
    var runTextElement = (DocUnitRunTextElement) runElement;
    assertEquals("text", runTextElement.getText());
    assertTrue(runTextElement.getItalic());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p><span style=\"font-style: italic;\">text</span></p>", htmlString);
  }

  @Test
  void testBuild_withTextAndParagraphStrike() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
    P paragraph = new P();
    PPr pPr = new PPr();
    ParaRPr rPr = new ParaRPr();
    BooleanDefaultTrue strike = new BooleanDefaultTrue();
    strike.setVal(true);
    rPr.setStrike(strike);
    pPr.setRPr(rPr);
    paragraph.setPPr(pPr);
    R run = new R();
    Text text = new Text();
    text.setValue("text");
    JAXBElement<Text> element = new JAXBElement<>(new QName("text"), Text.class, text);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    var result = builder.setParagraph(paragraph).build();

    assertTrue(result instanceof DocUnitParagraphElement);
    DocUnitParagraphElement paragraphElement = (DocUnitParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(DocUnitRunTextElement.class, runElement.getClass());
    assertEquals("text", ((DocUnitRunTextElement) runElement).getText());
    assertTrue(paragraphElement.getStrike());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p style=\"text-decoration: line-through;\">text</p>", htmlString);
  }

  @Test
  void testBuild_withTextAndRunStrike() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
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

    assertTrue(result instanceof DocUnitParagraphElement);
    DocUnitParagraphElement paragraphElement = (DocUnitParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(DocUnitRunTextElement.class, runElement.getClass());
    var runTextElement = (DocUnitRunTextElement) runElement;
    assertEquals("text", runTextElement.getText());
    assertTrue(runTextElement.getStrike());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p><span style=\"text-decoration: line-through;\">text</span></p>", htmlString);
  }

  @Test
  void testBuild_withTextAndParagraphUnderline() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
    P paragraph = new P();
    PPr pPr = new PPr();
    ParaRPr rPr = new ParaRPr();
    U underline = new U();
    underline.setVal(UnderlineEnumeration.SINGLE);
    rPr.setU(underline);
    pPr.setRPr(rPr);
    paragraph.setPPr(pPr);
    R run = new R();
    Text text = new Text();
    text.setValue("text");
    JAXBElement<Text> element = new JAXBElement<>(new QName("text"), Text.class, text);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    var result = builder.setParagraph(paragraph).build();

    assertTrue(result instanceof DocUnitParagraphElement);
    DocUnitParagraphElement paragraphElement = (DocUnitParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(DocUnitRunTextElement.class, runElement.getClass());
    assertEquals("text", ((DocUnitRunTextElement) runElement).getText());
    assertEquals("single", paragraphElement.getUnderline());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p style=\"text-decoration: underline;\">text</p>", htmlString);
  }

  @Test
  void testBuild_withTextAndRunUnderline() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
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

    assertTrue(result instanceof DocUnitParagraphElement);
    DocUnitParagraphElement paragraphElement = (DocUnitParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(DocUnitRunTextElement.class, runElement.getClass());
    var runTextElement = (DocUnitRunTextElement) runElement;
    assertEquals("text", runTextElement.getText());
    assertEquals("single", runTextElement.getUnderline());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p><span style=\"text-decoration: underline;\">text</span></p>", htmlString);
  }

  @Test
  void testBuild_withTextAndParagraphSubscript() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
    P paragraph = new P();
    PPr pPr = new PPr();
    ParaRPr rPr = new ParaRPr();
    CTVerticalAlignRun vertAlign = new CTVerticalAlignRun();
    vertAlign.setVal(STVerticalAlignRun.SUBSCRIPT);
    rPr.setVertAlign(vertAlign);
    pPr.setRPr(rPr);
    paragraph.setPPr(pPr);
    R run = new R();
    Text text = new Text();
    text.setValue("text");
    JAXBElement<Text> element = new JAXBElement<>(new QName("text"), Text.class, text);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    var result = builder.setParagraph(paragraph).build();

    assertTrue(result instanceof DocUnitParagraphElement);
    DocUnitParagraphElement paragraphElement = (DocUnitParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(DocUnitRunTextElement.class, runElement.getClass());
    assertEquals("text", ((DocUnitRunTextElement) runElement).getText());
    assertEquals("SUBSCRIPT", paragraphElement.getVertAlign().toString());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p style=\"vertical-align: sub\">text</p>", htmlString);
  }

  @Test
  void testBuild_withTextAndRunSubscript() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
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

    assertTrue(result instanceof DocUnitParagraphElement);
    DocUnitParagraphElement paragraphElement = (DocUnitParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(DocUnitRunTextElement.class, runElement.getClass());
    var runTextElement = ((DocUnitRunTextElement) runElement);
    assertEquals("text", runTextElement.getText());
    assertEquals("SUBSCRIPT", runTextElement.getVertAlign().toString());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p><span style=\"vertical-align: sub\">text</span></p>", htmlString);
  }

  @Test
  void testBuild_withTextAndParagraphSuperscript() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
    P paragraph = new P();
    PPr pPr = new PPr();
    ParaRPr rPr = new ParaRPr();
    CTVerticalAlignRun vertAlign = new CTVerticalAlignRun();
    vertAlign.setVal(STVerticalAlignRun.SUPERSCRIPT);
    rPr.setVertAlign(vertAlign);
    pPr.setRPr(rPr);
    paragraph.setPPr(pPr);
    R run = new R();
    Text text = new Text();
    text.setValue("text");
    JAXBElement<Text> element = new JAXBElement<>(new QName("text"), Text.class, text);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    var result = builder.setParagraph(paragraph).build();

    assertTrue(result instanceof DocUnitParagraphElement);
    DocUnitParagraphElement paragraphElement = (DocUnitParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(DocUnitRunTextElement.class, runElement.getClass());
    assertEquals("text", ((DocUnitRunTextElement) runElement).getText());
    assertEquals("SUPERSCRIPT", paragraphElement.getVertAlign().toString());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p style=\"vertical-align: super\">text</p>", htmlString);
  }

  @Test
  void testBuild_withTextAndRunSuperscript() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
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

    assertTrue(result instanceof DocUnitParagraphElement);
    DocUnitParagraphElement paragraphElement = (DocUnitParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(DocUnitRunTextElement.class, runElement.getClass());
    var runTextElement = ((DocUnitRunTextElement) runElement);
    assertEquals("text", runTextElement.getText());
    assertEquals("SUPERSCRIPT", runTextElement.getVertAlign().toString());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p><span style=\"vertical-align: super\">text</span></p>", htmlString);
  }

  @Test
  void testBuild_withMultipleTextBlocks() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
    P paragraph = new P();
    R run = new R();
    Text text = new Text();
    text.setValue("run text 1");
    JAXBElement<Text> element = new JAXBElement<>(new QName("text"), Text.class, text);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    R run2 = new R();
    Text text2 = new Text();
    text2.setValue("run text 2");
    JAXBElement<Text> element2 = new JAXBElement<>(new QName("text"), Text.class, text2);
    run2.getContent().add(element2);
    paragraph.getContent().add(run2);

    var result = builder.setParagraph(paragraph).build();

    assertTrue(result instanceof DocUnitParagraphElement);
    DocUnitParagraphElement paragraphElement = (DocUnitParagraphElement) result;
    assertEquals(2, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(DocUnitRunTextElement.class, runElement.getClass());
    var runTextElement = (DocUnitRunTextElement) runElement;
    assertEquals("run text 1", runTextElement.getText());
    runElement = paragraphElement.getRunElements().get(1);
    assertEquals(DocUnitRunTextElement.class, runElement.getClass());
    runTextElement = (DocUnitRunTextElement) runElement;
    assertEquals("run text 2", runTextElement.getText());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p>run text 1run text 2</p>", htmlString);
  }

  @Test
  void testBuild_withInlineImage() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
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

    assertTrue(result instanceof DocUnitParagraphElement);
    DocUnitParagraphElement paragraphElement = (DocUnitParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(DocUnitInlineImageElement.class, runElement.getClass());
    var runImageElement = (DocUnitInlineImageElement) runElement;
    assertEquals("content-type", runImageElement.getContentType());
    assertEquals("AQI=", runImageElement.getBase64Representation());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p><img src=\"data:content-type;base64, AQI=\" /></p>", htmlString);
  }

  @Test
  void testBuild_withInlineImage_withAlternateText() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
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

    assertTrue(result instanceof DocUnitParagraphElement);
    DocUnitParagraphElement paragraphElement = (DocUnitParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(DocUnitInlineImageElement.class, runElement.getClass());
    var runImageElement = (DocUnitInlineImageElement) runElement;
    assertEquals("content-type", runImageElement.getContentType());
    assertEquals("AQI=", runImageElement.getBase64Representation());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals(
        "<p><img src=\"data:content-type;base64, AQI=\" alt=\"namedescription\" /></p>",
        htmlString);
  }

  @Test
  void testBuild_withInlineImage_withSize() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
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

    assertTrue(result instanceof DocUnitParagraphElement);
    DocUnitParagraphElement paragraphElement = (DocUnitParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(DocUnitInlineImageElement.class, runElement.getClass());
    var runImageElement = (DocUnitInlineImageElement) runElement;
    assertEquals("content-type", runImageElement.getContentType());
    assertEquals("AQI=", runImageElement.getBase64Representation());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals(
        "<p><img src=\"data:content-type;base64, AQI=\" width=\"10\" height=\"10\" /></p>",
        htmlString);
  }

  @Test
  void testBuild_withInlineImage_withAlternateTextAndSize() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
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

    assertTrue(result instanceof DocUnitParagraphElement);
    DocUnitParagraphElement paragraphElement = (DocUnitParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(DocUnitInlineImageElement.class, runElement.getClass());
    var runImageElement = (DocUnitInlineImageElement) runElement;
    assertEquals("content-type", runImageElement.getContentType());
    assertEquals("AQI=", runImageElement.getBase64Representation());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals(
        "<p><img src=\"data:content-type;base64, AQI=\" alt=\"namedescription\" width=\"10\" height=\"10\" /></p>",
        htmlString);
  }

  @Test
  void testBuild_withAnchorGraphic() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
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

    assertTrue(result instanceof DocUnitParagraphElement);
    DocUnitParagraphElement paragraphElement = (DocUnitParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(DocUnitAnchorImageElement.class, runElement.getClass());
    var runImageElement = (DocUnitAnchorImageElement) runElement;
    assertEquals("content-type", runImageElement.getContentType());
    assertEquals("AQI=", runImageElement.getBase64Representation());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p><img src=\"data:content-type;base64, AQI=\" /></p>", htmlString);
  }

  @Test
  void testBuild_withAnchorGraphic_withLeftFloating() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
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

    assertTrue(result instanceof DocUnitParagraphElement);
    DocUnitParagraphElement paragraphElement = (DocUnitParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(DocUnitAnchorImageElement.class, runElement.getClass());
    var runImageElement = (DocUnitAnchorImageElement) runElement;
    assertEquals("content-type", runImageElement.getContentType());
    assertEquals("AQI=", runImageElement.getBase64Representation());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals(
        "<p class=\"clearfix\"><img src=\"data:content-type;base64, AQI=\" style=\"float: left;\" /></p>",
        htmlString);
  }

  @Test
  void testBuild_withAnchorGraphic_withRightFloating() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
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

    assertTrue(result instanceof DocUnitParagraphElement);
    DocUnitParagraphElement paragraphElement = (DocUnitParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(DocUnitAnchorImageElement.class, runElement.getClass());
    var runImageElement = (DocUnitAnchorImageElement) runElement;
    assertEquals("content-type", runImageElement.getContentType());
    assertEquals("AQI=", runImageElement.getBase64Representation());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals(
        "<p class=\"clearfix\"><img src=\"data:content-type;base64, AQI=\" style=\"float: right;\" /></p>",
        htmlString);
  }

  @Test
  void testBuild_withAnchorGraphic_withUnknownFloating() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
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

    assertTrue(result instanceof DocUnitParagraphElement);
    DocUnitParagraphElement paragraphElement = (DocUnitParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(DocUnitErrorRunElement.class, runElement.getClass());
    var errorElement = (DocUnitErrorRunElement) runElement;

    assertEquals(
        "unknown run element: anchor image with unknown alignment: center",
        errorElement.toString());
  }

  @Test
  void testBuild_withMultipleGraphicObjects() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
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
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
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
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
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

    assertTrue(result instanceof DocUnitNumberingListEntry);
    var numberingListEntry = (DocUnitNumberingListEntry) result;
    assertEquals("0", numberingListEntry.numId());
    assertEquals("0", numberingListEntry.iLvl());
    assertEquals(DocUnitNumberingListNumberFormat.DECIMAL, numberingListEntry.numberFormat());
    assertNotNull(numberingListEntry.paragraphElement());
    DocUnitParagraphElement paragraphElement =
        (DocUnitParagraphElement) numberingListEntry.paragraphElement();
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(DocUnitRunTextElement.class, runElement.getClass());
    var runTextElement = (DocUnitRunTextElement) runElement;

    var htmlString = numberingListEntry.toHtmlString();
    assertEquals("<li><p>test text</p></li>", htmlString);
  }

  @Test
  void testBuild_withNumberingList_withNotAllowedNumberingFormat() {
    TestMemoryAppender memoryAppender = addLoggingTestAppender();
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
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

    assertTrue(result instanceof DocUnitNumberingListEntry);
    var numberingListEntry = (DocUnitNumberingListEntry) result;
    assertEquals("0", numberingListEntry.numId());
    assertEquals("0", numberingListEntry.iLvl());
    assertEquals(DocUnitNumberingListNumberFormat.BULLET, numberingListEntry.numberFormat());
    assertNotNull(numberingListEntry.paragraphElement());
    DocUnitParagraphElement paragraphElement =
        (DocUnitParagraphElement) numberingListEntry.paragraphElement();
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(DocUnitRunTextElement.class, runElement.getClass());
    var runTextElement = (DocUnitRunTextElement) runElement;

    var htmlString = numberingListEntry.toHtmlString();
    assertEquals("<li><p>test text</p></li>", htmlString);

    assertEquals(1, memoryAppender.count(Level.ERROR));
    assertEquals(
        "not implemented number format (CHICAGO) in list. use default bullet list",
        memoryAppender.getMessage(Level.ERROR, 0));
  }

  private TestMemoryAppender addLoggingTestAppender() {
    TestMemoryAppender memoryAppender = new TestMemoryAppender();
    memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());

    Logger logger = (Logger) LoggerFactory.getLogger(DocUnitDocxBuilder.class);
    logger.addAppender(memoryAppender);

    memoryAppender.start();

    return memoryAppender;
  }

  private void detachLoggingTestAppender(TestMemoryAppender memoryAppender) {
    memoryAppender.stop();

    Logger logger = (Logger) LoggerFactory.getLogger(DocUnitDocxBuilder.class);
    logger.detachAppender(memoryAppender);
  }

  private Tbl generateTable(List<List<String>> cells) {
    Tbl table = new Tbl();

    for (List<String> rows : cells) {
      Tr row = new Tr();
      for (String cellText : rows) {
        Tc cell = new Tc();
        cell.getContent().add(generateParagraph(cellText));
        JAXBElement<Tc> tcElement = new JAXBElement<>(new QName("tc"), Tc.class, cell);
        row.getContent().add(tcElement);
      }
      table.getContent().add(row);
    }

    return table;
  }

  private P generateParagraph(String cellText) {
    P paragraph = new P();
    R run = new R();
    Text text = new Text();
    text.setValue(cellText);
    JAXBElement<Text> textElement = new JAXBElement<>(new QName("text"), Text.class, text);
    run.getContent().add(textElement);
    paragraph.getContent().add(run);

    return paragraph;
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
