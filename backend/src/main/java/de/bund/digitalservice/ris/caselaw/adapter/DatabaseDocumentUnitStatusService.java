package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseStatusRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import java.time.Instant;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

  @Override
  public void update(String documentNumber, Status status)
      throws DocumentationUnitNotExistsException {
    saveStatus(status, getLatestPublishing(documentNumber));
  }

  private void saveStatus(Status status, StatusDTO previousStatusDTO) {
    repository.save(
        StatusDTO.builder()
            .createdAt(Instant.now())
            .documentationUnitDTO(previousStatusDTO.getDocumentationUnitDTO())
            .publicationStatus(status.publicationStatus())
            .withError(status.withError())
            .build());
  }

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

  private StatusDTO getLatestPublishing(String documentNumber)
      throws DocumentationUnitNotExistsException {
    var documentUnit =
        databaseDocumentationUnitRepository
            .findByDocumentNumber(documentNumber)
            .orElseThrow(() -> new DocumentationUnitNotExistsException(documentNumber));
    return getLatestPublishing(documentUnit.getId());
  }

  private StatusDTO getLatestPublishing(UUID documentUuid)
      throws DocumentationUnitNotExistsException {
    return repository
        .findFirstByDocumentationUnitDTO_IdOrderByCreatedAtDesc(documentUuid)
        .orElseThrow(() -> new DocumentationUnitNotExistsException(documentUuid));
  }
}
