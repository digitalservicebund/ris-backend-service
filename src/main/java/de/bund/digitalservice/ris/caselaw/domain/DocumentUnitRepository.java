package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;
import org.springframework.data.repository.NoRepositoryBean;
import reactor.core.publisher.Mono;

@NoRepositoryBean
public interface DocumentUnitRepository {

  Mono<DocumentUnit> findByDocumentNumber(String documentNumber);

  Mono<DocumentUnit> findByUuid(UUID uuid);

  Mono<DocumentUnit> createNewDocumentUnit(String documentNumber);

  Mono<DocumentUnit> save(DocumentUnit documentUnit);

  Mono<DocumentUnit> attachFile(
      UUID documentUnitUuid, String fileUuid, String docx, String fileName);

  Mono<DocumentUnit> removeFile(UUID documentUnitId);

  Mono<Void> delete(DocumentUnit documentUnit);
}
