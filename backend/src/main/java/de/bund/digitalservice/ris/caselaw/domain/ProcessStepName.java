package de.bund.digitalservice.ris.caselaw.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ProcessStepName {
  NEU("Neu"),
  DOKUMENTATIONSWUERDIGKEIT("Dokumentationswürdigkeit"),
  ERSTERFASSUNG("Ersterfassung"),
  FACHDOKUMENTATION("Fachdokumentation"),
  QS_FORMAL("QS formal"),
  QS_FACHLICH("QS fachlich"),
  BLOCKIERT("Blockiert"),
  TERMINIERT("Terminiert"),
  ABGABE("Abgabe"),
  WIEDERVORLAGE("Wiedervorlage"),
  FERTIG("Fertig");

  private final String displayName;

  ProcessStepName(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  @JsonCreator
  public static ProcessStepName fromValue(String value) {
    for (ProcessStepName name : ProcessStepName.values()) {
      if (name.displayName.equalsIgnoreCase(value)) {
        return name;
      }
    }
    throw new IllegalArgumentException("Unknown ProcessStepName value: " + value);
  }
}
