package de.bund.digitalservice.ris.caselaw.adapter.eurlex;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.EurLexResultDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.EurLexResultDTO.EurLexResultDTOBuilder;
import de.bund.digitalservice.ris.caselaw.domain.SearchResult;
import jakarta.transaction.TransactionalException;
import java.io.StringWriter;
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
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Slf4j
public class EurLexSearchResultTransformer {
  private EurLexSearchResultTransformer() {}

  public static SearchResult transformDTOToDomain(EurLexResultDTO eurLexResultDTO) {
    if (eurLexResultDTO == null) {
      return null;
    }

    String fileNumber = parseFileNumberFromTitle(eurLexResultDTO.getTitle());

    return SearchResult.builder()
        .celex(eurLexResultDTO.getCelex())
        .ecli(eurLexResultDTO.getEcli())
        .courtType(eurLexResultDTO.getCourt().getType())
        .courtLocation(eurLexResultDTO.getCourt().getLocation())
        .date(eurLexResultDTO.getDate())
        .fileNumber(fileNumber)
        .publicationDate(
            LocalDate.ofInstant(eurLexResultDTO.getCreatedAt(), ZoneId.of("Europe/Berlin")))
        .title(eurLexResultDTO.getTitle())
        .htmlLink(eurLexResultDTO.getHtmlLink())
        .uri(eurLexResultDTO.getUri())
        .build();
  }

  private static String parseFileNumberFromTitle(String title) {
    Pattern tPattern = Pattern.compile("T-(\\d*)/(\\d*)(\u00A0\\w)?");
    Matcher tMatcher = tPattern.matcher(title);
    Pattern cPattern = Pattern.compile("C-(\\d*)/(\\d*)(\u00A0\\w)?");
    Matcher cMatcher = cPattern.matcher(title);

    List<String> fileNumbers = new ArrayList<>();
    tMatcher.results().forEach(result -> fileNumbers.add(result.group()));
    cMatcher.results().forEach(result -> fileNumbers.add(result.group()));
    return String.join(", ", fileNumbers);
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

  public static int getTotalNum(Element searchResults) {
    NodeList totalHits = searchResults.getElementsByTagName("totalhits");
    if (totalHits.getLength() == 1) {
      try {
        return Integer.parseInt(totalHits.item(0).getTextContent());
      } catch (NumberFormatException ignored) {
      }
    }

    return 0;
  }

  private static Pageable getPageable(Element searchResults) {
    NodeList numHits = searchResults.getElementsByTagName("numhits");
    NodeList page = searchResults.getElementsByTagName("page");

    if (numHits.getLength() == 1 && page.getLength() == 1) {
      try {
        int pageValue = Integer.parseInt(page.item(0).getTextContent());
        int numHitsValue = Integer.parseInt(numHits.item(0).getTextContent());

        return PageRequest.of(pageValue - 1, numHitsValue);
      } catch (NumberFormatException ignored) {
      }
    }

    return Pageable.unpaged();
  }

  private static void parseDocumentLink(Node result, EurLexResultDTOBuilder builder) {
    XPath xPath = XPathFactory.newDefaultInstance().newXPath();

    try {
      String htmlDocumentLink = xPath.compile("./document_link[@type='html']").evaluate(result);
      builder.htmlLink(htmlDocumentLink);
    } catch (XPathExpressionException e) {
      throw new RuntimeException(e);
    }
  }

  private static void parseContent(
      Node result, EurLexResultDTOBuilder builder, Map<String, CourtDTO> courts) {
    XPathFactory factory = XPathFactory.newInstance();
    XPath xPath = factory.newXPath();

    try {
      String title =
          xPath.compile("./content/NOTICE/EXPRESSION/EXPRESSION_TITLE/VALUE").evaluate(result);
      builder.title(title);
    } catch (XPathExpressionException e) {
      throw new RuntimeException(e);
    }

    try {
      String ecli = xPath.compile("./content/NOTICE/WORK/ECLI/VALUE").evaluate(result);
      builder.ecli(ecli);
    } catch (XPathExpressionException e) {
      throw new RuntimeException(e);
    }

    try {
      String celex = xPath.compile("./content/NOTICE/WORK/ID_CELEX/VALUE").evaluate(result);
      builder.celex(celex);
    } catch (XPathExpressionException e) {
      throw new RuntimeException(e);
    }

    try {
      String uri = xPath.compile("./content/NOTICE/WORK/URI/VALUE").evaluate(result);
      builder.uri(uri);
    } catch (XPathExpressionException e) {
      throw new RuntimeException(e);
    }

    try {
      String courtId =
          xPath.compile("./content/NOTICE/WORK/WORK_CREATED_BY_AGENT/IDENTIFIER").evaluate(result);
      switch (courtId) {
        case "CJ":
          builder.court(courts.get("EuGH"));
          break;
        case "GCEU":
          builder.court(courts.get("EuG"));
          break;
      }
    } catch (XPathExpressionException e) {
      throw new RuntimeException(e);
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
      throw new RuntimeException(e);
    }
  }
}
