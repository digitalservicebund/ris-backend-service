package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.NoRepositoryBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@NoRepositoryBean
public interface DocumentUnitRepository {

  Mono<DocumentUnit> findByDocumentNumber(String documentNumber);

  Mono<DocumentUnit> findByUuid(UUID uuid);

  Mono<DocumentUnit> createNewDocumentUnit(String documentNumber);

  Mono<DocumentUnit> save(DocumentUnit documentUnit);

  Mono<DocumentUnit> attachFile(
      UUID documentUnitUuid, String fileUuid, String type, String fileName);

  Mono<DocumentUnit> removeFile(UUID documentUnitId);

  Mono<Void> delete(DocumentUnit documentUnit);

  Flux<ProceedingDecision> searchForDocumentUnitsByProceedingDecisionInput(
      ProceedingDecision proceedingDecision);

  Flux<DocumentUnitListEntry> findAll(Sort sort);

  Flux<ProceedingDecision> findAllLinkedDocumentUnitsByParentDocumentUnitId(
      UUID parentDocumentUnitUuid);

  Mono<DocumentUnit> filterUnlinkedDocumentUnit(DocumentUnit documentUnit);

  Mono<DocumentUnit> linkDocumentUnits(UUID parentDocumentUnitUuid, UUID childDocumentUnitUuid);

  Mono<Void> unlinkDocumentUnits(UUID parentDocumentUnitUuid, UUID childDocumentUnitUuid);

  Mono<Long> countLinksByChildDocumentUnitUuid(UUID childDocumentUnitUuid);
}
