package de.bund.digitalservice.ris.caselaw.adapter.eurlex;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.EurLexResultDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.EurLexResultDTO.EurLexResultDTOBuilder;
import de.bund.digitalservice.ris.caselaw.domain.SearchResult;
import de.bund.digitalservice.ris.caselaw.domain.SearchResult.SearchResultBuilder;
import jakarta.transaction.TransactionalException;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Transformer for the transformation of EURLex search results from webservice */
@Slf4j
public class EurLexSearchResultTransformer {
  private EurLexSearchResultTransformer() {}

  /**
   * Transform the database object to the domain object
   *
   * @param eurLexResultDTO - database representation of the search result
   * @return domain object of the search result
   */
  public static SearchResult transformDTOToDomain(EurLexResultDTO eurLexResultDTO) {
    if (eurLexResultDTO == null) {
      return null;
    }

    SearchResultBuilder builder =
        SearchResult.builder()
            .celex(eurLexResultDTO.getCelex())
            .ecli(eurLexResultDTO.getEcli())
            .date(eurLexResultDTO.getDate())
            .fileNumber(eurLexResultDTO.getFileNumber())
            .fileNumber(eurLexResultDTO.getFileNumber())
            .htmlLink(eurLexResultDTO.getHtmlLink())
            .uri(eurLexResultDTO.getUri());

    if (eurLexResultDTO.getCreatedAt() == null) {
      log.error("No created at date found. Should be set by database.");
    } else {
      builder.publicationDate(
          LocalDate.ofInstant(eurLexResultDTO.getCreatedAt(), ZoneId.of("Europe/Berlin")));
    }

    if (eurLexResultDTO.getCourt() != null) {
      builder
          .courtType(eurLexResultDTO.getCourt().getType())
          .courtLocation(eurLexResultDTO.getCourt().getLocation());
    }

    return builder.build();
  }

  private static String parseFileNumberFromTitle(String title) {
    Pattern tPattern = Pattern.compile("T-(\\d*)/(\\d*)(\u00A0[a-zA-Z]+)?");
    Matcher tMatcher = tPattern.matcher(title);
    Pattern cPattern = Pattern.compile("C-(\\d*)/(\\d*)(\u00A0[a-zA-Z]+)?");
    Matcher cMatcher = cPattern.matcher(title);

    List<String> fileNumbers = new ArrayList<>();
    tMatcher.results().forEach(result -> fileNumbers.add(result.group()));
    cMatcher.results().forEach(result -> fileNumbers.add(result.group()));
    if (!fileNumbers.isEmpty()) {
      return String.join(", ", fileNumbers);
    }

    return null;
  }

  public static List<EurLexResultDTO> transformXmlToDTO(
      Element searchResults, Map<String, CourtDTO> courts) {
    if (searchResults == null) {
      return Collections.emptyList();
    }

    NodeList nodeList = searchResults.getElementsByTagName("result");
    List<EurLexResultDTO> results = new ArrayList<>();
    for (int i = 0; i < nodeList.getLength(); i++) {
      EurLexResultDTOBuilder builder = EurLexResultDTO.builder();
      parseContent(nodeList.item(i), builder, courts);
      parseDocumentLink(nodeList.item(i), builder);
      builder.status(EurLexResultStatus.NEW);
      builder.resultXml(getXmlAsString(nodeList.item(i)));

      results.add(builder.build());
    }

    return results;
  }

  private static String getXmlAsString(Node node) {
    try {
      TransformerFactory transFactory = TransformerFactory.newInstance();
      Transformer transformer = transFactory.newTransformer();
      StringWriter buffer = new StringWriter();
      transformer.setOutputProperty(OutputKeys.INDENT, "no");
      transformer.transform(new DOMSource(node), new StreamResult(buffer));
      return buffer.toString();
    } catch (TransactionalException | TransformerException ex) {
      log.error("Error while transforming XML", ex);
    }

    return null;
  }

