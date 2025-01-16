package de.bund.digitalservice.ris.caselaw.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.bund.digitalservice.ris.caselaw.config.LanguageToolConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("api/v1/caselaw/languagetool")
@Slf4j
public class LanguageToolController {

  @Autowired LanguageToolConfig languageToolConfig;

  @PostMapping(
      value = "/check",
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> check(
      @AuthenticationPrincipal OidcUser oidcUser, @RequestBody String text) {

    RestTemplate restTemplate = new RestTemplate();

    HttpHeaders headers = new HttpHeaders();

    headers.set("Accept", "application/json");
    headers.set("Content-Type", "application/x-www-form-urlencoded");

    String body =
        "text=" + text + "&language=" + languageToolConfig.getLanguage() + "&enabledOnly=false";

    HttpEntity<String> entity = new HttpEntity<>(body, headers);

    try {
      ResponseEntity<String> response =
          restTemplate.exchange(languageToolConfig.getUrl(), HttpMethod.POST, entity, String.class);

      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode jsonNode = objectMapper.readTree(response.getBody());

      return ResponseEntity.ok(jsonNode);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
