package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.FeatureToggleService;
import io.getunleash.Unleash;
import java.util.List;
import reactor.core.publisher.Mono;

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
  public Mono<Boolean> isEnabled(String toggleName) {
    boolean enabled = unleash.isEnabled(toggleName + "." + environment);
    return Mono.just(enabled);
  }
}
