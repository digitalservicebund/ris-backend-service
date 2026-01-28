package de.bund.digitalservice.ris.caselaw.domain;

public enum ProceedingType {
  VERFASSUNGSBESCHWERDE,
  ORGANSTREITVERFAHREN,
  KONKRETE_NORMENKONTROLLE,
  ABSTRAKTE_NORMENKONTROLLE,
  BUND_LAENDER_STREITIGKEIT,
  WAHLPRUEFUNGSVERFAHREN,
  EINSTWEILIGER_RECHTSSCHUTZ,
  SONSTIGE_VERFAHREN;

  @Override
  public String toString() {
    return switch (this) {
      case VERFASSUNGSBESCHWERDE -> "Verfassungsbeschwerde";
      case ORGANSTREITVERFAHREN -> "Organstreitverfahren";
      case KONKRETE_NORMENKONTROLLE -> "Konkrete Normenkontrolle";
      case ABSTRAKTE_NORMENKONTROLLE -> "Abstrakte Normenkontrolle";
      case BUND_LAENDER_STREITIGKEIT -> "Bund-Länder-Streitigkeit";
      case WAHLPRUEFUNGSVERFAHREN -> "Wahlprüfungsverfahren";
      case EINSTWEILIGER_RECHTSSCHUTZ -> "Einstweiliger Rechtsschutz";
      case SONSTIGE_VERFAHREN -> "Sonstige Verfahren";
    };
  }
}
