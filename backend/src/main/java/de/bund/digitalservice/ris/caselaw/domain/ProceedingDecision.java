package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ProceedingDecision(
    UUID uuid,
    String documentNumber,
    DataSource dataSource,
    Court court,
    Instant date,
    String fileNumber,
    DocumentType documentType,
    boolean dateKnown) {}
