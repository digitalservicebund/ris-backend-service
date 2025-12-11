package de.bund.digitalservice.ris.caselaw.domain.appeal;

public enum PkhPlaintiff {
  JA("Ja"),
  NEIN("Nein"),
  KEINE_ANGABE("Keine Angabe");

  public final String humanReadable;

  PkhPlaintiff(String humanReadable) {
    this.humanReadable = humanReadable;
  }
}
