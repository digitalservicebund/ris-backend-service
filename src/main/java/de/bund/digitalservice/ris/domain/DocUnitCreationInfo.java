package de.bund.digitalservice.ris.domain;

import lombok.Data;

@Data
public class DocUnitCreationInfo {
  public static final DocUnitCreationInfo EMPTY = new DocUnitCreationInfo();

  String documentationCenterAbbreviation;
  String documentType;
}
