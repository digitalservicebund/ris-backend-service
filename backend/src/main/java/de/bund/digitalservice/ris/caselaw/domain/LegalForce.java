package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalForceType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.Region;
import java.util.UUID;
import lombok.Builder;

/**
 * A record representing the legal force (Gesetzeskraft) of a norm reference, linked to a
 * documentation unit.
 *
 * @param id id of the legal force
 * @param type Type (Gesetzestyp) of the legal force
 * @param region Region (Geltungsbereich) of the legal force
 */
@Builder
public record LegalForce(UUID id, LegalForceType type, Region region) {}
