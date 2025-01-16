package de.bund.digitalservice.ris.caselaw.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/caselaw/languagetool")
@Slf4j
public class LanguageToolController {

  private final LanguageToolService languageToolService;

  public LanguageToolController(LanguageToolService languageToolService) {
    this.languageToolService = languageToolService;
  }

  @PostMapping(
      value = "/check",
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  // TODO: PreAutorize : blocked by a test
  public ResponseEntity<JsonNode> check(
      @AuthenticationPrincipal OidcUser oidcUser, @RequestBody String text) {
    try {
      return ResponseEntity.ok(languageToolService.check(text));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
