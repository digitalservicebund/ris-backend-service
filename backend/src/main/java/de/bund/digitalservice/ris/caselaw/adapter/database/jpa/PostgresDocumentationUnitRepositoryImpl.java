package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationUnitTransformer;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitSearchInput;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitException;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitLink;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitLinkType;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitSearchEntry;
import de.bund.digitalservice.ris.caselaw.domain.LinkedDocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@Slf4j
@Primary
public class PostgresDocumentationUnitRepositoryImpl implements DocumentUnitRepository {
  private final DatabaseDocumentationUnitRepository repository;
  private final DatabaseFileNumberRepository fileNumberRepository;
  private final DatabaseDocumentTypeRepository databaseDocumentTypeRepository;
  private final DatabaseDocumentCategoryRepository databaseDocumentCategoryRepository;
  private final DatabaseCourtRepository databaseCourtRepository;
  private final DatabaseNormReferenceRepository documentUnitNormRepository;
  private final DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  private final JPADatabaseKeywordRepository keywordRepository;
  private final DatabaseNormAbbreviationRepository normAbbreviationRepository;
  private final EntityManager entityManager;

  public PostgresDocumentationUnitRepositoryImpl(
      DatabaseDocumentationUnitRepository repository,
      DatabaseFileNumberRepository fileNumberRepository,
      DatabaseDocumentTypeRepository databaseDocumentTypeRepository,
      DatabaseDocumentCategoryRepository databaseDocumentCategoryRepository,
      DatabaseCourtRepository databaseCourtRepository,
      DatabaseNormReferenceRepository documentUnitNormRepository,
      DatabaseNormAbbreviationRepository normAbbreviationRepository,
      DatabaseDocumentationOfficeRepository documentationOfficeRepository,
      JPADatabaseKeywordRepository keywordRepository,
      EntityManager entityManager) {

    this.repository = repository;
    this.fileNumberRepository = fileNumberRepository;
    this.databaseDocumentTypeRepository = databaseDocumentTypeRepository;
    this.databaseCourtRepository = databaseCourtRepository;
    this.databaseDocumentCategoryRepository = databaseDocumentCategoryRepository;
    this.documentUnitNormRepository = documentUnitNormRepository;
    this.documentationOfficeRepository = documentationOfficeRepository;
    this.normAbbreviationRepository = normAbbreviationRepository;
    this.keywordRepository = keywordRepository;
    this.entityManager = entityManager;
  }

  @Override
  public Mono<DocumentUnit> findByDocumentNumber(String documentNumber) {
    if (log.isDebugEnabled()) {
      log.debug("find by document number: {}", documentNumber);
    }

    return Mono.just(
        DocumentationUnitTransformer.transformToDomain(
            repository.findByDocumentNumber(documentNumber).orElse(null)));
  }

  @Override
  public Mono<DocumentUnit> findByUuid(UUID uuid) {
    if (log.isDebugEnabled()) {
      log.debug("find by uuid: {}", uuid);
    }
    return Mono.just(
        DocumentationUnitTransformer.transformToDomain(repository.findById(uuid).orElse(null)));
  }

  @Override
  public Mono<DocumentUnit> createNewDocumentUnit(
      String documentNumber, DocumentationOffice documentationOffice) {

    return Mono.just(
            documentationOfficeRepository.findByAbbreviation(documentationOffice.abbreviation()))
        .flatMap(
            documentationOfficeDTO ->
                Mono.just(
                    repository.save(
                        DocumentationUnitDTO.builder()
                            .id(UUID.randomUUID())
                            .documentNumber(documentNumber)
                            .documentationOffice(documentationOfficeDTO)
                            // TODO is decisionDate = null the same as dateKnown? .dateKnown(true)
                            .legalEffect(LegalEffectDTO.KEINE_ANGABE)
                            .build())))
        .map(DocumentationUnitTransformer::transformToDomain);
  }

  @Override
  @Transactional(transactionManager = "connectionFactoryTransactionManager")
  public Mono<DocumentUnit> save(DocumentUnit documentUnit) {

    DocumentationUnitDTO documentationUnitDTO =
        repository.findById(documentUnit.uuid()).orElse(null);
    if (documentationUnitDTO == null) {
      log.info("Can't save non-existing docUnit with id = " + documentUnit.uuid());
      return Mono.empty();
    }

    // ---
    // Doing database-related (pre) transformation
    if (documentUnit.coreData() != null && documentUnit.coreData().documentType() != null) {
      documentationUnitDTO.setDocumentType(getDbDocType(documentUnit.coreData().documentType()));
    }

    documentationUnitDTO = saveKeywords(documentationUnitDTO, documentUnit);

    if (documentUnit.coreData() != null && documentUnit.coreData().court() != null) {
      var court =
          databaseCourtRepository
              .findByTypeAndLocation(
                  documentUnit.coreData().court().type(),
                  documentUnit.coreData().court().location())
              .stream()
              .findFirst();
      if (court.isEmpty()) {
        throw new DocumentationUnitException("no court found.");
      }
      documentationUnitDTO.setCourt(court.get());
    }
    // ---

    // Transform non-database-related properties
    documentationUnitDTO =
        DocumentationUnitTransformer.transformToDTO(documentationUnitDTO, documentUnit);
    documentationUnitDTO = repository.save(documentationUnitDTO);

    return Mono.just(DocumentationUnitTransformer.transformToDomain(documentationUnitDTO));
  }

