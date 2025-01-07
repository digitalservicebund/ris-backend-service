package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;
import lombok.Builder;

// TODO: Define data needed by frontend
@Builder
public record DuplicateRelation(UUID docUnitId1, UUID docUnitId2, DuplicateRelationStatus status) {}
