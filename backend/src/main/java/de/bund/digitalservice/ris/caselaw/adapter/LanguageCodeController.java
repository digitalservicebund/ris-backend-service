package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.LanguageCode;
import de.bund.digitalservice.ris.caselaw.domain.LanguageCodeService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/caselaw/languagecodes")
@Slf4j
public class LanguageCodeController {
  private final LanguageCodeService service;

  public LanguageCodeController(LanguageCodeService service) {
    this.service = service;
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  public List<LanguageCode> getDocumentTypes(
      @RequestParam(value = "q", required = false) String searchStr,
      @RequestParam(value = "sz", required = false, defaultValue = "200") Integer size) {
    return service.getLanguageCodes(searchStr, size);
  }
}
