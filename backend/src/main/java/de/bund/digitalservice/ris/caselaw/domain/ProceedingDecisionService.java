package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.ProceedingDecisionRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.swing.text.Document;
import java.util.List;
import java.util.UUID;

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

  public Flux<ProceedingDecision> getProceedingDecisionsForDocumentUnit(UUID documentUnitUuid) {
    return repository.findAllForDocumentUnit(documentUnitUuid);
  }
  
  @Transactional(transactionManager = "connectionFactoryTransactionManager")
  public Flux<ProceedingDecision> addProceedingDecision(UUID documentUnitUuid, ProceedingDecision proceedingDecision) {

    return documentUnitService.generateNewDocumentUnit(new DocumentUnitCreationInfo("KO", "RE"))
            .flatMap(documentUnit -> repository.linkProceedingDecisions(documentUnitUuid, documentUnit.uuid()).map(v -> documentUnit)
            )
            .map(documentUnit ->
                    documentUnitService.updateDocumentUnit(enrichNewDocumentUnitWithData(documentUnit, proceedingDecision))
            )
            .flatMapMany(documentUnit ->
                    repository.findAllForDocumentUnit(documentUnitUuid)
            );
    }

  private DocumentUnit enrichNewDocumentUnitWithData(DocumentUnit documentUnit, ProceedingDecision proceedingDecision) {
    CoreData coreData = documentUnit.coreData().toBuilder()
            .fileNumbers(List.of(proceedingDecision.fileNumber()))
            .documentType(proceedingDecision.documentType())
            .decisionDate(proceedingDecision.date())
            .court(proceedingDecision.court())
            .build();

    return documentUnit.toBuilder().coreData(coreData).build();
  }

}
