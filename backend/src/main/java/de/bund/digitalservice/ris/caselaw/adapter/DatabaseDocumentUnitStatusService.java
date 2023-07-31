package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitStatusRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitStatusDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresDocumentUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatus;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class DatabaseDocumentUnitStatusService implements DocumentUnitStatusService {

  private final DatabaseDocumentUnitStatusRepository repository;
  private final PostgresDocumentUnitRepositoryImpl documentUnitRepository;

  public DatabaseDocumentUnitStatusService(
      DatabaseDocumentUnitStatusRepository repository,
      PostgresDocumentUnitRepositoryImpl documentUnitRepository) {
    this.repository = repository;
    this.documentUnitRepository = documentUnitRepository;
  }

  @Override
  public Mono<DocumentUnit> setInitialStatus(DocumentUnit documentUnit) {
    return repository
        .save(
            DocumentUnitStatusDTO.builder()
                .newEntry(true)
                .id(UUID.randomUUID())
                .createdAt(documentUnit.creationtimestamp())
                .documentUnitId(documentUnit.uuid())
                .publicationStatus(PublicationStatus.UNPUBLISHED)
                .withError(false)
                .build())
        .then(documentUnitRepository.findByUuid(documentUnit.uuid()));
  }

  @Override
  public Mono<DocumentUnit> setToPublishing(
      DocumentUnit documentUnit, Instant publishDate, String issuerAddress) {
    return repository
        .save(
            DocumentUnitStatusDTO.builder()
                .newEntry(true)
                .id(UUID.randomUUID())
                .createdAt(publishDate)
                .documentUnitId(documentUnit.uuid())
                .publicationStatus(PublicationStatus.PUBLISHING)
                .withError(false)
                .issuerAddress(issuerAddress)
                .build())
        .then(documentUnitRepository.findByUuid(documentUnit.uuid()));
  }

  @Override
  public Mono<Void> update(String documentNumber, DocumentUnitStatus status) {
    return getLatestPublishing(documentNumber)
        .flatMap(previousStatusDTO -> saveStatus(status, previousStatusDTO))
        .then();
  }

  @Override
  public Mono<Void> update(UUID documentUuid, DocumentUnitStatus status) {
    return getLatestPublishing(documentUuid)
        .flatMap(previousStatusDTO -> saveStatus(status, previousStatusDTO))
        .then();
  }

  @NotNull
  private Mono<DocumentUnitStatusDTO> saveStatus(
      DocumentUnitStatus status, DocumentUnitStatusDTO previousStatusDTO) {
    return repository.save(
        DocumentUnitStatusDTO.builder()
            .newEntry(true)
            .id(UUID.randomUUID())
            .createdAt(Instant.now())
            .documentUnitId(previousStatusDTO.getDocumentUnitId())
            .issuerAddress(previousStatusDTO.getIssuerAddress())
            .publicationStatus(status.publicationStatus())
            .withError(status.withError())
            .build());
  }

  public Mono<String> getLatestIssuerAddress(String documentNumber) {
    return getLatestPublishing(documentNumber).map(DocumentUnitStatusDTO::getIssuerAddress);
  }

  @Override
  public Mono<PublicationStatus> getLatestStatus(UUID documentUuid) {
    return repository
        .findFirstByDocumentUnitIdOrderByCreatedAtDesc(documentUuid)
        .map(DocumentUnitStatusDTO::getPublicationStatus);
  }

  private Mono<DocumentUnitStatusDTO> getLatestPublishing(String documentNumber) {
    return documentUnitRepository
        .findByDocumentNumber(documentNumber)
        .flatMap(
            documentUnit ->
                repository.findFirstByDocumentUnitIdAndPublicationStatusOrderByCreatedAtDesc(
                    documentUnit.uuid(), PublicationStatus.PUBLISHING));
  }

  private Mono<DocumentUnitStatusDTO> getLatestPublishing(UUID documentUuid) {
    return repository.findFirstByDocumentUnitIdAndPublicationStatusOrderByCreatedAtDesc(
        documentUuid, PublicationStatus.PUBLISHING);
  }
}
