package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.proceedingdecision;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.FileNumberDTO;
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
  private final DatabaseProceedingDecisionRepository repository;

  private final FileNumberRepository fileNumberRepository;
  private final DocumentTypeRepository documentTypeRepository;

  PostgresProceedingDecisionRepositoryImpl(
          DatabaseProceedingDecisionRepository repository,
          DatabaseProceedingDecisionLinkRepository linkRepository,
          FileNumberRepository fileNumberRepository,
          DocumentTypeRepository documentTypeRepository) {
    this.linkRepository = linkRepository;
    this.repository = repository;
    this.fileNumberRepository = fileNumberRepository;
    this.documentTypeRepository = documentTypeRepository;
  }

  public Flux<ProceedingDecision> findAllForDocumentUnit(UUID parentDocumentUnitUuid){
    return repository.findAllById(
            repository
                    .findByUuid(parentDocumentUnitUuid)
                    .map(ProceedingDecisionDTO::id)
                    .flatMapMany(linkRepository::findAllByParentDocumentUnitId)
                    .map(ProceedingDecisionLinkDTO::getChildDocumentUnitId)
            )
            .flatMap(this::injectAdditionalInformation)
            .map(ProceedingDecisionTransformer::transformToDomain);
    };

  private Mono<ProceedingDecisionDTO> injectAdditionalInformation(ProceedingDecisionDTO proceedingDecisionDTO) {
    return injectFileNumbers(proceedingDecisionDTO)
            .flatMap(this::injectDocumentType);
  }

  private Mono<ProceedingDecisionDTO> injectFileNumbers(ProceedingDecisionDTO proceedingDecisionDTO) {
    return fileNumberRepository
            .findAllByDocumentUnitId(proceedingDecisionDTO.id())
            .collectList()
            .map(
                    fileNumbers -> {
                      proceedingDecisionDTO.toBuilder().fileNumbers(
                              fileNumbers.stream()
                                      .filter(fileNumberDTO -> !fileNumberDTO.getIsDeviating())
                                      .toList());
                      return proceedingDecisionDTO;
                    });
  }

  private Mono<ProceedingDecisionDTO> injectDocumentType(ProceedingDecisionDTO proceedingDecisionDTO) {
    if (proceedingDecisionDTO.documentTypeId() == null) {
      return Mono.just(proceedingDecisionDTO);
    }
    return documentTypeRepository
            .findById(proceedingDecisionDTO.documentTypeId())
            .defaultIfEmpty(DocumentTypeDTO.builder().build())
            .map(
                    documentTypeDTO -> {
                      proceedingDecisionDTO.toBuilder().documentTypeDTO(documentTypeDTO);
                      return proceedingDecisionDTO;
                    });
  }

    public Mono<ProceedingDecisionLinkDTO> linkProceedingDecisions(UUID parentDocumentUnitUuid, UUID childDocumentUnitUuid) {
    Mono<Long> parentDocumentUnitId =
            repository.findByUuid(parentDocumentUnitUuid).map(ProceedingDecisionDTO::id);
    Mono<Long> childDocumentUnitId =
            repository.findByUuid(childDocumentUnitUuid).map(ProceedingDecisionDTO::id);

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
