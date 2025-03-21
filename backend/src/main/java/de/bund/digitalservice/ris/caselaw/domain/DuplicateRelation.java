package de.bund.digitalservice.ris.caselaw.domain;

import java.time.LocalDate;
import lombok.Builder;

@Builder
public record DuplicateRelation(
    String documentNumber,
    DuplicateRelationStatus status,
    String courtLabel,
    String fileNumber,
    String documentType,
    PublicationStatus publicationStatus,
    LocalDate decisionDate,
    boolean isJdvDuplicateCheckActive) {}
