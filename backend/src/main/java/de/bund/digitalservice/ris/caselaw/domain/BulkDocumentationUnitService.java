package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.apache.coyote.BadRequestException;
import org.springframework.transaction.annotation.Transactional;

/**
 * This interface serves as the new domain service contract, designed for a phased refactoring.
 * Methods will be moved here from the legacy DocumentationUnitService, with concrete
 * implementations residing in a new service class (e.g., DatabaseDocumentationUnitService).
 *
 * <p>This approach allows for the introduction of new architectural patterns without a large, risky
 * refactor of the existing codebase. The ultimate goal is to consolidate all logic into this
 * service and its concrete implementation, and eventually deprecate the legacy service.
 */
public interface BulkDocumentationUnitService {

  @Transactional(rollbackFor = BadRequestException.class)
  void bulkAssignProcessStep(
      @NotNull List<UUID> documentationUnitIds,
      DocumentationUnitProcessStep documentationUnitProcessStep,
      User user)
      throws DocumentationUnitNotExistsException, BadRequestException;
}
