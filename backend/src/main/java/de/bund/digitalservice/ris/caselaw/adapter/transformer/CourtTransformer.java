package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CourtDTO;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CourtTransformer {
  private static final Pattern DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

  private CourtTransformer() {}

  public static Court transformDTO(CourtDTO courtDTO) {
    String revoked = extractRevoked(courtDTO.getAdditional());

    if (courtDTO.getSuperiorcourt() != null
        && courtDTO.getForeigncountry() != null
        && courtDTO.getSuperiorcourt().equalsIgnoreCase("ja")
        && courtDTO.getForeigncountry().equalsIgnoreCase("nein")) {

      return new Court(courtDTO.getCourttype(), null, courtDTO.getCourttype(), revoked);
    }

    return new Court(
        courtDTO.getCourttype(),
        courtDTO.getCourtlocation(),
        courtDTO.getCourttype() + " " + courtDTO.getCourtlocation(),
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
