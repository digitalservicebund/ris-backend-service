package de.bund.digitalservice.ris.caselaw.domain;

import lombok.Getter;

@Getter
public enum PublishState {
  SUCCESS("erfolgreich angekommen"),
  ERROR("fehlgeschlagen"),
  UNKNOWN("unbekannt"),
  SENT("im Versand");

  private final String displayText;

  PublishState(String displayText) {
    this.displayText = displayText;
  }
}