  /**
   * Extract the total number of search results from the xml of the webservice
   *
   * @param searchResults - dom element for the list of search results
   * @return total number of search results, 0 if no total hits element exist or multiple
   *     representation of the element exists or the number couldn't be parsed
   */
  public static int getTotalNum(Element searchResults) {
    NodeList totalHits = searchResults.getElementsByTagName("totalhits");

    if (totalHits.getLength() == 1) {
      try {
        return Integer.parseInt(totalHits.item(0).getTextContent());
      } catch (NumberFormatException ex) {
        // format parsing exception is ignored
      }
    }

    return 0;
  }

  private static void parseDocumentLink(Node result, EurLexResultDTOBuilder builder) {
    XPath xPath = XPathFactory.newDefaultInstance().newXPath();

    try {
      String htmlDocumentLink = xPath.compile("./document_link[@type='html']").evaluate(result);
      if (Strings.isNotBlank(htmlDocumentLink)) {
        builder.htmlLink(htmlDocumentLink);
      }
    } catch (XPathExpressionException e) {
      throw new EurLexSearchException("Html link of the result couldn't be parsed", e);
    }
  }

  private static void parseContent(
      Node result, EurLexResultDTOBuilder builder, Map<String, CourtDTO> courts) {
    XPathFactory factory = XPathFactory.newInstance();
    XPath xPath = factory.newXPath();

    try {
      String title =
          xPath.compile("./content/NOTICE/EXPRESSION/EXPRESSION_TITLE/VALUE").evaluate(result);
      if (Strings.isNotBlank(title)) {
        String fileNumber = parseFileNumberFromTitle(title);
        builder.fileNumber(fileNumber);
      }
    } catch (XPathExpressionException e) {
      throw new EurLexSearchException("Title of the result couldn't be parsed", e);
    }

    try {
      String ecli = xPath.compile("./content/NOTICE/WORK/ECLI/VALUE").evaluate(result);
      if (Strings.isNotBlank(ecli)) {
        builder.ecli(ecli);
      }
    } catch (XPathExpressionException e) {
      throw new EurLexSearchException("ECLI of the result couldn't be parsed", e);
    }

    String celex = null;
    try {
      celex = xPath.compile("./content/NOTICE/WORK/ID_CELEX/VALUE").evaluate(result);
      if (Strings.isNotBlank(celex)) {
        builder.celex(celex);
      }
    } catch (XPathExpressionException e) {
      throw new EurLexSearchException("Celex number of the result couldn't be parsed", e);
    }

    try {
      String courtId =
          xPath.compile("./content/NOTICE/WORK/WORK_CREATED_BY_AGENT/IDENTIFIER").evaluate(result);
      if (courtId.equals("CJ")) {
        builder.court(courts.get("EuGH"));
      } else if (courtId.equals("GCEU")) {
        builder.court(courts.get("EuG"));
      }
    } catch (XPathExpressionException e) {
      throw new EurLexSearchException("Court of the result couldn't be parsed", e);
    }

    try {
      String dayString =
          xPath.compile("./content/NOTICE/WORK/WORK_DATE_DOCUMENT/DAY").evaluate(result);
      int day = Integer.parseInt(dayString);
      String monthString =
          xPath.compile("./content/NOTICE/WORK/WORK_DATE_DOCUMENT/MONTH").evaluate(result);
      Month month = Month.of(Integer.parseInt(monthString));
      String yearString =
          xPath.compile("./content/NOTICE/WORK/WORK_DATE_DOCUMENT/YEAR").evaluate(result);
      int year = Integer.parseInt(yearString);
      builder.date(LocalDate.of(year, month, day));
    } catch (XPathExpressionException | NumberFormatException e) {
      throw new EurLexSearchException("Decision date of the result couldn't be parsed", e);
    }

    try {
      NodeList manifestations =
          (NodeList)
              xPath
                  .compile("./content//MANIFESTATION/SAMEAS/URI/VALUE")
                  .evaluate(result, XPathConstants.NODESET);
      for (int i = 0; i < manifestations.getLength(); i++) {
        if (manifestations
            .item(i)
            .getTextContent()
            .contains("/celex/" + URLEncoder.encode(celex, StandardCharsets.UTF_8) + ".DEU.fmx4")) {
          builder.uri(manifestations.item(i).getTextContent());
        }
      }
    } catch (XPathExpressionException e) {
      throw new EurLexSearchException("FMX4 manifestation of the result couldn't be parsed", e);
    }
  }
}
