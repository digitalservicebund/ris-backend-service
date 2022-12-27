package de.bund.digitalservice.ris.caselaw.domain;

public record DocumentUnitCreationInfo(
    String documentationCenterAbbreviation, String documentType) {
  public static final DocumentUnitCreationInfo EMPTY = new DocumentUnitCreationInfo(null, null);
}
