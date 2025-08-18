package de.bund.digitalservice.ris.caselaw.domain;

public enum PortalPublicationStatus {
  UNPUBLISHED("Unveröffentlicht"),
  PUBLISHED("Veröffentlicht"),
  WITHDRAWN("Zurückgezogen");

  public final String humanReadable;

  PortalPublicationStatus(String humanReadable) {
    this.humanReadable = humanReadable;
  }
}
