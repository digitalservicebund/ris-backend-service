package de.bund.digitalservice.ris.caselaw.domain.lookuptable.court;

import lombok.Builder;

@Builder
public record Court(String type, String location, String label, String revoked) {

  public static String generateLabel(String courtType, String courtLocation) {
    String label = "";

    if (courtType != null) {
      label += courtType;

      if (courtLocation != null) {
        label += " " + courtLocation;
      }
    }

    return label;
  }
}
