package de.bund.digitalservice.ris.caselaw.adapter.languagetool;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.TextCheckResponseTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.TextCheckService;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.Match;
import java.util.List;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class LanguageToolService extends TextCheckService {
  private final LanguageToolConfig languageToolConfig;

  public LanguageToolService(
      LanguageToolConfig languageToolConfig, DocumentationUnitService documentationUnitService) {

    super(documentationUnitService);
    this.languageToolConfig = languageToolConfig;
  }

  @Override
  protected List<Match> requestTool(String text) {
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

    return TextCheckResponseTransformer.transformToListOfDomainMatches(response.getBody());
  }
}
