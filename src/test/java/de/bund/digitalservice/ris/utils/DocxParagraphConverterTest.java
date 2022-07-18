package de.bund.digitalservice.ris.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.bund.digitalservice.ris.domain.docx.DocUnitTable;
import de.bund.digitalservice.ris.domain.docx.DocUnitTextElement;
import jakarta.xml.bind.JAXBElement;
import java.util.Collections;
import javax.xml.namespace.QName;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Text;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DocxParagraphConverterTest {
  private DocxConverter converter;

  @BeforeEach
  void setup() {
    converter = new DocxConverter();
  }

  @Test
  void testConvert_withTbl() {
    Tbl table = new Tbl();
    JAXBElement<Tbl> tblElement = new JAXBElement<>(new QName("table"), Tbl.class, table);

    var result = converter.convert(tblElement);

    assertTrue(result instanceof DocUnitTable);
  }

  @Test
  void testConvert_withP() {
    P paragraph = new P();
    R run = new R();
    Text text = new Text();
    text.setValue("text");
    JAXBElement<Text> element = new JAXBElement<>(new QName("text"), Text.class, text);
    run.getContent().add(element);
    paragraph.getContent().add(run);

    var result = converter.convert(paragraph);

    assertTrue(result instanceof DocUnitTextElement);
  }

  @Test
  void textSetStyles() {
    converter.setStyles(Collections.emptyMap());
  }

  @Test
  void textSetImages() {
    converter.setImages(Collections.emptyMap());
  }

  @Test
  void testConvert_withUnknownElement() {

    var result = converter.convert(new Object());

    assertEquals("unknown element: java.lang.Object", result.toString());
    assertEquals(
        "<p><span style=\"color: #FF0000;\">unknown element: java.lang.Object</span></p>",
        result.toHtmlString());
  }
}
