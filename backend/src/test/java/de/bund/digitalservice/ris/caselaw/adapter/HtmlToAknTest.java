package de.bund.digitalservice.ris.caselaw.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.AknKeyword;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Classification;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Header;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.JaxbHtml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Judgment;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.JudgmentBody;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Meta;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Title;
import jakarta.xml.bind.JAXB;
import java.io.StringWriter;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

class HtmlToAknTest {

  private static final String AKN_START =
      """
          <?xml version="1.0" encoding="utf-8"?>
          <akn:akomaNtoso xmlns:akn="http://docs.oasis-open.org/legaldocml/ns/akn/3.0/WD17"
                          xmlns:ris="http://example.com/0.1/"
                          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                          xsi:schemaLocation="http://docs.oasis-open.org/legaldocml/ns/akn/3.0/WD17 https://docs.oasis-open.org/legaldocml/akn-core/v1.0/csprd02/part2-specs/schemas/akomantoso30.xsd">
             <akn:judgment name="attributsemantik-noch-undefiniert">
          """;
  private static final String AKN_END =
      """
             </akn:judgment>
          </akn:akomaNtoso>
          """;

  private static String getAkn(Judgment judgment) {
    CaseLawLdml caseLawLdml = CaseLawLdml.builder().judgment(judgment).build();
    StringWriter writer = new StringWriter();
    JAXB.marshal(caseLawLdml, writer);
    return XmlUtils.xsltTransform(
        XmlUtils.getTemplates("caselawhandover/htmlToAknHtml.xslt"), writer.toString());
  }

  @Test
  void borderNumberInIntroTransformationTest() {

    String inputHtml =
        """
                <border-number>
                    <number>1</number>
                    <content>
                      <p>Lorem ipsum</p>
                    </content>
                </border-number>
                """;

    String expectedXml =
        """
                <akn:judgmentBody>
                 <akn:introduction>
                    <akn:hcontainer name="randnummer">
                       <akn:num>1</akn:num>
                       <akn:content>
                         <akn:p>Lorem ipsum</akn:p>
                       </akn:content>
                    </akn:hcontainer>
                 </akn:introduction>
               </akn:judgmentBody>
          """;

    String result =
        getAkn(
            Judgment.builder()
                .judgmentBody(
                    JudgmentBody.builder().introduction(JaxbHtml.build(inputHtml)).build())
                .build());

    assertEquals(
        StringUtils.deleteWhitespace(AKN_START + expectedXml + AKN_END),
        StringUtils.deleteWhitespace(result));
  }

  @Test
  void mixedTextInHeaderTest() {

    String inputHtml = "Hello<p> paragraph</p> world!";

    String expectedXml =
        """
              <akn:header>
                 <akn:block name="title">
                    <akn:docTitle>
                       <akn:subFlow name="titleWrapper">
                          <akn:p alternativeTo="textWrapper">Hello</akn:p>
                          <akn:p> paragraph</akn:p>
                          <akn:p alternativeTo="textWrapper"> world!</akn:p>
                       </akn:subFlow>
                    </akn:docTitle>
                 </akn:block>
              </akn:header>
              """;
    Header header = new Header().withBlock("title", Title.build(inputHtml));
    String result = getAkn(Judgment.builder().header(header).build());

    assertEquals(
        StringUtils.deleteWhitespace(AKN_START + expectedXml + AKN_END),
        StringUtils.deleteWhitespace(result));
  }

  @Test
  void keyword() {

    String keyword = "keyword1";

    String expectedXml =
        """
              <akn:meta>
                 <akn:classification source="attributsemantik-noch-undefiniert">
                    <akn:keyword dictionary="attributsemantik-noch-undefiniert"
                                 showAs="attributsemantik-noch-undefiniert"
                                 value="keyword1"/>
                 </akn:classification>
              </akn:meta>
              """;
    AknKeyword aknKeyword = new AknKeyword(keyword);
    String result =
        getAkn(
            Judgment.builder()
                .meta(
                    Meta.builder()
                        .classification(
                            Classification.builder().keyword(List.of(aknKeyword)).build())
                        .build())
                .build());

    assertEquals(
        StringUtils.deleteWhitespace(AKN_START + expectedXml + AKN_END),
        StringUtils.deleteWhitespace(result));
  }
}
