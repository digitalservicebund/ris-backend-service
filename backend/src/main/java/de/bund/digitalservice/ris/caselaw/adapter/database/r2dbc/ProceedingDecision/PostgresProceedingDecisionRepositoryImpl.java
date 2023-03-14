package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.ProceedingDecision;

import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitBuilder;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.*;
import de.bund.digitalservice.ris.caselaw.domain.DataSource;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.ProceedingDecision;
import de.bund.digitalservice.ris.caselaw.domain.ProceedingDecisionRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

@Repository
public class PostgresProceedingDecisionRepositoryImpl implements ProceedingDecisionRepository {

    private final DatabaseProceedingDecisionRepository repository;
    private final DatabaseProceedingDecisionLinkRepository linkRepository;
    private final PostgresDocumentUnitRepositoryImpl documentUnitRepository;

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

    @Override
    public Flux<ProceedingDecisionDTO> saveAll(List<ProceedingDecisionDTO> proceedingDecisionDTOs, Long parentDocumentUnitId) {

        List<ProceedingDecisionLinkDTO> proceedingDecisionLinkDTOs = proceedingDecisionDTOs.stream().map(decisionDTO -> {
            return ProceedingDecisionLinkDTO.builder().parentDocumentUnitId(parentDocumentUnitId).childDocumentUnitId(decisionDTO.getId()).build();
        }).toList();

        linkRepository.saveAll(proceedingDecisionLinkDTOs);
        proceedingDecisionDTOs.stream().map(decisionDTO -> {
            return documentUnitRepository.save(DocumentUnitBuilder.newInstance().setDocumentUnitDTO(
                    DocumentUnitDTO.builder()
                            .dataSource(DataSourceDTO.PROCEEDING_DECISION)
                            .courtLocation(decisionDTO.getCourtLocation())
                            .courtType(decisionDTO.getCourtType())
                            .decisionDate(decisionDTO.getDecisionDate())
                            .fileNumbers(Arrays.asList(FileNumberDTO.builder().fileNumber(decisionDTO.getFileNumber()).build()))
                            .build()).build());
        });

        //TODO Domain Respository anpassen, so dass nur mit domain objekten gearbeitet wird
        //TODO Transformer nutzen?

        return proceedingDecisionDTOs;
    }

}
