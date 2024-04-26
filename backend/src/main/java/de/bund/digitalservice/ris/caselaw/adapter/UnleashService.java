package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.FeatureToggleService;
import io.getunleash.Unleash;
import java.util.List;

public class UnleashService implements FeatureToggleService {
  private final Unleash unleash;
  private final String environment;

  public UnleashService(Unleash unleash, String environment) {
    this.unleash = unleash;
    this.environment = environment;
  }

  @Override
  public List<String> getFeatureToggleNames() {
    return unleash.more().getFeatureToggleNames();
  }

  @Override
  public boolean isEnabled(String toggleName) {
    return unleash.isEnabled(toggleName + "." + environment);
  }
}
