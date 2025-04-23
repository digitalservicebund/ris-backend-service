package de.bund.digitalservice.ris.caselaw.domain;

public enum PublicationStatus {
  EXTERNAL_HANDOVER_PENDING("Fremdanlage"),
  UNPUBLISHED("Unveröffentlicht"),
  PUBLISHED("Veröffentlicht"),
  PUBLISHING("In Veröffentlichung"),
  DELETING("Löschen"),
  LOCKED("Gesperrt"),
  DUPLICATED("Dublette"),
  UNKNOWN("Unbekannt");

  public final String deTranslation;

  PublicationStatus(String deTranslation) {
    this.deTranslation = deTranslation;
  }
}
