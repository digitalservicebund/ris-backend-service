package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;
import lombok.Builder;

/**
 * A reference to a legal periodical (Fundstelle).
 *
 * @param id The id of the reference.
 * @param primaryReference Whether the reference is a primary reference (otherwise secondary).
 * @param citation The citation (Zitierung).
 * @param referenceSupplement The reference supplement (Klammerzusatz).
 * @param footnote The footnote (used by BFH).
 * @param legalPeriodicalId The id of the legal periodical (Periodikum).
 * @param legalPeriodicalAbbreviation The abbreviation of the legal periodical.
 * @param legalPeriodicalTitle The title of the legal periodical.
 * @param legalPeriodicalSubtitle The subtitle of the legal periodical.
 */
@Builder(toBuilder = true)
public record Reference(
    UUID id,
    Boolean primaryReference,
    String citation,
    String referenceSupplement,
    String footnote,
    UUID legalPeriodicalId,
    String legalPeriodicalAbbreviation,
    String legalPeriodicalTitle,
    String legalPeriodicalSubtitle) {}
