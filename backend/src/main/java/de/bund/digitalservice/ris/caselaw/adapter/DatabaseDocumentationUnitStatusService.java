package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitStatusService;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Service to handle the status of a document unit in the database. */
@Service
@Slf4j
public class DatabaseDocumentationUnitStatusService implements DocumentationUnitStatusService {

  private final DatabaseDocumentationUnitRepository databaseDocumentationUnitRepository;

  public DatabaseDocumentationUnitStatusService(
      DatabaseDocumentationUnitRepository databaseDocumentationUnitRepository) {
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
    DocumentationUnitDTO docUnit =
        databaseDocumentationUnitRepository
            .findByDocumentNumber(documentNumber)
            .orElseThrow(() -> new DocumentationUnitNotExistsException(documentNumber));

    docUnit.toBuilder()
        .status(
            List.of(
                StatusDTO.builder()
                    .createdAt(Instant.now())
                    .publicationStatus(status.publicationStatus())
                    .withError(status.withError())
                    .build()))
        .build();

    databaseDocumentationUnitRepository.save(docUnit);
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
    List<StatusDTO> status = docUnit.get().getStatus();
    status.sort((s1, s2) -> s2.getCreatedAt().compareTo(s1.getCreatedAt()));
    var mostRecentStatus = status.get(0);
    if (mostRecentStatus == null) {
      return null;
    }

    return mostRecentStatus.getPublicationStatus();
  }
}
