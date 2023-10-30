package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CourtDTO;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CourtTransformer {
  private static final Pattern DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

  private CourtTransformer() {}

  public static Court transformToDomain(CourtDTO courtDTO) {
    if (courtDTO == null) return null;

    String revoked = extractRevoked(courtDTO.getAdditionalInformation());

    if (courtDTO.isSuperiorCourt() && !courtDTO.isForeignCourt()) {

      return new Court(courtDTO.getId(), courtDTO.getType(), null, courtDTO.getType(), revoked);
    }

    return new Court(
        courtDTO.getId(),
        courtDTO.getType(),
        courtDTO.getLocation(),
        courtDTO.getType() + " " + courtDTO.getLocation(),
        revoked);
  }

  private static String extractRevoked(String additional) {
    if (additional == null || additional.isBlank()) {
      return null;
    }

    additional = additional.toLowerCase();

    if (additional.contains("aufgehoben")) {
      String revoked = "aufgehoben";
      Matcher matcher = DATE_PATTERN.matcher(additional);

      if (matcher.find()) {
        revoked += " seit: " + matcher.group().substring(0, 4);
      }

      return revoked;
    }

    // detect more patterns?
    return null;
  }
}
