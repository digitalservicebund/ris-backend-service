package de.bund.digitalservice.ris.caselaw.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.bund.digitalservice.ris.caselaw.domain.TextRange;
import de.bund.digitalservice.ris.caselaw.domain.languagetool.LanguageToolResponse;
import de.bund.digitalservice.ris.caselaw.domain.languagetool.Match;
import de.bund.digitalservice.ris.caselaw.domain.languagetool.TextCorrectionService;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
  public LanguageToolResponse check(String text) throws JsonProcessingException {
    RestTemplate restTemplate = new RestTemplate();

    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", "application/json");
    headers.set("Content-Type", "application/x-www-form-urlencoded");

    String body =
        "text=" + text + "&language=" + languageToolConfig.getLanguage() + "&enabledOnly=false";

    HttpEntity<String> entity = new HttpEntity<>(body, headers);

    ResponseEntity<LanguageToolResponse> response =
        restTemplate.exchange(
            languageToolConfig.getUrl(), HttpMethod.POST, entity, LanguageToolResponse.class);

    if (Objects.requireNonNull(response.getBody()).getMatches() != null) {
      for (Match match : response.getBody().getMatches()) {
        addTextContentToMatch(match, text);
      }
    }

    return response.getBody();
  }

  public void addTextContentToMatch(Match match, String text) {
    match.setTextContent(
        decodeText(text).substring(match.getOffset(), match.getOffset() + match.getLength()));
  }

  public String decodeText(String encodedText) {
    try {
      return URLDecoder.decode(encodedText, StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new RuntimeException("Failed to decode text", e);
    }.
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

  public static boolean matchIsBetweenNoIndexPosition(
      Match match, List<TextRange> noIndexPositions) {
    for (TextRange noIndexPosition : noIndexPositions) {
      if (noIndexPosition.start() < match.getOffset() && noIndexPosition.end() < match.end()) {
        return true;
      }
    }
    return false;
  }

  public static List<Match> removeNoIndexMatches(
      List<Match> matches, Document doc, String plainText) {
    var noIndexPositions = findNoIndexPositions(doc, plainText);

    List<Match> list = new ArrayList<>();
    for (Match match : matches) {
      if (!matchIsBetweenNoIndexPosition(match, noIndexPositions)) {
        list.add(match);
      }
    }
    return list;
  }
}
