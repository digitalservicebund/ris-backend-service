package de.bund.digitalservice.ris.caselaw.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.bund.digitalservice.ris.caselaw.config.LanguageToolConfig;
import de.bund.digitalservice.ris.caselaw.domain.TextRange;
import de.bund.digitalservice.ris.caselaw.domain.languagetool.TextCorrectionService;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LanguageToolService implements TextCorrectionService {
  private final LanguageToolConfig languageToolConfig;

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

    getNoIndexTextRanges(text);
    ObjectMapper objectMapper = new ObjectMapper();

    return objectMapper.readValue(response.getBody(), JsonNode.class);
  }

  public List<TextRange> getNoIndexTextRanges(String text) {
    String regex = "<noindex>(.*?)</noindex>";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(text);
    List<TextRange> noIndexTextRanges = new ArrayList<>();
    while (matcher.find()) {
      noIndexTextRanges.add(TextRange.builder().start(matcher.start()).end(matcher.end()).build());
    }
    return noIndexTextRanges;
  }

  public void removeNoIndexMatches(String text) {
    var noIndexMatches = getNoIndexTextRanges(text);
  }
}
