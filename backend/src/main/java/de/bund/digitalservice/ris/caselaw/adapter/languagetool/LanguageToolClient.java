package de.bund.digitalservice.ris.caselaw.adapter.languagetool;

import com.google.gson.JsonObject;
import java.util.Collections;
import java.util.Objects;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class LanguageToolClient {

  private final RestTemplate restTemplate;
  private final LanguageToolConfig languageToolConfig;

  public LanguageToolClient(RestTemplate restTemplate, LanguageToolConfig languageToolConfig) {
    this.restTemplate = restTemplate;
    this.languageToolConfig = languageToolConfig;
  }

  public LanguageToolResponse checkText(JsonObject annotations) {
    // Prepare the form data
    MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
    formData.add("data", annotations.toString());
    formData.add("language", languageToolConfig.getLanguage());
    formData.add("mode", "all");
    if (!languageToolConfig.getDisabledRules().isEmpty()) {
      formData.add("disabledRules", String.join(",", languageToolConfig.getDisabledRules()));
    }
    if (!languageToolConfig.getDisabledCategories().isEmpty()) {
      formData.add(
          "disabledCategories", String.join(",", languageToolConfig.getDisabledCategories()));
    }

    // Set headers
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

    // Create the HTTP request
    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

    // Make the API call and get the response
    ResponseEntity<LanguageToolResponse> response =
        restTemplate.postForEntity(
            languageToolConfig.getUrl(), request, LanguageToolResponse.class);

    return Objects.requireNonNull(response.getBody());
  }
}
