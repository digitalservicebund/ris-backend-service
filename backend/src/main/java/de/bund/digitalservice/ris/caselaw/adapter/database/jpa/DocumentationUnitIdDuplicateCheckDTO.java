package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import java.util.UUID;

/**
 * An interface representing a documentation unit with reduced information about its duplicate
 * status
 */
public interface DocumentationUnitIdDuplicateCheckDTO {
  UUID getId();

  Boolean getIsJdvDuplicateCheckActive();

  PublicationStatus getStatus();
}
