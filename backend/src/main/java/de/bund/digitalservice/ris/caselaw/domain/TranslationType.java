package de.bund.digitalservice.ris.caselaw.domain;

public enum TranslationType {
  AMTLICH,
  NICHT_AMTLICH,
  KEINE_ANGABE;

  @Override
  public String toString() {
    return switch (this) {
      case AMTLICH -> "Amtlich";
      case NICHT_AMTLICH -> "Nicht-amtlich";
      case KEINE_ANGABE -> "Keine Angabe";
    };
  }
}
