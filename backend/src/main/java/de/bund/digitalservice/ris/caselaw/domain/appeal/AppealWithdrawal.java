package de.bund.digitalservice.ris.caselaw.domain.appeal;

public enum AppealWithdrawal {
  JA("Ja"),
  NEIN("Nein"),
  KEINE_ANGABE("Keine Angabe");

  public final String humanReadable;

  AppealWithdrawal(String humanReadable) {
    this.humanReadable = humanReadable;
  }
}
