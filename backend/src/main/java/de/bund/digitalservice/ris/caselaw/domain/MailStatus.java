package de.bund.digitalservice.ris.caselaw.domain;

import lombok.Getter;

/** Enum representing the status of an email. */
@Getter
public enum MailStatus {
  SUCCESS("erfolgreich angekommen"),
  ERROR("fehlgeschlagen"),
  UNKNOWN("unbekannt"),
  SENT("im Versand");

  private final String displayText;

  MailStatus(String displayText) {
    this.displayText = displayText;
  }
}
