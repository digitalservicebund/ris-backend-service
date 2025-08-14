package de.bund.digitalservice.ris.caselaw.adapter.eurlex;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.EurLexResultDTO;
import de.bund.digitalservice.ris.caselaw.domain.SearchResult;
import java.io.IOException;
import java.io.StringReader;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@Slf4j
class EurLexSearchResultTransformerTest {
  @Test
  void testTransformDTOToDomain() {
    Instant now = Instant.now();
    EurLexResultDTO dto =
        EurLexResultDTO.builder()
            .resultXml("<xml><result>s/result>")
            .celex("celex")
            .court(CourtDTO.builder().type("court-type").location("court-location").build())
            .uri("uri")
            .date(LocalDate.of(2024, Month.DECEMBER, 24))
            .ecli("ecli")
            .htmlLink("html-link")
            .status(EurLexResultStatus.NEW)
            .fileNumber("T-123/45")
            .createdAt(now)
            .build();
    SearchResult expected =
        SearchResult.builder()
            .courtType("court-type")
            .courtLocation("court-location")
            .uri("uri")
            .celex("celex")
            .htmlLink("html-link")
            .ecli("ecli")
            .fileNumber("T-123/45")
            .date(LocalDate.of(2024, Month.DECEMBER, 24))
            .publicationDate(now)
            .build();

    SearchResult result = EurLexSearchResultTransformer.transformDTOToDomain(dto);

    assertThat(result).isEqualTo(expected);
  }

  @Test
  void testTransformDTOToDomain_withDTOisNull_returnsNull() {
    SearchResult result = EurLexSearchResultTransformer.transformDTOToDomain(null);

    assertThat(result).isNull();
  }

