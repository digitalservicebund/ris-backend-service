package de.bund.digitalservice.ris.caselaw.domain.docx;

public enum DocXPropertyField {
  FILE_NUMBER("Aktenzeichen"),
  COURT_TYPE("Gerichtstyp"),
  LEGAL_EFFECT("Rechtskraft"),
  APPRAISAL_BODY("Spruchkoerper");

  private final String key;

  private DocXPropertyField(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }

  public static DocXPropertyField fromKey(String key) {
    for (DocXPropertyField field : values()) {
      if (field.getKey().equals(key)) {
        return field;
      }
    }
    return null;
  }
}
