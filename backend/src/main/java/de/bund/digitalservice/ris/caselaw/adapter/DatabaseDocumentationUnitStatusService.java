package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitHistoryLogService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitStatusService;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLogEventType;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import java.time.Instant;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service to handle the status of a document unit in the database. */
@Service
@Slf4j
public class DatabaseDocumentationUnitStatusService implements DocumentationUnitStatusService {

  private final DatabaseDocumentationUnitRepository databaseDocumentationUnitRepository;
  private final DocumentationUnitHistoryLogService historyService;

  public DatabaseDocumentationUnitStatusService(
      DatabaseDocumentationUnitRepository databaseDocumentationUnitRepository,
      DocumentationUnitHistoryLogService historyService) {
    this.databaseDocumentationUnitRepository = databaseDocumentationUnitRepository;
    this.historyService = historyService;
  }

  /**
   * Update the status of a document unit.
   *
   * @param documentNumber the document number of the documentation unit
   * @param status the new status
   * @param user currently logged-in user
   * @throws DocumentationUnitNotExistsException if the documentation unit does not exist
   */
  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public void update(String documentNumber, Status status, User user)
      throws DocumentationUnitNotExistsException {

    DocumentationUnitDTO docUnit =
        databaseDocumentationUnitRepository
            .findByDocumentNumber(documentNumber)
            .orElseThrow(() -> new DocumentationUnitNotExistsException(documentNumber));

    StatusDTO oldStatus = docUnit.getStatus();
    StatusDTO newStatus =
        StatusDTO.builder()
            .createdAt(Instant.now())
            .publicationStatus(status.publicationStatus())
            .withError(status.withError())
            .documentationUnit(docUnit)
            .build();
    docUnit.setStatus(newStatus);

    if (oldStatus != null
        && !Objects.equals(newStatus.getPublicationStatus(), oldStatus.getPublicationStatus())) {
      String description =
          "Status geändert: %s → %s"
              .formatted(
                  oldStatus.getPublicationStatus().deTranslation,
                  newStatus.getPublicationStatus().deTranslation);
      historyService.saveHistoryLog(docUnit.getId(), user, HistoryLogEventType.STATUS, description);
    }

    databaseDocumentationUnitRepository.save(docUnit);
  }

  /**
   * Get the most recent status of a document unit.
   *
   * @param documentNumber the document number of the documentation unit
   * @return the most recent publication status of the documentation unit
   */
  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public PublicationStatus getLatestStatus(String documentNumber)
      throws DocumentationUnitNotExistsException {
    return databaseDocumentationUnitRepository
        .findByDocumentNumber(documentNumber)
        .orElseThrow(() -> new DocumentationUnitNotExistsException(documentNumber))
        .getStatus()
        .getPublicationStatus();
  }
}
