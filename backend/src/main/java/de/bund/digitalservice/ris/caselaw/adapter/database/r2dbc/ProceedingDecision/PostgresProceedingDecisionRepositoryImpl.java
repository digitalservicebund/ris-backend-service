package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.ProceedingDecision;

import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitBuilder;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.*;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.ProceedingDecision;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Repository
public class PostgresProceedingDecisionRepositoryImpl {

    private final DatabaseProceedingDecisionRepository repository;
    private final DatabaseProceedingDecisionLinkRepository linkRepository;
    private final PostgresDocumentUnitRepositoryImpl documentUnitRepository;

    private final DatabaseDocumentUnitRepository databaseDocumentUnitRepository;

    public PostgresProceedingDecisionRepositoryImpl(
            DatabaseProceedingDecisionRepository repository, DatabaseProceedingDecisionLinkRepository linkRepository, PostgresDocumentUnitRepositoryImpl documentUnitRepository) {
        this.repository = repository;
        this.linkRepository = linkRepository;
        this.documentUnitRepository = documentUnitRepository;
    }

    @Override
    public Flux<ProceedingDecision> findAllByDocumentUnitId(Long id) {
        return linkRepository.findAllByParentDocumentUnitId(id)
                .flatMap(proceedingDecisionLinkDTO -> repository.findById(proceedingDecisionLinkDTO.getChildDocumentUnitId()))
                .map(proceedingDecisionDTO -> ProceedingDecision.builder()
                        .fileNumber(proceedingDecisionDTO.getFileNumber())
                        .court(
                                DocumentUnitBuilder.newInstance().getCourtObject(
                                        proceedingDecisionDTO.getCourtType(),
                                        proceedingDecisionDTO.getCourtLocation()))
                        .date(proceedingDecisionDTO.getDecisionDate())
                        .build());
    }

    //TODO make it better
    @Override
    public Flux<ProceedingDecisionDTO> saveAll(List<ProceedingDecisionDTO> proceedingDecisionDTOs, Long parentDocumentUnitId) {

        return Flux.fromStream(proceedingDecisionDTOs.stream().map(decisionDTO -> {
            Mono<DocumentUnit> savedDocumentUnit = documentUnitRepository.save(DocumentUnitBuilder.newInstance().setDocumentUnitDTO(
                    DocumentUnitDTO.builder()
                            .dataSource(DataSourceDTO.PROCEEDING_DECISION)
                            .courtLocation(decisionDTO.getCourtLocation())
                            .courtType(decisionDTO.getCourtType())
                            .decisionDate(decisionDTO.getDecisionDate())
                            .fileNumbers(Arrays.asList(FileNumberDTO.builder().fileNumber(decisionDTO.getFileNumber()).build()))
                            .build()).build());


            linkRepository.save(ProceedingDecisionLinkDTO.builder()
                    .parentDocumentUnitId(parentDocumentUnitId)
                    .childDocumentUnitId(databaseDocumentUnitRepository
                            .findByUuid(savedDocumentUnit.block().uuid())
                            .block().getId())
                    .build());

            return databaseDocumentUnitRepository
                    .findByUuid(savedDocumentUnit.block().uuid()).map(documentUnitDTO -> {
                        return ProceedingDecisionDTO.builder()
                                .uuid(documentUnitDTO.getUuid())
                                .id(documentUnitDTO.getId())
                                .courtType(documentUnitDTO.getCourtType())
                                .courtLocation(documentUnitDTO.getCourtLocation())
                                .fileNumber(documentUnitDTO.getFileNumbers().stream().findAny().get().getFileNumber())
                                .build();
                    }).block();
        }));
    }

}
