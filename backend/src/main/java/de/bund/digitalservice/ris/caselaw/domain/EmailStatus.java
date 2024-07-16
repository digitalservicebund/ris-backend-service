package de.bund.digitalservice.ris.caselaw.domain;

import lombok.Getter;

/** Enum representing the status of an email. */
@Getter
public enum EmailStatus {
  SUCCESS("erfolgreich angekommen"),
  ERROR("fehlgeschlagen"),
  UNKNOWN("unbekannt"),
  SENT("im Versand");

  private final String displayText;

  EmailStatus(String displayText) {
    this.displayText = displayText;
  }
}
