package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseStatusRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Service to handle the status of a document unit in the database. */
@Service
@Slf4j
public class DatabaseDocumentUnitStatusService implements DocumentUnitStatusService {

  private final DatabaseStatusRepository repository;

  private final DatabaseDocumentationUnitRepository databaseDocumentationUnitRepository;

  public DatabaseDocumentUnitStatusService(
      DatabaseStatusRepository repository,
      DatabaseDocumentationUnitRepository databaseDocumentationUnitRepository) {
    this.repository = repository;
    this.databaseDocumentationUnitRepository = databaseDocumentationUnitRepository;
  }

  /**
   * Update the status of a document unit.
   *
   * @param documentNumber the document number of the documentation unit
   * @param status the new status
   * @throws DocumentationUnitNotExistsException if the documentation unit does not exist
   */
  @Override
  public void update(String documentNumber, Status status)
      throws DocumentationUnitNotExistsException {
    repository.save(
        StatusDTO.builder()
            .createdAt(Instant.now())
            .documentationUnitDTO(
                databaseDocumentationUnitRepository
                    .findByDocumentNumber(documentNumber)
                    .orElseThrow(() -> new DocumentationUnitNotExistsException(documentNumber)))
            .publicationStatus(status.publicationStatus())
            .withError(status.withError())
            .build());
  }

  /**
   * Get the most recent status of a document unit.
   *
   * @param documentNumber the document number of the documentation unit
   * @return the most recent publication status of the documentation unit
   */
  @Override
  public PublicationStatus getLatestStatus(String documentNumber) {
    var docUnit = databaseDocumentationUnitRepository.findByDocumentNumber(documentNumber);
    if (docUnit.isEmpty()) {
      return null;
    }
    StatusDTO entity =
        repository.findFirstByDocumentationUnitDTOOrderByCreatedAtDesc(docUnit.get());
    if (entity == null) {
      return null;
    }

    return entity.getPublicationStatus();
  }
}
