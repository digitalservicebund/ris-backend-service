package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;

public interface FeatureToggleService {
  List<String> getFeatureToggleNames();

  Boolean isEnabled(String toggleName);
}
