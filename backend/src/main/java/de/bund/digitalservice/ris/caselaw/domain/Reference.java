package de.bund.digitalservice.ris.caselaw.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;
import lombok.Builder;

/**
 * A reference to a legal periodical (Fundstelle).
 *
 * @param rank The rank of the reference.
 * @param primaryReference Whether the reference is a primary reference.
 * @param citation The citation.
 * @param referenceSupplement The reference supplement (Klammerzusatz).
 * @param footnote The footnote.
 * @param legalPeriodicalId The id of the legal periodical.
 * @param legalPeriodicalAbbreviation The abbreviation of the legal periodical.
 * @param legalPeriodicalTitle The title of the legal periodical.
 * @param legalPeriodicalSubtitle The subtitle of the legal periodical.
 */
@Builder(toBuilder = true)
// to ignore the validationErrors that the frontend might be sending along
@JsonIgnoreProperties(ignoreUnknown = true)
public record Reference(
    Boolean primaryReference,
    String citation,
    String referenceSupplement, // Klammerzusatz
    String footnote,
    UUID legalPeriodicalId,
    String legalPeriodicalAbbreviation,
    String legalPeriodicalTitle,
    String legalPeriodicalSubtitle) {}
