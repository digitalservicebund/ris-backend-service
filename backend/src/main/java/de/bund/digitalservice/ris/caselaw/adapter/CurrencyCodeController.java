package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.CurrencyCode;
import de.bund.digitalservice.ris.caselaw.domain.CurrencyCodeService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/caselaw/currencycodes")
@Slf4j
public class CurrencyCodeController {
  private final CurrencyCodeService service;

  public CurrencyCodeController(CurrencyCodeService service) {
    this.service = service;
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  public List<CurrencyCode> getDocumentTypes(
      @RequestParam(value = "q", required = false) String searchStr,
      @RequestParam(value = "sz", required = false, defaultValue = "200") Integer size) {
    return service.getCurrencyCodes(searchStr, size);
  }
}
