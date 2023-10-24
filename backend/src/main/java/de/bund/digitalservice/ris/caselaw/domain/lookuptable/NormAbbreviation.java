package de.bund.digitalservice.ris.caselaw.domain.lookuptable;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record NormAbbreviation(
    UUID id,
    String abbreviation,
    Instant decisionDate,
    Long documentId,
    String documentNumber,
    String officialLetterAbbreviation,
    String officialLongTitle,
    String officialShortTitle,
    String source,
    List<DocumentType> documentTypes,
    Region region) {}
