package de.bund.digitalservice.ris.caselaw.adapter.eurlex;

import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.SearchResult;
import de.bund.digitalservice.ris.caselaw.domain.SearchService;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/caselaw/eurlex")
public class EurLexController {
  private final SearchService service;
  private final UserService userService;

  public EurLexController(SearchService service, UserService userService) {
    this.service = service;
    this.userService = userService;
  }

  @GetMapping
  @PreAuthorize("@userIsInternal.apply(#oidcUser)")
  public ResponseEntity<Page<SearchResult>> getSearchResults(
      @AuthenticationPrincipal OidcUser oidcUser,
      @RequestParam(value = "page", required = false) String page,
      @RequestParam(value = "file-number") Optional<String> fileNumber,
      @RequestParam(value = "celex") Optional<String> celex,
      @RequestParam(value = "court") Optional<String> court,
      @RequestParam(value = "start-date") Optional<LocalDate> startDate,
      @RequestParam(value = "end-date") Optional<LocalDate> endDate) {

    DocumentationOffice documentationOffice = userService.getUser(oidcUser).documentationOffice();

    return ResponseEntity.ok(
        service.getSearchResults(page, documentationOffice, fileNumber, celex, court, startDate, endDate));
  }
}
