package de.bund.digitalservice.ris.caselaw.adapter.languagetool;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.TextCheckResponseTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.TextCheckService;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.Match;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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

    // Prepare the form data
    MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
    formData.add("data", text);
    formData.add("language", languageToolConfig.getLanguage());
    formData.add("mode", "all");
    formData.add("disabledRules", "WHITESPACE_RULE");

    // Set headers (optional but good practice)
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

    // Create the HTTP request
    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

    ResponseEntity<LanguageToolResponse> response =
        restTemplate.postForEntity(
            languageToolConfig.getUrl(), request, LanguageToolResponse.class);

    return TextCheckResponseTransformer.transformToListOfDomainMatches(
        Objects.requireNonNull(response.getBody()));
  }
}
