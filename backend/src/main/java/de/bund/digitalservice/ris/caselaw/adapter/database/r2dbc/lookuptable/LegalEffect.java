package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

public enum LegalEffect {
  YES("Ja"),
  NO("Nein"),
  NOT_SPECIFIED("Keine Angabe");

  private final String label;

  LegalEffect(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
