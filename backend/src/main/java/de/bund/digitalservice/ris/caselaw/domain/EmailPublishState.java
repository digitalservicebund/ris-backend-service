package de.bund.digitalservice.ris.caselaw.domain;

import lombok.Getter;

@Getter
public enum EmailPublishState {
  SUCCESS("erfolgreich angekommen"),
  ERROR("fehlgeschlagen"),
  UNKNOWN("unbekannt"),
  SENT("im Versand");

  private final String displayText;

  EmailPublishState(String displayText) {
    this.displayText = displayText;
  }
}
