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
import de.bund.digitalservice.ris.domain.docx.AnchorImageElement;
import de.bund.digitalservice.ris.domain.docx.BorderNumber;
import de.bund.digitalservice.ris.domain.docx.DocxImagePart;
import de.bund.digitalservice.ris.domain.docx.ErrorRunElement;
import de.bund.digitalservice.ris.domain.docx.InlineImageElement;
import de.bund.digitalservice.ris.domain.docx.NumberingListEntry;
import de.bund.digitalservice.ris.domain.docx.ParagraphElement;
import de.bund.digitalservice.ris.domain.docx.RunTextElement;
import de.bund.digitalservice.ris.domain.docx.TableElement;
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
import org.docx4j.wml.CTBorder;
import org.docx4j.wml.CTShd;
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
import org.docx4j.wml.STBorder;
import org.docx4j.wml.STShd;
import org.docx4j.wml.STVerticalAlignRun;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblBorders;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.Tc;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.TcPrInner;
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
    table.setTblPr(new TblPr());
    var result = builder.setTable(table).build();

    assertTrue(result instanceof TableElement);
    TableElement tableElement = (TableElement) result;
    assertEquals(3, tableElement.rows.size());
    var cells = tableElement.rows.get(0).cells;
    assertEquals(3, cells.size());
    assertEquals(1, cells.get(0).paragraphElements.size());
    assertEquals("<p>cell r1c1</p>", cells.get(0).paragraphElements.get(0).toHtmlString());
    assertEquals(1, cells.get(1).paragraphElements.size());
    assertEquals("<p>cell r1c2</p>", cells.get(1).paragraphElements.get(0).toHtmlString());
    assertEquals(1, cells.get(2).paragraphElements.size());
    assertEquals("<p>cell r1c3</p>", cells.get(2).paragraphElements.get(0).toHtmlString());
    cells = tableElement.rows.get(1).cells;
    assertEquals(3, cells.size());
    assertEquals(1, cells.get(0).paragraphElements.size());
    assertEquals("<p>cell r2c1</p>", cells.get(0).paragraphElements.get(0).toHtmlString());
    assertEquals(1, cells.get(1).paragraphElements.size());
    assertEquals("<p>cell r2c2</p>", cells.get(1).paragraphElements.get(0).toHtmlString());
    assertEquals(1, cells.get(2).paragraphElements.size());
    assertEquals("<p>cell r2c3</p>", cells.get(2).paragraphElements.get(0).toHtmlString());
    cells = tableElement.rows.get(2).cells;
    assertEquals(3, cells.size());
    assertEquals(1, cells.get(0).paragraphElements.size());
    assertEquals("<p>cell r3c1</p>", cells.get(0).paragraphElements.get(0).toHtmlString());
    assertEquals(1, cells.get(1).paragraphElements.size());
    assertEquals("<p>cell r3c2</p>", cells.get(1).paragraphElements.get(0).toHtmlString());
    assertEquals(1, cells.get(2).paragraphElements.size());
    assertEquals("<p>cell r3c3</p>", cells.get(2).paragraphElements.get(0).toHtmlString());
  }

  @Test
  void testBuild_withEmptyTable() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
    Tbl table = new Tbl();
    table.getContent().add(new Object());

    var result = builder.setTable(table).build();

    assertTrue(result instanceof TableElement);
    assertTrue(((TableElement) result).rows.isEmpty());
  }

  @Test
  void testBuild_withTable_withBorder() {
    var border1 = new CTBorder();
    border1.setVal(STBorder.SINGLE);
    border1.setColor("a64d79");
    border1.setSz(BigInteger.valueOf(24));

    var border2 = new CTBorder();
    border2.setVal(STBorder.SINGLE);
    border2.setColor("abc");
    border2.setSz(BigInteger.valueOf(48));

    TblBorders borders = new TblBorders();
    borders.setTop(border1);
    borders.setRight(border1);
    borders.setBottom(border2);
    borders.setLeft(border2);
    borders.setInsideH(border1);
    borders.setInsideV(border2);
    TblPr tblPr = new TblPr();
    tblPr.setTblBorders(borders);
    Tbl table = new Tbl();
    table.setTblPr(tblPr);

    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
    var result = builder.setTable(table).build();
    assertTrue(result.toHtmlString().contains("border-top: 3px solid #a64d79;"));
    assertTrue(result.toHtmlString().contains("border-right: 3px solid #a64d79;"));
    assertTrue(result.toHtmlString().contains("border-bottom: 6px solid #abc;"));
    assertTrue(result.toHtmlString().contains("border-left: 6px solid #abc;"));
  }

  @Test
  void testBuild_withTable_withCell() {
    var topRightCell = generateTableCellWidthBorder("MNOPQR", 12);
    var rightCtBorder = new CTBorder();
    rightCtBorder.setVal(STBorder.SINGLE);
    rightCtBorder.setSz(BigInteger.valueOf(24));
    rightCtBorder.setColor("foo");
    topRightCell.getValue().getTcPr().getTcBorders().setRight(rightCtBorder);

    var gridSpan = new TcPrInner.GridSpan();
    gridSpan.setVal(BigInteger.valueOf(2));
    topRightCell.getValue().getTcPr().setGridSpan(gridSpan);

    var shading = new CTShd();
    shading.setVal(STShd.CLEAR);
    shading.setFill("111222");
    topRightCell.getValue().getTcPr().setShd(shading);

    var row = new Tr();
    row.getContent().add(generateTableCellWidthBorder("ABCDEF", 12));
    row.getContent().add(generateTableCellWidthBorder("GHIJKL", 12));
    row.getContent().add(topRightCell);
    row.getContent().add(generateTableCellWidthBorder("MNOPQR", 12));

    var tableCtBorder = new CTBorder();
    tableCtBorder.setVal(STBorder.SINGLE);
    tableCtBorder.setSz(BigInteger.valueOf(48));
    tableCtBorder.setColor("auto");

    var tableBorders = new TblBorders();
    tableBorders.setInsideV(tableCtBorder);
    tableBorders.setInsideH(tableCtBorder);

    var tblPr = new TblPr();
    tblPr.setTblBorders(tableBorders);
    var tbl = new Tbl();
    tbl.setTblPr(tblPr);
    tbl.getContent().add(row);

    var builder = DocUnitDocxBuilder.newInstance();
    builder.setTable(tbl);

    var result = builder.build().toHtmlString();

    // cell should have colspan
    assertTrue(result.contains("colspan=\"2\""));

    // cell should take insideV from table
    assertTrue(
        result.contains(
            "<td style=\"min-width: 5px; padding: 5px; border-top: 1px solid #ghijkl; border-right: 6px solid #000; border-left: 6px solid #000;\">"));

    // insideV from table should not overwrite cell's border
    assertTrue(
        result.contains(
            "<td colspan=\"2\" style=\"min-width: 5px; padding: 5px; background-color: #111222; border-top: 1px solid #mnopqr; border-right: 3px solid #foo; border-left: 6px solid #000;\"><p>foo</p></td>"));
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

    assertTrue(result instanceof BorderNumber);
    var borderNumberElement = (BorderNumber) result;
    assertEquals("1", borderNumberElement.getNumber());

    var htmlString = borderNumberElement.toHtmlString();
    assertEquals("<border-number><number>1</number></border-number>", htmlString);
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
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
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

    assertTrue(result instanceof ParagraphElement);
    ParagraphElement paragraphElement = (ParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(RunTextElement.class, runElement.getClass());
    assertEquals("text", ((RunTextElement) runElement).getText());
    assertTrue(paragraphElement.getStyleString().contains("font-size: 24pt"));

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

    assertTrue(result instanceof ParagraphElement);
    ParagraphElement paragraphElement = (ParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(RunTextElement.class, runElement.getClass());
    assertEquals("text", ((RunTextElement) runElement).getText());
    assertTrue(paragraphElement.getStyleString().contains("font-weight: bold"));

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

    assertTrue(result instanceof ParagraphElement);
    ParagraphElement paragraphElement = (ParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(RunTextElement.class, runElement.getClass());
    assertEquals("text", ((RunTextElement) runElement).getText());
    assertTrue(paragraphElement.getStyleString().contains("font-style: italic"));

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

    assertTrue(result instanceof ParagraphElement);
    ParagraphElement paragraphElement = (ParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(RunTextElement.class, runElement.getClass());
    assertEquals("text", ((RunTextElement) runElement).getText());
    assertTrue(paragraphElement.getStyleString().contains("text-decoration: line-through"));

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

    assertTrue(result instanceof ParagraphElement);
    ParagraphElement paragraphElement = (ParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(RunTextElement.class, runElement.getClass());
    assertEquals("text", ((RunTextElement) runElement).getText());
    assertTrue(paragraphElement.getStyleString().contains("text-decoration: underline"));

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

    assertTrue(result instanceof ParagraphElement);
    ParagraphElement paragraphElement = (ParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(RunTextElement.class, runElement.getClass());
    assertEquals("text", ((RunTextElement) runElement).getText());
    assertTrue(paragraphElement.getStyleString().contains("vertical-align: sub"));

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p style=\"vertical-align: sub;\">text</p>", htmlString);
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

    assertTrue(result instanceof ParagraphElement);
    ParagraphElement paragraphElement = (ParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(RunTextElement.class, runElement.getClass());
    assertEquals("text", ((RunTextElement) runElement).getText());
    assertTrue(paragraphElement.getStyleString().contains("vertical-align: super"));

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p style=\"vertical-align: super;\">text</p>", htmlString);
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
        cell.setTcPr(new TcPr());
        JAXBElement<Tc> tcElement = new JAXBElement<>(new QName("tc"), Tc.class, cell);
        row.getContent().add(tcElement);
      }
      table.getContent().add(row);
    }

    return table;
  }

  private JAXBElement<Tc> generateTableCellWidthBorder(String color, Integer width) {
    var ctBorder = new CTBorder();
    ctBorder.setVal(STBorder.SINGLE);
    ctBorder.setSz(BigInteger.valueOf(width));
    ctBorder.setColor(color);

    var tcBorders = new TcPrInner.TcBorders();
    tcBorders.setTop(ctBorder);

    var tcPr = new TcPr();
    tcPr.setTcBorders(tcBorders);

    var tc = new Tc();
    tc.setTcPr(tcPr);
    tc.getContent().add(generateParagraph("foo"));
    return new JAXBElement<>(new QName("tc"), Tc.class, tc);
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
