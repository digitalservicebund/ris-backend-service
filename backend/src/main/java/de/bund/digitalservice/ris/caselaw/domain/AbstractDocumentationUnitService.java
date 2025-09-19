package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.apache.coyote.BadRequestException;
import org.springframework.transaction.annotation.Transactional;

public abstract class AbstractDocumentationUnitService {
  protected AbstractDocumentationUnitService() {}

  @Transactional(rollbackFor = BadRequestException.class)
  public abstract void bulkAssignProcessStep(
      @NotNull List<UUID> documentationUnitIds,
      DocumentationUnitProcessStep documentationUnitProcessStep)
      throws DocumentationUnitNotExistsException, BadRequestException;
}