  private DocumentationUnitDTO saveKeywords(
      DocumentationUnitDTO documentationUnitDTO, DocumentUnit documentUnit) {
    if (documentUnit != null && documentUnit.contentRelatedIndexing() != null) {
      ContentRelatedIndexing contentRelatedIndexing = documentUnit.contentRelatedIndexing();

      if (contentRelatedIndexing.keywords() != null) {
        Set<KeywordDTO> keywordDTOs = new HashSet<>();
        List<String> keywords = contentRelatedIndexing.keywords();
        for (int i = 0; i < keywords.size(); i++) {
          String value = keywords.get(i);
          keywordRepository
              .findByValue(value)
              .ifPresentOrElse(
                  keywordDTO -> keywordDTOs.add(keywordDTO),
                  () ->
                      keywordDTOs.add(
                          KeywordDTO.builder().id(UUID.randomUUID()).value(value).build()));
        }
        documentationUnitDTO.setKeywords(keywordDTOs);
      }
    }
    return documentationUnitDTO;
  }

  private DocumentTypeDTO getDbDocType(DocumentType documentType) {
    // TODO cache category at application start
    DocumentTypeDTO docTypeDTO =
        databaseDocumentTypeRepository.findFirstByAbbreviationAndCategory(
            documentType.jurisShortcut(), databaseDocumentCategoryRepository.findFirstByLabel("R"));

    if (docTypeDTO == null) {
      throw new DocumentationUnitException(
          "no document type for the shortcut '" + documentType.jurisShortcut() + "' found.");
    }
    return docTypeDTO;
  }

  @Override
  public Mono<DocumentUnit> attachFile(
      UUID documentUnitUuid, String fileUuid, String type, String fileName) {
    var docUnitDto = repository.findById(documentUnitUuid).orElseThrow();
    docUnitDto.setOriginalFileDocument(
        OriginalFileDocumentDTO.builder()
            .s3ObjectPath(fileUuid)
            .filename(fileName)
            .extension(type)
            .uploadTimestamp(Instant.now())
            .build());
    docUnitDto = repository.save(docUnitDto);
    return Mono.just(DocumentationUnitTransformer.transformToDomain(docUnitDto));
  }

  @Override
  public Mono<DocumentUnit> removeFile(UUID documentUnitId) {
    var docUnitDto = repository.findById(documentUnitId).orElseThrow();
    docUnitDto.setOriginalFileDocument(null);
    docUnitDto = repository.save(docUnitDto);
    return Mono.just(DocumentationUnitTransformer.transformToDomain(docUnitDto));
  }

  @Override
  public Mono<Void> delete(DocumentUnit documentUnit) {
    repository.deleteById(documentUnit.uuid());
    return Mono.empty();
  }

  @Override
  public Mono<Long> count() {
    return Mono.just(repository.count());
  }

  @Override
  public Flux<LinkedDocumentationUnit> searchByLinkedDocumentationUnit(
      LinkedDocumentationUnit linkedDocumentationUnit, Pageable pageable) {
    throw new UnsupportedOperationException("not implemented yet");
  }

  public Page<DocumentationUnitSearchEntry> searchByDocumentUnitSearchInput(
      Pageable pageable,
      DocumentationOffice documentationOffice,
      DocumentUnitSearchInput searchInput) {
    if (log.isDebugEnabled()) {
      log.debug("Find by overview search: {}, {}", documentationOffice, searchInput);
    }
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public Flux<LinkedDocumentationUnit> findAllLinkedDocumentUnitsByParentDocumentUnitUuidAndType(
      UUID parentDocumentUnitUuid, DocumentationUnitLinkType type) {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public Mono<DocumentationUnitLink> linkDocumentUnits(
      UUID parentDocumentUnitUuid, UUID childDocumentUnitUuid, DocumentationUnitLinkType type) {
    if (log.isDebugEnabled()) {
      log.debug(
          "link documentation unitst: {}, {}, {}",
          parentDocumentUnitUuid,
          childDocumentUnitUuid,
          type);
    }

    if (parentDocumentUnitUuid == null || childDocumentUnitUuid == null || type == null) {
      return Mono.empty();
    }

    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public Mono<Void> unlinkDocumentUnit(
      UUID parentDocumentationUnitUuid,
      UUID childDocumentationUnitUuid,
      DocumentationUnitLinkType type) {
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public Mono<Long> countLinksByChildDocumentUnitUuid(UUID childDocumentUnitUuid) {
    if (log.isDebugEnabled()) {
      log.debug("count links by child documentation unit uuid: {}", childDocumentUnitUuid);
    }
    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  // TODO how to delete orphaned without dataSource
  public Mono<Void> deleteIfOrphanedLinkedDocumentationUnit(UUID documentUnitUuid) {
    if (log.isDebugEnabled()) {
      log.debug("delete if orphaned linked documentation unit: {}", documentUnitUuid);
    }

    throw new UnsupportedOperationException("not implemented yet");
  }

  @Override
  public Mono<Long> countSearchByLinkedDocumentationUnit(
      LinkedDocumentationUnit linkedDocumentationUnit) {

    throw new UnsupportedOperationException("not implemented yet");
  }
}
