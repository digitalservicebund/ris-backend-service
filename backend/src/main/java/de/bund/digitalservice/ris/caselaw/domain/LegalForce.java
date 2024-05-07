package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalForceType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.Region;
import java.util.UUID;

public record LegalForce(UUID id, LegalForceType type, Region region) {}
