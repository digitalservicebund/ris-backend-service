package de.bund.digitalservice.ris.caselaw.domain;

public enum Addressee {
  BEVOLLMAECHTIGTER,
  BESCHWERDEFUEHRER_ANTRAGSTELLER;

  @Override
  public String toString() {
    return switch (this) {
      case BEVOLLMAECHTIGTER -> "Bevollmächtigter";
      case BESCHWERDEFUEHRER_ANTRAGSTELLER -> "Beschwerdeführer / Antragsteller";
    };
  }
}
