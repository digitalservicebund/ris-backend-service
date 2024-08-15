package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalPeriodical;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

/** A legal periodical edition (Ausgabe eines Periodikums für die Periodikaauswertung). */
@Builder
public record LegalPeriodicalEdition(
    UUID id,
    LegalPeriodical legalPeriodical,
    String prefix,
    String suffix,
    String name,
    List<Reference> references) {}
