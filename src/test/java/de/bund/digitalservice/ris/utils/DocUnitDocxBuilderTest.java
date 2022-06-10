package de.bund.digitalservice.ris.utils;

import static org.junit.jupiter.api.Assertions.*;

import de.bund.digitalservice.ris.datamodel.docx.DocUnitRandnummer;
import de.bund.digitalservice.ris.datamodel.docx.DocUnitTable;
import de.bund.digitalservice.ris.datamodel.docx.DocUnitTextElement;
import jakarta.xml.bind.JAXBElement;
import java.math.BigInteger;
import javax.xml.namespace.QName;
import org.docx4j.wml.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("test")
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
  void testBuild_withRandnummer() {
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

    assertTrue(result instanceof DocUnitRandnummer);
    assertEquals("1", ((DocUnitRandnummer) result).getNumber());
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

    assertTrue(result instanceof DocUnitTextElement);
    assertEquals("text", ((DocUnitTextElement) result).getText());
  }

  @Test
  void testBuild_withTextPossibleRandnummer() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
    P paragraph = new P();
    paragraph.setPPr(new PPr());

    var result = builder.setParagraph(paragraph).build();

    assertTrue(result instanceof DocUnitTextElement);
    assertEquals("", ((DocUnitTextElement) result).getText());
  }

  @Test
  void testBuild_withParagraphWithoutText() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
    P paragraph = new P();
    R run = new R();
    paragraph.getContent().add(run);
    paragraph.getContent().add(new P.Hyperlink());

    var result = builder.setParagraph(paragraph).build();

    assertNull(result);
  }

  @Test
  void testBuild_withTextAndAlignment() {
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

    assertTrue(result instanceof DocUnitTextElement);
    DocUnitTextElement textElement = (DocUnitTextElement) result;
    assertEquals("text", textElement.getText());
    assertEquals("center", textElement.getAlignment());
  }

  @Test
  void testBuild_withTextAndSize() {
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

    assertTrue(result instanceof DocUnitTextElement);
    DocUnitTextElement textElement = (DocUnitTextElement) result;
    assertEquals("text", textElement.getText());
    assertEquals("48", textElement.getSize().toString());
  }

  @Test
  void testBuild_withTextAndWeight() {
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

    assertTrue(result instanceof DocUnitTextElement);
    DocUnitTextElement textElement = (DocUnitTextElement) result;
    assertEquals("text", textElement.getText());
    assertEquals(true, textElement.getBold());
  }

  @Test
  void testBuild_withMultipleTextBlocks() {
    DocUnitDocxBuilder builder = DocUnitDocxBuilder.newInstance();
    P paragraph = new P();
    R run = new R();
    Text text = new Text();
    text.setValue("combined");
    JAXBElement<Text> element = new JAXBElement<>(new QName("text"), Text.class, text);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    R run2 = new R();
    Text text2 = new Text();
    text2.setValue("text");
    JAXBElement<Text> element2 = new JAXBElement<>(new QName("text"), Text.class, text2);
    run2.getContent().add(element2);
    paragraph.getContent().add(run2);

    var result = builder.setParagraph(paragraph).build();

    assertTrue(result instanceof DocUnitTextElement);
    assertEquals("combinedtext", ((DocUnitTextElement) result).getText());
  }
}
