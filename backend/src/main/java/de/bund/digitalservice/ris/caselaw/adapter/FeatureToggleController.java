package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.FeatureToggleService;
import java.util.concurrent.TimeUnit;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/feature-toggles")
public class FeatureToggleController {
  private final FeatureToggleService service;

  public FeatureToggleController(FeatureToggleService service) {
    this.service = service;
  }

  @GetMapping("/{toggleName}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Boolean> isEnabled(@PathVariable String toggleName) {
    return ResponseEntity.ok()
        .cacheControl(
            CacheControl.maxAge(1, TimeUnit.MINUTES).staleWhileRevalidate(1, TimeUnit.DAYS))
        .body(service.isEnabled(toggleName));
  }
}
