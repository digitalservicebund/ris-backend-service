package de.bund.digitalservice.ris.caselaw.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ProcessStepName {
  NEU,
  DOKUMENTATIONSWUERDIGKEIT,
  ERSTERFASSUNG,
  FACHDOKUMENTATION,
  QS_FORMAL,
  QS_FACHLICH,
  BLOCKIERT,
  TERMINIERT,
  ABGABE,
  WIEDERVORLAGE,
  FERTIG;

  @JsonCreator
  public static ProcessStepName fromValue(String value) {
    for (ProcessStepName name : ProcessStepName.values()) {
      if (name.name()
          .equalsIgnoreCase(value)) { // .name() gets the enum constant name (e.g., "NEU")
        return name;
      }
    }
    return null;
  }
}
