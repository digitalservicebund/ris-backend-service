package de.bund.digitalservice.ris.caselaw.domain;

public enum TypeOfIncome {
  LAND_UND_FORTWIRTSCHAFT("Land- und Forstwirtschaft"),
  GEWERBEBETRIEB("Gewerbebetrieb"),
  SELBSTSTAENDIGE_ARBEIT("Selbständige Arbeit"),
  NICHTSELBSTSTAENDIGE_ARBEIT("Nichtselbständige Arbeit"),
  KAPITALVERMOEGEN("Kapitalvermögen"),
  VERMIETUNG_UND_VERPACHTUNG("Vermietung und Verpachtung"),
  SONSTIGE_EINKUENFTE("Sonstige Einkünfte"),
  ESTG("EStG"),
  GEWSTG("GewStG"),
  USTG("UStG");

  public final String humanReadable;

  TypeOfIncome(String humanReadable) {
    this.humanReadable = humanReadable;
  }
}
