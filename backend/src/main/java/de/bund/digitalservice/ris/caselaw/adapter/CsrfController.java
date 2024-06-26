package de.bund.digitalservice.ris.caselaw.adapter;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CsrfController {
  @GetMapping("/csrf")
  @PreAuthorize("permitAll")
  public CsrfToken csrf(CsrfToken csrfToken) {
    return csrfToken;
  }
}
