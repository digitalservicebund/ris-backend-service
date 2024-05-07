package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalForceType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.Region;
import java.util.UUID;
import lombok.Builder;

@Builder
public record LegalForce(UUID id, LegalForceType type, Region region) {}
