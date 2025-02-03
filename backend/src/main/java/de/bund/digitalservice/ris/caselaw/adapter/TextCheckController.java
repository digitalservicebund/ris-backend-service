package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.TextCheckResponseTransformer;
import de.bund.digitalservice.ris.caselaw.domain.TextCheckService;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.Match;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.TextCheckAllResponse;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.TextCheckResponse;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/caselaw/documentunits")
@Slf4j
public class TextCheckController {

  private final TextCheckService textCheckService;

  public TextCheckController(TextCheckService textCheckService) {
    this.textCheckService = textCheckService;
  }

  @PostMapping(
      value = "/text-check",
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<TextCheckResponse> check(
      @AuthenticationPrincipal OidcUser oidcUser, @RequestBody String text) {
    try {
      return ResponseEntity.ok(textCheckService.checkAsResponse(text));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @GetMapping("{id}/text-check/all")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<TextCheckAllResponse> checkWholeDocumentationUnit(
      @PathVariable("id") UUID id) {
    List<Match> allMatches;

    try {
      allMatches = textCheckService.checkWholeDocumentationUnit(id);
    } catch (DocumentationUnitNotExistsException ex) {
      return ResponseEntity.internalServerError().build();
    }

    return ResponseEntity.ok(TextCheckResponseTransformer.transformToAllDomain(allMatches));
  }
}
