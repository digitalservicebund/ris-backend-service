package de.bund.digitalservice.ris.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.domain.docx.DocUnitBorderNumber;
import de.bund.digitalservice.ris.domain.docx.DocUnitImageElement;
import de.bund.digitalservice.ris.domain.docx.DocUnitParagraphElement;
import de.bund.digitalservice.ris.domain.docx.DocUnitRunTextElement;
import de.bund.digitalservice.ris.domain.docx.DocUnitTable;
import jakarta.xml.bind.JAXBElement;
import java.math.BigInteger;
import java.util.HashMap;
import javax.xml.namespace.QName;
import org.docx4j.dml.CTBlip;
import org.docx4j.dml.CTBlipFillProperties;
import org.docx4j.dml.Graphic;
import org.docx4j.dml.GraphicData;
import org.docx4j.dml.picture.Pic;
import org.docx4j.dml.wordprocessingDrawing.Anchor;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.Jc;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.ParaRPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;
import org.docx4j.wml.U;
import org.docx4j.wml.UnderlineEnumeration;
import org.junit.jupiter.api.Test;

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
    assertEquals(returnedBuilder.paragraph, paragraph);
  }

  @Test
  void testSetTable() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
    Tbl table = new Tbl();

    var returnedBuilder = builder.setTable(table);

    assertEquals(builder, returnedBuilder);
    assertEquals(returnedBuilder.table, table);
  }

  @Test
  void testBuild_withTable() {
    // check every possible field because correct converting is not ready yet
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
    Tbl table = new Tbl();

    Tr rightTr = new Tr();
    Text rightTrText = new Text();
    rightTrText.setValue("text in tr;");
    JAXBElement<Text> rightTrTextElement =
        new JAXBElement<>(new QName("text"), Text.class, rightTrText);
    rightTr.getContent().add(rightTrTextElement);
    table.getContent().add(rightTr);

    Tr wrongTr = new Tr();
    wrongTr.getContent().add(new Object());
    table.getContent().add(wrongTr);

    Tc tc = new Tc();
    Text tcText = new Text();
    tcText.setValue(";tc text;");
    tc.getContent().add(tcText);
    table.getContent().add(tc);

    P paragraph = new P();
    R paragraphRun = new R();
    Text pText = new Text();
    pText.setValue("p text;");
    JAXBElement<Text> pTextElement = new JAXBElement<>(new QName("text"), Text.class, pText);
    paragraphRun.getContent().add(pTextElement);
    paragraph.getContent().add(paragraphRun);
    table.getContent().add(paragraph);

    R rightRun = new R();
    Text rightRText = new Text();
    rightRText.setValue("r text;");
    JAXBElement<Text> rTextElement = new JAXBElement<>(new QName("text"), Text.class, rightRText);
    rightRun.getContent().add(rTextElement);
    table.getContent().add(rightRun);

    R wrongRun = new R();
    wrongRun.getContent().add(new Object());
    table.getContent().add(wrongRun);

    var result = builder.setTable(table).build();

    assertTrue(result instanceof DocUnitTable);
    assertEquals(
        "text in tr;java.lang.Object;tc text;p text;r text;java.lang.Object",
        ((DocUnitTable) result).getTextContent());
  }

  @Test
  void testBuild_withEmptyTable() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
    Tbl table = new Tbl();
    table.getContent().add(new Object());

    var result = builder.setTable(table).build();

    assertTrue(result instanceof DocUnitTable);
    assertEquals("<no table elements found>", ((DocUnitTable) result).getTextContent());
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
  void testBuild_withTextAndParagraphAlignment() {
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
    assertEquals("<p style=\"font-size: 24px;\">text</p>", htmlString);
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

    var result = builder.setParagraph(paragraph).build();

    assertTrue(result instanceof DocUnitParagraphElement);
    DocUnitParagraphElement paragraphElement = (DocUnitParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(DocUnitRunTextElement.class, runElement.getClass());
    var runTextElement = ((DocUnitRunTextElement) runElement);
    assertEquals("text", runTextElement.getText());
    assertEquals("48", runTextElement.getSize().toString());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p><span style=\"font-size: 24px;\">text</span></p>", htmlString);
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
    assertEquals(true, paragraphElement.getBold());

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
    assertEquals(true, runTextElement.getBold());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p><span style=\"font-weight: bold;\">text</span></p>", htmlString);
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
    Inline inline = new Inline();
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
    drawing.getAnchorOrInline().add(inline);
    JAXBElement<Drawing> element = new JAXBElement<>(new QName("drawing"), Drawing.class, drawing);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    HashMap<String, BinaryPartAbstractImage> images = new HashMap<>();
    BinaryPartAbstractImage image = mock(BinaryPartAbstractImage.class);
    when(image.getContentType()).thenReturn("content-type");
    when(image.getBytes()).thenReturn(new byte[] {1, 2});
    images.put("image-ref", image);

    var result = builder.setParagraph(paragraph).setImages(images).build();

    assertTrue(result instanceof DocUnitParagraphElement);
    DocUnitParagraphElement paragraphElement = (DocUnitParagraphElement) result;
    assertEquals(1, paragraphElement.getRunElements().size());
    var runElement = paragraphElement.getRunElements().get(0);
    assertEquals(DocUnitImageElement.class, runElement.getClass());
    var runImageElement = (DocUnitImageElement) runElement;
    assertEquals("content-type", runImageElement.getContentType());
    assertEquals("AQI=", runImageElement.getBase64Representation());

    var htmlString = paragraphElement.toHtmlString();
    assertEquals("<p><img src=\"data:content-type;base64, AQI=\" /></p>", htmlString);
  }

  @Test
  void testBuild_withAnchorGraphic() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
    P paragraph = new P();
    R run = new R();
    Drawing drawing = new Drawing();
    Anchor anchor = new Anchor();
    drawing.getAnchorOrInline().add(anchor);
    JAXBElement<Drawing> element = new JAXBElement<>(new QName("drawing"), Drawing.class, drawing);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    Exception exception =
        assertThrows(
            DocxConverterException.class,
            () -> {
              builder.setParagraph(paragraph).build();
            });

    assertEquals("unsupported drawing object", exception.getMessage());
  }

  @Test
  void testBuild_withMultipleGraphicObjects() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
    P paragraph = new P();
    R run = new R();
    Drawing drawing = new Drawing();
    Anchor anchor = new Anchor();
    drawing.getAnchorOrInline().add(anchor);
    Inline inline = new Inline();
    drawing.getAnchorOrInline().add(inline);
    JAXBElement<Drawing> element = new JAXBElement<>(new QName("drawing"), Drawing.class, drawing);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    Exception exception =
        assertThrows(
            DocxConverterException.class,
            () -> {
              builder.setParagraph(paragraph).build();
            });

    assertEquals("more than one graphic data in a drawing", exception.getMessage());
  }

  @Test
  void testBuild_withInlineImageWithoutGraphicData() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
    P paragraph = new P();
    R run = new R();
    Drawing drawing = new Drawing();
    Inline inline = new Inline();
    Graphic graphic = new Graphic();
    inline.setGraphic(graphic);
    drawing.getAnchorOrInline().add(inline);
    JAXBElement<Drawing> element = new JAXBElement<>(new QName("drawing"), Drawing.class, drawing);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    Exception exception =
        assertThrows(
            DocxConverterException.class,
            () -> {
              builder.setParagraph(paragraph).build();
            });

    assertEquals("no graphic data", exception.getMessage());
  }
}
