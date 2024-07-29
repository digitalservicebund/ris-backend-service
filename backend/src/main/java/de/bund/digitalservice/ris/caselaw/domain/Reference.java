package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalPeriodical;
import java.util.UUID;
import lombok.Builder;

/**
 * A reference to a legal periodical (Fundstelle).
 *
 * @param id The id of the reference.
 * @param legalPeriodical The periodical, where reference was found.
 * @param citation The citation (Zitierung).
 * @param referenceSupplement The reference supplement (Klammerzusatz).
 * @param footnote The footnote (used by BFH).
 */
@Builder(toBuilder = true)
public record Reference(
    UUID id,
    String citation,
    String referenceSupplement,
    String footnote,
    LegalPeriodical legalPeriodical) {}
