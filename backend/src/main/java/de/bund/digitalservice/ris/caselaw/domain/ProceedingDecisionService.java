package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class ProceedingDecisionService {
  private final ProceedingDecisionRepository repository;
  private final DocumentUnitService documentUnitService;

  public ProceedingDecisionService(
      ProceedingDecisionRepository repository, DocumentUnitService documentUnitService) {
    this.repository = repository;
    this.documentUnitService = documentUnitService;
  }

  @Transactional(transactionManager = "connectionFactoryTransactionManager")
  public Flux<ProceedingDecision> addProceedingDecision(
      UUID documentUnitUuid, ProceedingDecision proceedingDecision) {

    return documentUnitService
        .generateNewDocumentUnit(new DocumentUnitCreationInfo("KO", "RE"))
        .flatMap(
            documentUnit ->
                repository
                    .linkProceedingDecisions(documentUnitUuid, documentUnit.uuid())
                    .map(v -> documentUnit))
        .flatMap(
            documentUnit ->
                documentUnitService.updateDocumentUnit(
                    enrichNewDocumentUnitWithData(documentUnit, proceedingDecision)))
        .flatMapMany(documentUnit -> repository.findAllForDocumentUnit(documentUnitUuid));
  }

  private DocumentUnit enrichNewDocumentUnitWithData(
      DocumentUnit documentUnit, ProceedingDecision proceedingDecision) {
    List<String> fileNumbers = null;
    if (!StringUtils.isBlank(proceedingDecision.fileNumber())) {
      fileNumbers = List.of(proceedingDecision.fileNumber());
    }

    CoreData coreData =
        documentUnit.coreData().toBuilder()
            .fileNumbers(fileNumbers)
            .documentType(proceedingDecision.documentType())
            .decisionDate(proceedingDecision.date())
            .court(proceedingDecision.court())
            .build();

    return documentUnit.toBuilder()
        .dataSource(DataSource.PROCEEDING_DECISION)
        .coreData(coreData)
        .build();
  }
}
