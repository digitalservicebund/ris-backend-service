package de.bund.digitalservice.ris.caselaw.domain;

public enum RelatedDocumentationType {
  ACTIVE_CITATION(Values.ACTIVE_CITATION, "Aktivzitierung"),
  PASSIVE_CITATION(Values.PASSIVE_CITATION, "Passivzitierung"),
  ENSUING_DECISION(Values.ENSUING_DECISION, "nachgehende Entscheidung"),
  PENDING_DECISION(Values.PENDING_DECISION, "anhängige Entscheidung"),
  PREVIOUS_DECISION(Values.PREVIOUS_DECISION, "vorgehende Entscheidung");

  private final String databaseValue;
  private final String name;

  RelatedDocumentationType(String databaseValue, String name) {
    this.databaseValue = databaseValue;
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public String getDatabaseValue() {
    return databaseValue;
  }

  @Override
  public String toString() {
    return databaseValue;
  }

  public static class Values {
    public static final String ACTIVE_CITATION = "caselaw_active_citation";
    public static final String PASSIVE_CITATION = "caselaw_passive_citation";
    public static final String ENSUING_DECISION = "ensuing_decision";
    public static final String PENDING_DECISION = "pending_decision";
    public static final String PREVIOUS_DECISION = "previous_decision";

    private Values() {}
  }
}
