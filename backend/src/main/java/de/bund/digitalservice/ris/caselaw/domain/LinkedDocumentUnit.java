package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.time.Instant;
import lombok.Builder;

@Builder
public record LinkedDocumentUnit(
    Long id, Court court, Instant date, String fileNumber, DocumentType documentType) {}
