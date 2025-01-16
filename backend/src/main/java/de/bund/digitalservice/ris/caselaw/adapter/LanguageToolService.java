package de.bund.digitalservice.ris.caselaw.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.bund.digitalservice.ris.caselaw.config.LanguageToolConfig;
import de.bund.digitalservice.ris.caselaw.domain.TextCorrectionService;
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

    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readTree(response.getBody());
  }
}
