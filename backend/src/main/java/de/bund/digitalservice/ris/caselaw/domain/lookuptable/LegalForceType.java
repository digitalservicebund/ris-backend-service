package de.bund.digitalservice.ris.caselaw.domain.lookuptable;

import java.util.UUID;
import lombok.Builder;

@Builder
public record LegalForceType(UUID id, String abbreviation, String label) {}
