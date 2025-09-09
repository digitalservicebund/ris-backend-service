package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.TextCheckResponseTransformer;
import de.bund.digitalservice.ris.caselaw.domain.TextCheckService;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.CategoryType;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.Match;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.TextCheckAllResponse;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.TextCheckCategoryResponse;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckWord;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckWordRequest;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.query.Param;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/caselaw")
@Slf4j
public class TextCheckController {

  private final TextCheckService textCheckService;
  private final UserService userService;

  public TextCheckController(TextCheckService textCheckService, UserService userService) {
    this.textCheckService = textCheckService;
    this.userService = userService;
  }

  @GetMapping("documentunits/{id}/text-check/all")
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

  @GetMapping("documentunits/{id}/text-check")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<TextCheckCategoryResponse> checkCategory(
      @PathVariable("id") UUID id, @Param("category") String category) {
    try {
      return ResponseEntity.ok(textCheckService.checkCategory(id, CategoryType.forName(category)));
    } catch (DocumentationUnitNotExistsException ex) {
      return ResponseEntity.internalServerError().build();
    }
  }

  @PostMapping(
      value = "documentunits/{id}/text-check/ignored-word",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<IgnoredTextCheckWord> addIgnoredWord(
      @PathVariable("id") UUID id, @RequestBody IgnoredTextCheckWordRequest request) {
    try {
      return ResponseEntity.ok(textCheckService.addIgnoreWord(id, request.word()));

    } catch (Exception e) {
      log.error("Adding word failed", e);
    }

    return ResponseEntity.internalServerError().build();
  }

  @DeleteMapping(
      value = "documentunits/{id}/text-check/ignored-word/{word}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  @Transactional
  public ResponseEntity<Void> removeIgnoredWord(
      @PathVariable("id") UUID id, @PathVariable("word") String word) {
    try {
      textCheckService.removeIgnoredWord(id, word);
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      log.error("Removing word failed", e);
    }

    return ResponseEntity.internalServerError().build();
  }

  @PostMapping(
      value = "text-check/ignored-word",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<IgnoredTextCheckWord> addIgnoredWordGlobally(
      @RequestBody IgnoredTextCheckWordRequest request,
      @AuthenticationPrincipal OidcUser oidcUser) {

    var documentationOffice = userService.getDocumentationOffice(oidcUser);
    if (documentationOffice.isEmpty()) {
      return ResponseEntity.badRequest().build();
    }
    try {
      return ResponseEntity.ok(
          textCheckService.addIgnoreWord(request.word(), documentationOffice.get()));

    } catch (Exception e) {
      log.error("Adding word failed", e);
    }

    return ResponseEntity.internalServerError().build();
  }

  @DeleteMapping(
      value = "text-check/ignored-word/{word}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  @Transactional
  public ResponseEntity<Void> removeIgnoredWordGlobally(@PathVariable("word") String word) {
    try {
      var success = textCheckService.removeIgnoredWord(word);
      if (success) {
        return ResponseEntity.ok().build();
      }
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      log.error("Removing word failed", e);
    }

    return ResponseEntity.internalServerError().build();
  }
}
