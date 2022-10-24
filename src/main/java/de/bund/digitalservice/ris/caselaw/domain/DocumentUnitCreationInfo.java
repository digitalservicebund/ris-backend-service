package de.bund.digitalservice.ris.caselaw.domain;

import lombok.Data;

@Data
public class DocumentUnitCreationInfo {
  public static final DocumentUnitCreationInfo EMPTY = new DocumentUnitCreationInfo();

  String documentationCenterAbbreviation;
  String documentType;
}
