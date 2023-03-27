package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.proceedingdecision;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitMetadataRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitMetadataDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.FileNumberRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ProceedingDecisionTransformer;
import de.bund.digitalservice.ris.caselaw.domain.ProceedingDecision;
import de.bund.digitalservice.ris.caselaw.domain.ProceedingDecisionRepository;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class PostgresProceedingDecisionRepositoryImpl implements ProceedingDecisionRepository {
  private final DatabaseProceedingDecisionLinkRepository linkRepository;
  private final DatabaseDocumentUnitMetadataRepository repository;

  private final FileNumberRepository fileNumberRepository;
  private final DocumentTypeRepository documentTypeRepository;

  PostgresProceedingDecisionRepositoryImpl(
      DatabaseDocumentUnitMetadataRepository repository,
      DatabaseProceedingDecisionLinkRepository linkRepository,
      FileNumberRepository fileNumberRepository,
      DocumentTypeRepository documentTypeRepository) {
    this.linkRepository = linkRepository;
    this.repository = repository;
    this.fileNumberRepository = fileNumberRepository;
    this.documentTypeRepository = documentTypeRepository;
  }

  public Flux<ProceedingDecision> findAllForDocumentUnit(UUID parentDocumentUnitUuid) {
    return repository
        .findAllById(
            repository
                .findByUuid(parentDocumentUnitUuid)
                .map(DocumentUnitMetadataDTO::getId)
                .flatMapMany(linkRepository::findAllByParentDocumentUnitId)
                .map(ProceedingDecisionLinkDTO::getChildDocumentUnitId))
        .flatMap(this::injectAdditionalInformation)
        .map(ProceedingDecisionTransformer::transformToDomain);
  }

  private Mono<DocumentUnitMetadataDTO> injectAdditionalInformation(
      DocumentUnitMetadataDTO documentUnitMetadataDTO) {
    return injectFileNumbers(documentUnitMetadataDTO).flatMap(this::injectDocumentType);
  }

  private Mono<DocumentUnitMetadataDTO> injectFileNumbers(
      DocumentUnitMetadataDTO documentUnitMetadataDTO) {
    return fileNumberRepository
        .findAllByDocumentUnitId(documentUnitMetadataDTO.getId())
        .collectList()
        .map(
            fileNumbers -> {
              documentUnitMetadataDTO.setFileNumbers(
                  fileNumbers.stream()
                      .filter(fileNumberDTO -> !fileNumberDTO.getIsDeviating())
                      .toList());
              return documentUnitMetadataDTO;
            });
  }

  private Mono<DocumentUnitMetadataDTO> injectDocumentType(
      DocumentUnitMetadataDTO documentUnitMetadataDTO) {
    if (documentUnitMetadataDTO.getDocumentTypeId() == null) {
      return Mono.just(documentUnitMetadataDTO);
    }
    return documentTypeRepository
        .findById(documentUnitMetadataDTO.getDocumentTypeId())
        .defaultIfEmpty(DocumentTypeDTO.builder().build())
        .map(
            documentTypeDTO -> {
              documentUnitMetadataDTO.setDocumentTypeDTO(documentTypeDTO);
              return documentUnitMetadataDTO;
            });
  }

  public Mono<ProceedingDecisionLinkDTO> linkProceedingDecisions(
      UUID parentDocumentUnitUuid, UUID childDocumentUnitUuid) {
    Mono<Long> parentDocumentUnitId =
        repository.findByUuid(parentDocumentUnitUuid).map(DocumentUnitMetadataDTO::getId);
    Mono<Long> childDocumentUnitId =
        repository.findByUuid(childDocumentUnitUuid).map(DocumentUnitMetadataDTO::getId);

    return Mono.zip(parentDocumentUnitId, childDocumentUnitId)
        .flatMap(
            tuple ->
                linkRepository.save(
                    ProceedingDecisionLinkDTO.builder()
                        .parentDocumentUnitId(tuple.getT1())
                        .childDocumentUnitId(tuple.getT2())
                        .build()));
  }
}
