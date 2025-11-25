package de.bund.digitalservice.ris.caselaw.domain;

public enum TranslationType {
  AMTLICH,
  NICHT_AMTLICH,
  KEINE_ANGABE;

  @Override
  public String toString() {
    return switch (this) {
      case AMTLICH -> "amtlich";
      case NICHT_AMTLICH -> "nicht-amtlich";
      case KEINE_ANGABE -> "keine Angabe";
    };
  }
}
