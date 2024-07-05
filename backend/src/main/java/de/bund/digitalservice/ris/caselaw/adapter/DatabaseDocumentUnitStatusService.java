package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseStatusRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import java.time.Instant;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DatabaseDocumentUnitStatusService implements DocumentUnitStatusService {

  private final DatabaseStatusRepository repository;

  private final DocumentUnitRepository documentUnitRepository;

  private final DatabaseDocumentationUnitRepository databaseDocumentationUnitRepository;

  public DatabaseDocumentUnitStatusService(
      DatabaseStatusRepository repository,
      DatabaseDocumentationUnitRepository databaseDocumentationUnitRepository,
      DocumentUnitRepository documentUnitRepository) {
    this.repository = repository;
    this.documentUnitRepository = documentUnitRepository;
    this.databaseDocumentationUnitRepository = databaseDocumentationUnitRepository;
  }

  @Override
  public DocumentUnit setInitialStatus(DocumentUnit documentUnit)
      throws DocumentationUnitNotExistsException {

    repository.save(
        StatusDTO.builder()
            .createdAt(Instant.now())
            .documentationUnitDTO(
                databaseDocumentationUnitRepository.getReferenceById(documentUnit.uuid()))
            .publicationStatus(PublicationStatus.UNPUBLISHED)
            .withError(false)
            .build());

    return documentUnitRepository
        .findByUuid(documentUnit.uuid())
        .orElseThrow(() -> new DocumentationUnitNotExistsException(documentUnit.uuid()));
  }

  @Override
  public DocumentUnit setToPublishing(
      DocumentUnit documentUnit, Instant publishDate, String issuerAddress)
      throws DocumentationUnitNotExistsException {

    repository.save(
        StatusDTO.builder()
            .createdAt(publishDate)
            .documentationUnitDTO(
                databaseDocumentationUnitRepository.getReferenceById(documentUnit.uuid()))
            .publicationStatus(PublicationStatus.PUBLISHING)
            .withError(false)
            .issuerAddress(issuerAddress)
            .build());

    return documentUnitRepository
        .findByUuid(documentUnit.uuid())
        .orElseThrow(() -> new DocumentationUnitNotExistsException(documentUnit.uuid()));
  }

  @Override
  public void update(String documentNumber, Status status)
      throws DocumentationUnitNotExistsException {
    saveStatus(status, getLatestPublishing(documentNumber));
  }

  @Override
  public void update(UUID documentUuid, Status status) throws DocumentationUnitNotExistsException {
    saveStatus(status, getLatestPublishing(documentUuid));
  }

  private void saveStatus(Status status, StatusDTO previousStatusDTO) {

    repository.save(
        StatusDTO.builder()
            .createdAt(Instant.now())
            .documentationUnitDTO(previousStatusDTO.getDocumentationUnitDTO())
            .issuerAddress(previousStatusDTO.getIssuerAddress())
            .publicationStatus(status.publicationStatus())
            .withError(status.withError())
            .build());
  }

  public String getLatestIssuerAddress(String documentNumber) {
    try {
      return getLatestPublishing(documentNumber).getIssuerAddress();
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public PublicationStatus getLatestStatus(UUID documentUuid) {
    StatusDTO entity =
        repository.findFirstByDocumentationUnitDTOOrderByCreatedAtDesc(
            databaseDocumentationUnitRepository.getReferenceById(documentUuid));

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
        .findFirstByDocumentationUnitDTO_IdAndPublicationStatusOrderByCreatedAtDesc(
            documentUuid, PublicationStatus.PUBLISHING)
        .orElseThrow(() -> new DocumentationUnitNotExistsException(documentUuid));
  }
}
