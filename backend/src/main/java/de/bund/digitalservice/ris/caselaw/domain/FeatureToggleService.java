package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import reactor.core.publisher.Mono;

public interface FeatureToggleService {
  List<String> getFeatureToggleNames();

  Mono<Boolean> isEnabled(String toggleName);
}
