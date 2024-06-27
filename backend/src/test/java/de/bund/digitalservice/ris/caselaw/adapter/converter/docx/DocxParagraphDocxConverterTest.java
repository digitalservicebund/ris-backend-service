package de.bund.digitalservice.ris.caselaw.adapter.converter.docx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.bund.digitalservice.ris.caselaw.domain.docx.TableElement;
import de.bund.digitalservice.ris.caselaw.domain.docx.TextElement;
import jakarta.xml.bind.JAXBElement;
import java.math.BigInteger;
import javax.xml.namespace.QName;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.R;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Text;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DocxParagraphDocxConverterTest {
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

    assertTrue(result instanceof TableElement);
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

    assertTrue(result instanceof TextElement);
  }

  @ParameterizedTest
  @CsvSource({"0, 0", "1, 40", "720, 40", "721, 80", "1440, 80", "1441, 120"})
  void testConvert_withIndentedP(BigInteger indentationInTwips, int expectedMarginLeft) {
    P paragraph = new P();
    PPr pPr = new PPr();
    PPrBase.Ind ind = new PPrBase.Ind();
    ind.setLeft(indentationInTwips);
    pPr.setInd(ind);
    paragraph.setPPr(pPr);

    var result = converter.convert(paragraph);

    assertTrue(result.toHtmlString().contains("margin-left: " + expectedMarginLeft + ".0px"));
  }

  @Test
  void testConvert_withUnknownElement() {

    var result = converter.convert(new Object());

    assertEquals("unknown element: java.lang.Object", result.toString());
  }
}
