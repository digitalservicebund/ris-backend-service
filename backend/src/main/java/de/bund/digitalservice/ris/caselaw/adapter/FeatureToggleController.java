package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.FeatureToggleService;
import java.util.List;
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

  public List<String> getFeatureToggleNames() {
    return service.getFeatureToggleNames();
  }

  @GetMapping("/{toggleName}")
  public Boolean isEnabled(@PathVariable String toggleName) {
    return service.isEnabled(toggleName);
  }
}
