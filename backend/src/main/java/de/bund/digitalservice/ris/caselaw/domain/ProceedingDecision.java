package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.domain.validator.DateKnownConstraint;
import jakarta.validation.constraints.PastOrPresent;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
@DateKnownConstraint
public record ProceedingDecision(
    UUID uuid,
    String documentNumber,
    DataSource dataSource,
    Court court,
    @PastOrPresent Instant date,
    String fileNumber,
    DocumentType documentType,
    boolean dateKnown) {}
