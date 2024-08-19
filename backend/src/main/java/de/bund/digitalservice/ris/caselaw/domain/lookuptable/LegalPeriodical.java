package de.bund.digitalservice.ris.caselaw.domain.lookuptable;

import java.util.UUID;
import lombok.Builder;

/**
 * A legal periodical (Fundstelle).
 *
 * @param legalPeriodicalId The id of the legal periodical (Periodikum).
 * @param legalPeriodicalAbbreviation The abbreviation of the legal periodical.
 * @param legalPeriodicalTitle The title of the legal periodical.
 * @param legalPeriodicalSubtitle The subtitle of the legal periodical.
 * @param primaryReference The category of reference ('amtlich' or 'nichtamtlich').
 * @param citationStyle An example of the style of citation for referencing.
 */
@Builder
public record LegalPeriodical(
    UUID legalPeriodicalId,
    String legalPeriodicalAbbreviation,
    String legalPeriodicalTitle,
    String legalPeriodicalSubtitle,
    Boolean primaryReference,
    String citationStyle) {}
