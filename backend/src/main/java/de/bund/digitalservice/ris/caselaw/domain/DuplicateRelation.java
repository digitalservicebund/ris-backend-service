package de.bund.digitalservice.ris.caselaw.domain;

import lombok.Builder;

@Builder
public record DuplicateRelation(String documentNumber, DuplicateRelationStatus status) {}
