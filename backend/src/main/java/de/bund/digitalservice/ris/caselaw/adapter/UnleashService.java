package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.FeatureToggleService;
import io.getunleash.Unleash;
import java.util.List;

public class UnleashService implements FeatureToggleService {
  private final Unleash unleash;

  public UnleashService(Unleash unleash) {
    this.unleash = unleash;
  }

  @Override
  public List<String> getFeatureToggleNames() {
    return unleash.more().getFeatureToggleNames();
  }

  @Override
  public Boolean isEnabled(String toggleName) {
    return unleash.isEnabled(toggleName);
  }
}
