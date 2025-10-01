package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.domain.FmxRepository;
import de.bund.digitalservice.ris.caselaw.domain.fmx.Fmx2Html;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.jaxp.SaxonTransformerFactory;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({FmxConverterService.class, FmxRepository.class, XmlUtilService.class})
class FmxConverterServiceTest {

  @MockitoBean FmxRepository fmxRepository;
  @MockitoBean XmlUtilService xmlUtilService;

  private final TransformerFactory transformerFactory = new SaxonTransformerFactory();

  private FmxConverterService service;

  @BeforeEach
  void setup() throws TransformerConfigurationException, IOException {
    service = new FmxConverterService(fmxRepository, xmlUtilService);
    ClassPathResource xsltResource = new ClassPathResource("xml/fmxToHtml.xslt");
    String fileContent = IOUtils.toString(xsltResource.getInputStream(), StandardCharsets.UTF_8);
    Templates templates =
        transformerFactory.newTemplates(new StreamSource(new StringReader(fileContent)));
    when(xmlUtilService.getTemplates("xml/fmxToHtml.xslt")).thenReturn(templates);
  }

  @Test
  @SuppressWarnings("java:S5961")
  void content_shouldReturnHtml() {
    UUID uuid = UUID.randomUUID();
    when(fmxRepository.getFmxAsString(uuid)).thenReturn(xml);

    var result = service.getFmx(uuid);

    // all text content should be present with given styles where applicable
    assertThat(result.html())
        .contains("<span style=\"text-transform: uppercase;\">Urteil des Gerichtshofs</span>");
    assertThat(result.html())
        .contains("<p>„Keyword one&nbsp;– Keyword two&nbsp;– Keyword three“</p>");
    assertThat(result.html()).contains("<p>25.&nbsp;Januar 2024<p>");
    assertThat(result.html()).contains("<p>In der Rechtssache</p>");
    assertThat(result.html()).contains("<p><em>Plaintifs</em></p>");
    assertThat(result.html()).contains("gegen");
    assertThat(result.html()).contains("<p><u>Defendants</u></p>");
    assertThat(result.html()).contains("<p>erlässt</p>");
    assertThat(result.html()).contains("<p>DER GERICHTSHOF (Dritte Kammer)</p>");
    assertThat(result.html()).contains("aufgrund des schriftlichen Verfahrens");
    assertThat(result.html()).contains("<p>unter Berücksichtigung der Erklärungen</p>");
    assertThat(result.html()).contains("<ul style=\"list-style-type:'- ';\">");
    assertThat(result.html()).contains("<li><p>Item one</p></li>");
    assertThat(result.html()).contains("<li><p>Item two</p></li>");
    assertThat(result.html()).contains("</ul>");
    assertThat(result.html()).contains("folgendes");
    assertThat(result.html()).contains("<b>Urteil</b>");
    assertThat(result.html())
        .contains(
            "<border-number><number>1</number><content><p>border number 1 text content</p></content></border-number>");
    assertThat(result.html())
        .contains(
            "<p>Aus diesen Gründen hat der Gerichtshof (Dritte Kammer) für Recht erkannt:</p>");
    assertThat(result.html()).contains("<dt><b>1.</b></dt>");
    assertThat(result.html()).contains("<dd><p><sub>Numbered item one</sub></p></dd>");
    assertThat(result.html()).contains("<dt><b>2.</b></dt>");
    assertThat(result.html()).contains("<dd><p><sup>Numbered item two</sup></p></dd>");
    assertThat(result.html()).contains("<s>Unterschriften</s>");

    // BIB element is removed
    assertThat(result.html()).doesNotContain("C-687/21");
    assertThat(result.html()).doesNotContain("62021CJ0687");
    assertThat(result.html()).doesNotContain("EU:C:2024:72");
    assertThat(result.html()).doesNotContain("CJ");
    // CURR.TITLE is removed
    assertThat(result.html()).doesNotContain("Urteil vom 25. 1. 2024 – Rechtssache C&#8209;687/21");
  }

  @Test
  void empty_shouldReturnEmpty() {
    UUID uuid = UUID.randomUUID();
    when(fmxRepository.getFmxAsString(uuid)).thenReturn("");

    var result = service.getFmx(uuid);

    assertThat(result).isEqualTo(Fmx2Html.EMPTY);
  }

  @Test
  void null_shouldReturnEmpty() {
    UUID uuid = UUID.randomUUID();
    when(fmxRepository.getFmxAsString(uuid)).thenReturn(null);

    var result = service.getFmx(uuid);

    assertThat(result).isEqualTo(Fmx2Html.EMPTY);
  }