  @Test
  void testTransformXmlToDTO_withOnlyDecisionDateSet_shouldLetAllOtherValuesEmpty()
      throws ParserConfigurationException, IOException, SAXException {
    String resultXml =
        "<result>"
            + "<document_link type=\"html\">html-link</document_link>"
            + "<content><NOTICE><WORK><WORK_DATE_DOCUMENT>"
            + "<DAY>12</DAY>"
            + "<MONTH>12</MONTH>"
            + "<YEAR>2024</YEAR>"
            + "</WORK_DATE_DOCUMENT></WORK></NOTICE></content>"
            + "</result>";
    Document doc =
        DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(
                new InputSource(
                    new StringReader("<searchResults>" + resultXml + "</searchResults>")));
    Element element = doc.getDocumentElement();
    EurLexResultDTO expected =
        EurLexResultDTO.builder()
            .htmlLink("html-link")
            .date(LocalDate.of(2024, Month.DECEMBER, 12))
            .status(EurLexResultStatus.NEW)
            .resultXml("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + resultXml)
            .build();

    List<EurLexResultDTO> dtos =
        EurLexSearchResultTransformer.transformXmlToDTO(element, Collections.emptyMap());

    assertThat(dtos).hasSize(1);
    EurLexResultDTO dto = dtos.get(0);
    assertThat(dto).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  void testTransformXmlToDTO_withAllPossibleValuesForEuGH_shouldTakeOverAllValues()
      throws ParserConfigurationException, IOException, SAXException {
    String resultXml =
        "<result>"
            + "<document_link type=\"html\">html-link</document_link>"
            + "<content><NOTICE>"
            + "<WORK>"
            + "<WORK_DATE_DOCUMENT>"
            + "<DAY>12</DAY>"
            + "<MONTH>12</MONTH>"
            + "<YEAR>2024</YEAR>"
            + "</WORK_DATE_DOCUMENT>"
            + "<ECLI><VALUE>ecli</VALUE></ECLI>"
            + "<ID_CELEX><VALUE>celex123</VALUE></ID_CELEX>"
            + "<URI><VALUE>uri</VALUE></URI>"
            + "<WORK_CREATED_BY_AGENT><IDENTIFIER>CJ</IDENTIFIER></WORK_CREATED_BY_AGENT>"
            + "</WORK>"
            + "<EXPRESSION>"
            + "<EXPRESSION_TITLE><VALUE>title C-01/20</VALUE></EXPRESSION_TITLE>"
            + "</EXPRESSION>"
            + "<MANIFESTATION>"
            + "<SAMEAS>"
            + "<URI><VALUE>http://publications.europa.eu/resource/celex/celex123.DEU.fmx4</VALUE></URI>"
            + "</SAMEAS>"
            + "</MANIFESTATION>"
            + "</NOTICE></content>"
            + "</result>";
    Document doc =
        DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(
                new InputSource(
                    new StringReader("<searchResults>" + resultXml + "</searchResults>")));
    Element element = doc.getDocumentElement();
    CourtDTO court = CourtDTO.builder().type("court-type").location("court-location").build();
    EurLexResultDTO expected =
        EurLexResultDTO.builder()
            .htmlLink("html-link")
            .date(LocalDate.of(2024, Month.DECEMBER, 12))
            .status(EurLexResultStatus.NEW)
            .resultXml("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + resultXml)
            .ecli("ecli")
            .uri("http://publications.europa.eu/resource/celex/celex123.DEU.fmx4")
            .court(court)
            .celex("celex123")
            .fileNumber("C-01/20")
            .build();

    List<EurLexResultDTO> dtos =
        EurLexSearchResultTransformer.transformXmlToDTO(element, Map.of("EuGH", court));

    assertThat(dtos).hasSize(1);
    EurLexResultDTO dto = dtos.get(0);
    assertThat(dto).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  void testTransformXmlToDTO_withCourtEuG_shouldAddTheDatabaseObjectForTheCourt()
      throws ParserConfigurationException, IOException, SAXException {
    String resultXml =
        "<result>"
            + "<content><NOTICE>"
            + "<WORK>"
            + "<WORK_DATE_DOCUMENT>"
            + "<DAY>12</DAY>"
            + "<MONTH>12</MONTH>"
            + "<YEAR>2024</YEAR>"
            + "</WORK_DATE_DOCUMENT>"
            + "<WORK_CREATED_BY_AGENT><IDENTIFIER>GCEU</IDENTIFIER></WORK_CREATED_BY_AGENT>"
            + "</WORK>"
            + "</NOTICE></content>"
            + "</result>";
    Document doc =
        DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(
                new InputSource(
                    new StringReader("<searchResults>" + resultXml + "</searchResults>")));
    Element element = doc.getDocumentElement();
    CourtDTO court = CourtDTO.builder().type("court-type").location("court-location").build();
    EurLexResultDTO expected =
        EurLexResultDTO.builder()
            .date(LocalDate.of(2024, Month.DECEMBER, 12))
            .status(EurLexResultStatus.NEW)
            .resultXml("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + resultXml)
            .court(court)
            .build();

    List<EurLexResultDTO> dtos =
        EurLexSearchResultTransformer.transformXmlToDTO(element, Map.of("EuG", court));

    assertThat(dtos).hasSize(1);
    EurLexResultDTO dto = dtos.get(0);
    assertThat(dto).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  void testTransformXmlToDTO_withXmlIsNull_returnsAnEmptyList() {
    List<EurLexResultDTO> dtos =
        EurLexSearchResultTransformer.transformXmlToDTO(null, Collections.emptyMap());

    assertThat(dtos).isEmpty();
  }

  @Test
  void testTransformXmlToDTO_withoutDecisionDate_shouldThrowAnException()
      throws ParserConfigurationException, IOException, SAXException {
    String resultXml = "<result>" + "</result>";
    Document doc =
        DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(
                new InputSource(
                    new StringReader("<searchResults>" + resultXml + "</searchResults>")));
    Element element = doc.getDocumentElement();
    Map<String, CourtDTO> courts = new HashMap<>();

    assertThatThrownBy(() -> EurLexSearchResultTransformer.transformXmlToDTO(element, courts))
        .isInstanceOf(EurLexSearchException.class)
        .hasMessage("Decision date of the result couldn't be parsed");
  }

  @ParameterizedTest
  @CsvSource({
    "title with file number T-123/45 and text behind, T-123/45",
    "title with file number C-123/45 and text behind, C-123/45",
    "title with file number T-123/45\u00A0abc and text behind, T-123/45\u00A0abc",
    "title with file number C-123/45\u00A0abc and text behind, C-123/45\u00A0abc",
    "title with file number T-123/45\u00A0abc and T-987/65\u00A0zyx and text behind, 'T-123/45\u00A0abc, T-987/65\u00A0zyx'",
    "title with file number C-123/45\u00A0abc and C-987/65\u00A0zyx and text behind, 'C-123/45\u00A0abc, C-987/65\u00A0zyx'",
    "title with file number T-12/345 abc and text behind, T-12/345",
    "title with file number T-12/345\u00A0123 and text behind, T-12/345",
  })
  void testTransformXmlToDTO_withFileNumberWithAllowedPattern_hasParsedFileNumber(
      String title, String fileNumbers)
      throws ParserConfigurationException, IOException, SAXException {
    String resultXml =
        "<result>"
            + "<content><NOTICE><WORK><WORK_DATE_DOCUMENT>"
            + "<DAY>12</DAY>"
            + "<MONTH>12</MONTH>"
            + "<YEAR>2024</YEAR>"
            + "</WORK_DATE_DOCUMENT></WORK>"
            + "<EXPRESSION>"
            + "<EXPRESSION_TITLE><VALUE>"
            + title
            + "</VALUE></EXPRESSION_TITLE>"
            + "</EXPRESSION></NOTICE></content>"
            + "</result>";
    Document doc =
        DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(
                new InputSource(
                    new StringReader("<searchResults>" + resultXml + "</searchResults>")));
    Element element = doc.getDocumentElement();

    List<EurLexResultDTO> dtos =
        EurLexSearchResultTransformer.transformXmlToDTO(element, Collections.emptyMap());

    assertThat(dtos).hasSize(1);
    EurLexResultDTO dto = dtos.get(0);
    assertThat(dto.getFileNumber()).isEqualTo(fileNumbers);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "title with file number T-12345\u00A0abc and text behind",
        "title with file number T-12a45\u00A0abc and text behind"
      })
  void testTransformXmlToDTO_withFileNumberWithWrongFormat_hasNoFileNumberInDTO(String title)
      throws ParserConfigurationException, IOException, SAXException {
    String resultXml =
        "<result>"
            + "<content><NOTICE><WORK><WORK_DATE_DOCUMENT>"
            + "<DAY>12</DAY>"
            + "<MONTH>12</MONTH>"
            + "<YEAR>2024</YEAR>"
            + "</WORK_DATE_DOCUMENT></WORK>"
            + "<EXPRESSION>"
            + "<EXPRESSION_TITLE><VALUE>"
            + title
            + "</VALUE></EXPRESSION_TITLE>"
            + "</EXPRESSION></NOTICE></content>"
            + "</result>";
    Document doc =
        DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(
                new InputSource(
                    new StringReader("<searchResults>" + resultXml + "</searchResults>")));
    Element element = doc.getDocumentElement();

    List<EurLexResultDTO> dtos =
        EurLexSearchResultTransformer.transformXmlToDTO(element, Collections.emptyMap());

    assertThat(dtos).hasSize(1);
    EurLexResultDTO dto = dtos.get(0);
    assertThat(dto.getFileNumber()).isNull();
  }

  @Test
  void testGetTotalNum() throws ParserConfigurationException, IOException, SAXException {
    String resultXml = "<result>" + "<totalhits>56</totalhits>" + "</result>";
    Document doc =
        DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(
                new InputSource(
                    new StringReader("<searchResults>" + resultXml + "</searchResults>")));
    Element element = doc.getDocumentElement();

    int result = EurLexSearchResultTransformer.getTotalNum(element);

    assertThat(result).isEqualTo(56);
  }

  @Test
  void testGetTotalNum_withNotParsableNumber_shouldReturnZero()
      throws ParserConfigurationException, IOException, SAXException {
    String resultXml = "<result>" + "<totalhits>abc</totalhits>" + "</result>";
    Document doc =
        DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(
                new InputSource(
                    new StringReader("<searchResults>" + resultXml + "</searchResults>")));
    Element element = doc.getDocumentElement();

    int result = EurLexSearchResultTransformer.getTotalNum(element);

    assertThat(result).isZero();
  }

  @Test
  void testGetTotalNum_withMultipleTotalHitsValues_shouldReturnZero()
      throws ParserConfigurationException, IOException, SAXException {
    String resultXml =
        "<result>" + "<totalhits>123</totalhits>" + "<totalhits>456</totalhits>" + "</result>";
    Document doc =
        DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(
                new InputSource(
                    new StringReader("<searchResults>" + resultXml + "</searchResults>")));
    Element element = doc.getDocumentElement();

    int result = EurLexSearchResultTransformer.getTotalNum(element);

    assertThat(result).isZero();
  }
}
