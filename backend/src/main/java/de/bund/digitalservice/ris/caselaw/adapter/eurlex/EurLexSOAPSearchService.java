package de.bund.digitalservice.ris.caselaw.adapter.eurlex;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseCourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.EurLexResultDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.SearchResult;
import de.bund.digitalservice.ris.caselaw.domain.SearchService;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/** Implementation of the search service with a SOAP webservice to the EUR-Lex API. */
@Service
@Slf4j
public class EurLexSOAPSearchService implements SearchService {
  private static final int PAGE_SIZE = 100;

  @Value("${eurlex.username:test}")
  private String userName;

  @Value("${eurlex.password:test}")
  private String password;

  @Value("${eurlex.url:https://eur-lex.europa.eu/EURLexWebService?WSDL}")
  private String url;

  private final EurLexResultRepository repository;
  private final DatabaseCourtRepository courtRepository;
  private final EurlexRetrievalService eurlexRetrievalService;

  public EurLexSOAPSearchService(
      EurLexResultRepository repository,
      DatabaseCourtRepository courtRepository,
      EurlexRetrievalService eurlexRetrievalService) {
    this.repository = repository;
    this.courtRepository = courtRepository;
    this.eurlexRetrievalService = eurlexRetrievalService;
  }

  /**
   * Search the EUR-Lex API for european caselaw decisions.
   *
   * @param page - page number to filter the decisions. The entries per page are always 100. If the
   *     page parameter is null, the first page is requested.
   * @param fileNumber - filter option for the file number
   * @param celex - filter option for the celex number
   * @param court - filter option for the court
   * @param startDate - start date as filter for the publication date
   * @param endDate - end date as filter for the publication date
   * @return a page object with the 100 entries of the page
   */
  @Override
  public Page<SearchResult> getSearchResults(
      String page,
      DocumentationOffice documentationOffice,
      Optional<String> fileNumber,
      Optional<String> celex,
      Optional<String> court,
      Optional<LocalDate> startDate,
      Optional<LocalDate> endDate) {

    if (documentationOffice == null || documentationOffice.abbreviation() == null) {
      return new PageImpl<>(List.of(), PageRequest.of(0, PAGE_SIZE), 0);
    }

    if (!documentationOffice.abbreviation().equals("DS")
        && !documentationOffice.abbreviation().equals("BGH")
        && !documentationOffice.abbreviation().equals("BFH")) {
      return new PageImpl<>(List.of(), PageRequest.of(0, PAGE_SIZE), 0);
    }

    int pageNumber = 0;
    if (page != null) {
      try {
        pageNumber = Integer.parseInt(page);
      } catch (NumberFormatException ignored) {
        // ignore errors by number parsing
      }
    }

    if (court.isEmpty()) {
      if (documentationOffice.abbreviation().equals("BGH")) {
        court = Optional.of("EuG");
      } else if (documentationOffice.abbreviation().equals("BFH")) {
        court = Optional.of("EuGH");
      }
    }

    return repository
        .findAllNewWithUriBySearchParameters(
            PageRequest.of(pageNumber, PAGE_SIZE), fileNumber, celex, court, startDate, endDate)
        .map(EurLexSearchResultTransformer::transformDTOToDomain);
  }

  @Override
  public void updateResultStatus(List<String> celexNumbers) {
    List<EurLexResultDTO> existing = repository.findAllByCelexNumbers(celexNumbers);
    if (existing.isEmpty()) {
      return;
    }
    existing.forEach(eurLexResultDTO -> eurLexResultDTO.setStatus(EurLexResultStatus.ASSIGNED));
    repository.saveAll(existing);
  }

  @Scheduled(cron = "0 0 3 * * *", zone = "Europe/Berlin")
  @SchedulerLock(name = "eurlex-update", lockAtMostFor = "PT5M")
  public void requestNewestDecisions() {
    requestNewestDecisions(1);
  }

  @SuppressWarnings("java:S2142")
  private void requestNewestDecisions(int pageNumber) {
    Element searchResults;

    String response =
        eurlexRetrievalService.requestEurlexResultList(
            url, generatePayload(pageNumber, LocalDate.now().minusDays(90)));
    searchResults = extractSearchResultsFromResponse(response);

    if (searchResults != null) {
      Map<String, CourtDTO> courts = new HashMap<>();
      courts.put("EuGH", courtRepository.findByType("EuGH"));
      courts.put("EuG", courtRepository.findByType("EuG"));
      List<EurLexResultDTO> transformedList =
          EurLexSearchResultTransformer.transformXmlToDTO(searchResults, courts);

      transformedList.forEach(
          result -> {
            var existing = repository.findByCelexNumber(result.getCelex());
            if (existing
                .isPresent()) { // update the existing entry by overriding everything except the
              // status
              result.setId(existing.get().getId());
              result.setStatus(existing.get().getStatus());
            }
          });

      repository.saveAll(transformedList);

      int totalNum = EurLexSearchResultTransformer.getTotalNum(searchResults);
      if (totalNum > pageNumber * PAGE_SIZE) {
        requestNewestDecisions(pageNumber + 1);
      }
    }
  }

  private Element extractSearchResultsFromResponse(String response) {
    try {
      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      Document doc =
          documentBuilderFactory
              .newDocumentBuilder()
              .parse(new InputSource(new StringReader(response)));

      NodeList searchResultsList = doc.getElementsByTagName("searchResults");

      if (searchResultsList.getLength() == 1
          && searchResultsList.item(0) instanceof Element element) {
        return element;
      }

      return null;
    } catch (ParserConfigurationException | SAXException | IOException ex) {
      log.error("Can't get search results from EUR-Lex webservice.", ex);
      throw new EurLexSearchException(ex);
    }
  }

  private String generatePayload(int pageNumber, LocalDate lastUpdate) {
    return "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:sear=\"http://eur-lex.europa.eu/search\">"
        + "<soap:Header>"
        + "<wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" soap:mustUnderstand=\"true\">"
        + "<wsse:UsernameToken xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" wsu:Id=\"UsernameToken-1\">"
        + "<wsse:Username>"
        + userName
        + "</wsse:Username>"
        + "<wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\">"
        + password
        + "</wsse:Password>"
        + "</wsse:UsernameToken>"
        + "</wsse:Security>"
        + "</soap:Header>"
        + "<soap:Body>"
        + "<sear:searchRequest>"
        + "<sear:expertQuery><![CDATA["
        + "DTS_SUBDOM = EU_CASE_LAW"
        + " AND (FM_CODED = JUDG OR FM_CODED = OPIN_JUR OR FM_CODED = ORDER)"
        + " AND DD >= "
        + lastUpdate.format(DateTimeFormatter.ofPattern("dd/MM/yyy"))
        + " AND CASE_LAW_SUMMARY = false"
        + "]]></sear:expertQuery>"
        + "<sear:page>"
        + pageNumber
        + "</sear:page>"
        + "<sear:pageSize>"
        + PAGE_SIZE
        + "</sear:pageSize>"
        + "<sear:searchLanguage>de</sear:searchLanguage>"
        + "<sear:showDocumentsAvailableIn>de</sear:showDocumentsAvailableIn>"
        + "</sear:searchRequest>"
        + "</soap:Body>"
        + "</soap:Envelope>";
  }

  /**
   * Clean up the data that has been added for e2e tests, so that each e2e test-suite can run with a
   * clean slate state of the system.
   */
  @Override
  @Transactional
  public void cleanUpTestdata() {
    repository.deleteAllByCelexNumbers(List.of("62024CO0878", "62023CJ0538", "62019CV0001(02)"));
  }
}
