package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;
import lombok.Builder;

/**
 * A (reduced) record representing the duplicate of another documentation unit
 *
 * @param id id of the documentation unit (duplicate)
 * @param isJdvDuplicateCheckActive This field represents the "Dupcode ausschalten" functionality
 *     from the jDV. It is set to false in the migration if the duplication check should be ignored.
 *     After the jDV has been deactivated, this field will not be needed anymore.
 */
@Builder
public record DocumentationUnitIdDuplicateCheck(UUID id, Boolean isJdvDuplicateCheckActive) {}