  private String xml =
"""
<?xml version="1.0" encoding="UTF-8"?>
<JUDGMENT xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:noNamespaceSchemaLocation="http://formex.publications.europa.eu/schema/formex-05.58-20161101.xd">
    <BIB.JUDGMENT>
        <REF.CASE FILE="n.a.">
            <NO.CASE>C-687/21</NO.CASE>
        </REF.CASE>
        <NO.CELEX>62021CJ0687</NO.CELEX>
        <NO.ECLI ECLI="ECLI:EU:C:2024:72">EU:C:2024:72</NO.ECLI>
        <AUTHOR>CJ</AUTHOR>
    </BIB.JUDGMENT>
    <CURR.TITLE>
        <PAGE.HEADER>
            <P>Urteil vom 25. 1. 2024 – Rechtssache C&#8209;687/21</P>
        </PAGE.HEADER>
    </CURR.TITLE>
    <TITLE>
        <TI>
            <P><HT TYPE="UC">Urteil des Gerichtshofs</HT></P>
            <P>
                <DATE ISO="20240125">25. Januar 2024</DATE>
                <NOTE TYPE="FOOTNOTE" NUMBERING="STAR" NOTE.ID="E0001">
                    <P>Verfahrenssprache: Deutsch.</P>
                </NOTE>
            </P>
        </TI>
    </TITLE>
    <INTERMEDIATE>
        <INDEX IDX.CLOSE="“" SEPARATOR=" – " IDX.OPEN="„">
            <KEYWORD>Keyword one</KEYWORD>
            <KEYWORD>Keyword two</KEYWORD>
            <KEYWORD>Keyword three</KEYWORD>
        </INDEX>
    </INTERMEDIATE>
    <JUDGMENT.INIT>
        <P>In der Rechtssache</P>
    </JUDGMENT.INIT>
    <PARTIES>
        <PLAINTIFS>
            <P><HT TYPE="ITALIC">Plaintifs</HT></P>
        </PLAINTIFS>
        <AGAINST>gegen</AGAINST>
        <DEFENDANTS>
            <P><HT TYPE="UNDERLINE">Defendants</HT></P>
        </DEFENDANTS>
    </PARTIES>
    <P>erlässt</P>
    <PREAMBLE>
        <PREAMBLE.INIT>
            <P>DER GERICHTSHOF (Dritte Kammer)</P>
        </PREAMBLE.INIT>
        <GR.VISA>
            <VISA>aufgrund des schriftlichen Verfahrens,</VISA>
        </GR.VISA>
        <GR.CONSID>
            <CONSID>
                <P>unter Berücksichtigung der Erklärungen</P>
                <LIST TYPE="NDASH">
                    <ITEM><P>Item one</P></ITEM>
                    <ITEM><P>Item two</P></ITEM>
                </LIST>
            </CONSID>
        </GR.CONSID>
        <PREAMBLE.FINAL>folgendes</PREAMBLE.FINAL>
    </PREAMBLE>
    <CONTENTS.JUDGMENT>
        <GR.SEQ LEVEL="1">
            <TITLE>
                <TI>
                    <P>
                        <HT TYPE="BOLD">Urteil</HT>
                    </P>
                </TI>
            </TITLE>
            <NP.ECR IDENTIFIER="NP0001">
                <NO.P>1</NO.P>
                <TXT>border number 1 text content</TXT>
            </NP.ECR>
            <NP.ECR IDENTIFIER="NP0002">
                <NO.P>2</NO.P>
                <TXT>border number 2 text content</TXT>
            </NP.ECR>
        </GR.SEQ>
        <JURISDICTION>
            <INTRO>Aus diesen Gründen hat der Gerichtshof (Dritte Kammer) für Recht erkannt:</INTRO>
            <LIST TYPE="ARAB">
                <ITEM>
                    <NP>
                        <NO.P>
                            <HT TYPE="BOLD">1.</HT>
                        </NO.P>
                        <TXT><HT TYPE="SUB">Numbered item one</HT></TXT>
                    </NP>
                </ITEM>
                <ITEM>
                    <NP>
                        <NO.P>
                            <HT TYPE="BOLD">2.</HT>
                        </NO.P>
                        <P>
                            <HT TYPE="SUP">Numbered item two</HT>
                        </P>
                    </NP>
                </ITEM>
            </LIST>
        </JURISDICTION>
    </CONTENTS.JUDGMENT>
    <SIGNATURE.CASE>
        <SIGNATORY>
            <P><HT TYPE="STROKE">Unterschriften</HT></P>
        </SIGNATORY>
    </SIGNATURE.CASE>
</JUDGMENT>
      """;
}
