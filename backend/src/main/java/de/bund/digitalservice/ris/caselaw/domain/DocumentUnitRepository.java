package de.bund.digitalservice.ris.caselaw.domain;

import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;
import reactor.core.publisher.Mono;

@NoRepositoryBean
public interface DocumentUnitRepository {

  Mono<DocumentUnit> findByDocumentNumber(String documentNumber);

  DocumentUnit findByUuid(UUID uuid);

  Mono<DocumentUnit> createNewDocumentUnit(
      String documentNumber, DocumentationOffice documentationOffice);

  Mono<DocumentUnit> save(DocumentUnit documentUnit);

  Mono<DocumentUnit> attachFile(
      UUID documentUnitUuid, String fileUuid, String type, String fileName);

  DocumentUnit removeFile(UUID documentUnitId);

  void delete(DocumentUnit documentUnit);

  <T extends RelatedDocumentationUnit> Page<T> searchByRelatedDocumentationUnit(
      T linkedDocumentationUnit, Pageable pageable);

  Page<DocumentationUnitSearchEntry> searchByDocumentUnitSearchInput(
      Pageable pageable,
      DocumentationOffice documentationOffice,
      DocumentUnitSearchInput searchInput);

  Map<RelatedDocumentationType, Long> getAllDocumentationUnitWhichLink(UUID documentUnitUuid);
}
