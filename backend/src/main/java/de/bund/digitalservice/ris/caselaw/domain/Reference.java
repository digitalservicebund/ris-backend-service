package de.bund.digitalservice.ris.caselaw.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

/**
 * A reference to a legal periodical (Fundstelle).
 *
 * @param rank The rank of the reference.
 * @param referenceSupplement The reference supplement (Klammerzusatz).
 * @param legalPeriodical The legal periodical.
 * @param citation The citation.
 * @param footnote The footnote.
 */
@Builder(toBuilder = true)
// to ignore the validationErrors that the frontend might be sending along
@JsonIgnoreProperties(ignoreUnknown = true)
public record Reference(
    Integer rank,
    // Klammerzusatz
    String referenceSupplement,
    LegalPeriodical legalPeriodical,
    String citation,
    String footnote) {}
