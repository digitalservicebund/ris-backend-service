package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;
import lombok.Builder;

/**
 * LegalPeriodical represents a legal periodical.
 *
 * @param id the unique identifier of the legal periodical
 * @param abbreviation the abbreviation of the legal periodical
 * @param title the title of the legal periodical
 * @param subtitle the subtitle of the legal periodical
 * @param primaryReference whether the legal periodical is a primary reference
 * @param citationStyle the citation style of the legal periodical
 */
@Builder
public record LegalPeriodical(
    UUID id,
    String abbreviation,
    String title,
    String subtitle,
    Boolean primaryReference,
    String citationStyle) {}
