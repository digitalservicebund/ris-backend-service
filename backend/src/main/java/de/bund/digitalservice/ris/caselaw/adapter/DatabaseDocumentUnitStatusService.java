package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseStatusRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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
  public Mono<DocumentUnit> setInitialStatus(DocumentUnit documentUnit) {
    try {
      return Mono.just(
              repository.save(
                  StatusDTO.builder()
                      .createdAt(Instant.now())
                      .documentationUnitDTO(
                          databaseDocumentationUnitRepository.getReferenceById(documentUnit.uuid()))
                      .publicationStatus(PublicationStatus.UNPUBLISHED)
                      .withError(false)
                      .build()))
          .then(
              Mono.just(
                  documentUnitRepository
                      .findByUuid(documentUnit.uuid())
                      .orElseThrow(
                          () -> new DocumentationUnitNotExistsException(documentUnit.uuid()))));
    } catch (DocumentationUnitNotExistsException e) {
      return Mono.error(e);
    }
  }

  @Override
  public Mono<DocumentUnit> setToPublishing(
      DocumentUnit documentUnit, Instant publishDate, String issuerAddress) {
    try {
      return Mono.just(
              repository.save(
                  StatusDTO.builder()
                      .createdAt(publishDate)
                      .documentationUnitDTO(
                          databaseDocumentationUnitRepository.getReferenceById(documentUnit.uuid()))
                      .publicationStatus(PublicationStatus.PUBLISHING)
                      .withError(false)
                      .issuerAddress(issuerAddress)
                      .build()))
          .then(
              Mono.just(
                  documentUnitRepository
                      .findByUuid(documentUnit.uuid())
                      .orElseThrow(
                          () -> new DocumentationUnitNotExistsException(documentUnit.uuid()))));
    } catch (DocumentationUnitNotExistsException e) {
      return Mono.error(e);
    }
  }

  @Override
  public Mono<Void> update(String documentNumber, Status status)
      throws DocumentationUnitNotExistsException {

    return getLatestPublishing(documentNumber)
        .map(previousStatusDTO -> saveStatus(status, previousStatusDTO))
        .then();
  }

  @Override
  public Mono<Void> update(UUID documentUuid, Status status) {
    return getLatestPublishing(documentUuid)
        .flatMap(previousStatusDTO -> saveStatus(status, previousStatusDTO))
        .then();
  }

  @NotNull
  private Mono<StatusDTO> saveStatus(Status status, StatusDTO previousStatusDTO) {

    return Mono.just(
        repository.save(
            StatusDTO.builder()
                .createdAt(Instant.now())
                .documentationUnitDTO(previousStatusDTO.getDocumentationUnitDTO())
                .issuerAddress(previousStatusDTO.getIssuerAddress())
                .publicationStatus(status.publicationStatus())
                .withError(status.withError())
                .build()));
  }

  public Mono<String> getLatestIssuerAddress(String documentNumber) {
    try {
      return getLatestPublishing(documentNumber).map(StatusDTO::getIssuerAddress);
    } catch (Exception e) {
      return Mono.empty();
    }
  }

  @Override
  public Mono<PublicationStatus> getLatestStatus(UUID documentUuid) {
    return Mono.just(
            repository.findFirstByDocumentationUnitDTOOrderByCreatedAtDesc(
                databaseDocumentationUnitRepository.getReferenceById(documentUuid)))
        .map(StatusDTO::getPublicationStatus);
  }

  private Mono<StatusDTO> getLatestPublishing(String documentNumber)
      throws DocumentationUnitNotExistsException {
    var documentUnit =
        databaseDocumentationUnitRepository
            .findByDocumentNumber(documentNumber)
            .orElseThrow(() -> new DocumentationUnitNotExistsException(documentNumber));
    return getLatestPublishing(documentUnit.getId());
  }

  private Mono<StatusDTO> getLatestPublishing(UUID documentUuid) {
    try {
      return Mono.just(
          repository
              .findFirstByDocumentationUnitDTO_IdAndPublicationStatusOrderByCreatedAtDesc(
                  documentUuid, PublicationStatus.PUBLISHING)
              .orElseThrow(() -> new DocumentationUnitNotExistsException(documentUuid)));
    } catch (Exception e) {
      return Mono.error(e);
    }
  }
}
