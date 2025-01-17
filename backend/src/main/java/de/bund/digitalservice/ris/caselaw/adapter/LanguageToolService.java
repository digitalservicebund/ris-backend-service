package de.bund.digitalservice.ris.caselaw.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.bund.digitalservice.ris.caselaw.config.LanguageToolConfig;
import de.bund.digitalservice.ris.caselaw.domain.TextRange;
import de.bund.digitalservice.ris.caselaw.domain.languagetool.TextCorrectionService;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LanguageToolService implements TextCorrectionService {
  private final LanguageToolConfig languageToolConfig;

  private static final String NO_INDEX_ELEMENT = "noindex";

  public LanguageToolService(LanguageToolConfig languageToolConfig) {
    this.languageToolConfig = languageToolConfig;
  }

  @Override
  public JsonNode check(String text) throws JsonProcessingException {
    RestTemplate restTemplate = new RestTemplate();

    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", "application/json");
    headers.set("Content-Type", "application/x-www-form-urlencoded");

    String body =
        "text=" + text + "&language=" + languageToolConfig.getLanguage() + "&enabledOnly=false";

    HttpEntity<String> entity = new HttpEntity<>(body, headers);

    ResponseEntity<String> response =
        restTemplate.exchange(languageToolConfig.getUrl(), HttpMethod.POST, entity, String.class);

    ObjectMapper objectMapper = new ObjectMapper();

    return objectMapper.readValue(response.getBody(), JsonNode.class);
  }

  public static List<TextRange> findNoIndexPositions(Document doc) {
    return findNoIndexPositions(doc, doc.text());
  }

  public static List<TextRange> findNoIndexPositions(Document doc, String plainText) {
    List<TextRange> positions = new ArrayList<>();

    Elements noIndexElements = doc.select(LanguageToolService.NO_INDEX_ELEMENT);

    for (Element noIndexElement : noIndexElements) {
      String noIndexText = noIndexElement.text();

      int start = plainText.indexOf(noIndexText);
      if (start != -1) {
        int end = start + noIndexText.length();
        positions.add(TextRange.builder().start(start).end(end).text(noIndexText).build());
      }
    }

    return positions;
  }

  public void removeNoIndexMatches(String text) {
    // var noIndexMatches = getNoIndexTextRanges(text);
  }
}
