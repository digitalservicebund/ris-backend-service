package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentTypeTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationUnitTransformer;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitSearchInput;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatus;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitException;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitSearchEntry;
import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
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
  private final DatabaseProcedureRepository databaseProcedureRepository;
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
      DatabaseProcedureRepository databaseProcedureRepository,
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
    this.databaseProcedureRepository = databaseProcedureRepository;
    this.documentUnitNormRepository = documentUnitNormRepository;
    this.documentationOfficeRepository = documentationOfficeRepository;
    this.normAbbreviationRepository = normAbbreviationRepository;
    this.keywordRepository = keywordRepository;
    this.entityManager = entityManager;
  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public Mono<DocumentUnit> findByDocumentNumber(String documentNumber) {
    if (log.isDebugEnabled()) {
      log.debug("find by document number: {}", documentNumber);
    }

    return Mono.just(
        DocumentationUnitTransformer.transformToDomain(
            repository.findByDocumentNumber(documentNumber).orElse(null)));
  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public DocumentUnit findByUuid(UUID uuid) {
    if (log.isDebugEnabled()) {
      log.debug("find by uuid: {}", uuid);
    }

    return DocumentationUnitTransformer.transformToDomain(repository.findById(uuid).orElse(null));
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
  @Transactional(transactionManager = "jpaTransactionManager")
  public Mono<DocumentUnit> save(DocumentUnit documentUnit) {

    DocumentationUnitDTO documentationUnitDTO =
        repository.findById(documentUnit.uuid()).orElse(null);
    if (documentationUnitDTO == null) {
      log.info("Can't save non-existing docUnit with id = " + documentUnit.uuid());
      return Mono.empty();
    }

    // ---
    // Doing database-related (pre) transformation
    if (documentUnit.coreData() != null) {

      if (documentUnit.coreData().documentType() != null) {
        documentationUnitDTO.setDocumentType(getDbDocType(documentUnit.coreData().documentType()));
      }

      documentationUnitDTO = saveKeywords(documentationUnitDTO, documentUnit);

      if (documentUnit.coreData().court() != null) {
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

      if (documentUnit.coreData().procedure() != null) {
        documentationUnitDTO.setProcedures(getDbProcedures(documentUnit, documentationUnitDTO));
      }
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
    if (documentType == null) {
      return null;
    }
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

  private List<ProcedureDTO> getDbProcedures(
      DocumentUnit documentUnit, DocumentationUnitDTO documentationUnitDTO) {
    Stream<String> procedureLabels = Stream.of(documentUnit.coreData().procedure().label());
    if (documentUnit.coreData().previousProcedures() != null)
      procedureLabels =
          Stream.concat(procedureLabels, documentUnit.coreData().previousProcedures().stream());

    return procedureLabels
        .map(procedureLabel -> findOrCreateProcedureDTO(procedureLabel, documentationUnitDTO))
        .toList();
  }

  private ProcedureDTO findOrCreateProcedureDTO(
      String procedureLabel, DocumentationUnitDTO documentationUnitDTO) {
    return Optional.ofNullable(
            databaseProcedureRepository.findByLabelAndDocumentationOffice(
                procedureLabel, documentationUnitDTO.getDocumentationOffice()))
        .orElse(
            ProcedureDTO.builder()
                .label(procedureLabel)
                .documentationOffice(documentationUnitDTO.getDocumentationOffice())
                .documentationUnits(List.of(documentationUnitDTO))
                .build());
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
  public DocumentUnit removeFile(UUID documentUnitId) {
    var docUnitDto = repository.findById(documentUnitId).orElseThrow();

    docUnitDto.setOriginalFileDocument(null);

    docUnitDto = repository.save(docUnitDto);

    return DocumentationUnitTransformer.transformToDomain(docUnitDto);
  }

  @Override
  public Mono<Void> delete(DocumentUnit documentUnit) {
    repository.deleteById(documentUnit.uuid());
    return Mono.empty();
  }

  @Override
  public Page<RelatedDocumentationUnit> searchByRelatedDocumentationUnit(
      RelatedDocumentationUnit relatedDocumentationUnit, Pageable pageable) {
    throw new UnsupportedOperationException("not implemented yet");
  }

  public Page<DocumentationUnitSearchEntry> searchByDocumentUnitSearchInput(
      Pageable pageable,
      DocumentationOffice documentationOffice,
      DocumentUnitSearchInput searchInput) {
    if (log.isDebugEnabled()) {
      log.debug("Find by overview search: {}, {}", documentationOffice, searchInput);
    }

    DocumentationOfficeDTO documentationOfficeDTO =
        documentationOfficeRepository.findByAbbreviation(documentationOffice.abbreviation());

    Page<DocumentationUnitMetadataDTO> documentUnitDTOList =
        repository.searchByDocumentUnitSearchInput(
            documentationOfficeDTO.getId(),
            searchInput.documentNumberOrFileNumber(),
            searchInput.courtType(),
            searchInput.courtLocation(),
            searchInput.decisionDate(),
            //            searchInput.status() == null ? null :
            // searchInput.status().publicationStatus(),
            searchInput.myDocOfficeOnly(),
            pageable);

    List<DocumentationUnitSearchEntry> list =
        documentUnitDTOList.stream()
            .map(
                metadataDTO ->
                    // TODO move to transformer
                    DocumentationUnitSearchEntry.builder()
                        .uuid(metadataDTO.getId())
                        .documentNumber(metadataDTO.getDocumentNumber())
                        .decisionDate(metadataDTO.getDecisionDate())
                        .fileName(
                            metadataDTO.getOriginalFileDocument() == null
                                ? null
                                : metadataDTO.getOriginalFileDocument().getFilename())
                        .documentType(
                            metadataDTO.getDocumentType() == null
                                ? null
                                : DocumentTypeTransformer.transformDTO(
                                        metadataDTO.getDocumentType())
                                    .jurisShortcut())
                        .courtLocation(
                            metadataDTO.getCourt() == null
                                ? null
                                : metadataDTO.getCourt().getLocation())
                        .courtType(
                            metadataDTO.getCourt() == null
                                ? null
                                : metadataDTO.getCourt().getType())
                        .fileNumber(
                            metadataDTO.getFileNumbers() == null
                                    || metadataDTO.getFileNumbers().isEmpty()
                                ? null
                                : metadataDTO.getFileNumbers().get(0).getValue())
                        .status(
                            metadataDTO.getStatus() == null || metadataDTO.getStatus().isEmpty()
                                ? null
                                : DocumentUnitStatus.builder()
                                    // TODO is the first status the most recent?
                                    .publicationStatus(
                                        metadataDTO.getStatus().get(0) == null
                                            ? null
                                            : metadataDTO.getStatus().get(0).getPublicationStatus())
                                    .withError(
                                        metadataDTO.getStatus().get(0) == null
                                            || metadataDTO.getStatus().get(0).isWithError())
                                    .build())
                        .build())
            .toList();

    // TODO fix count
    return new PageImpl<>(list, pageable, 100);
  }

  @Override
  // TODO how to delete orphaned without dataSource
  public Mono<Void> deleteIfOrphanedLinkedDocumentationUnit(UUID documentUnitUuid) {
    if (log.isDebugEnabled()) {
      log.debug("delete if orphaned linked documentation unit: {}", documentUnitUuid);
    }

    throw new UnsupportedOperationException("not implemented yet");
  }
  //  @NotNull
  //  private static Predicate[] getPredicates(
  //      DocumentUnitSearchInput searchInput,
  //      DocumentationOfficeDTO documentationOfficeDTO,
  //      CriteriaBuilder builder,
  //      Root<DocumentationUnitSearchEntryDTO> root) {
  //    List<Predicate> restrictions = new ArrayList<>();
  //
  //    if (searchInput.documentNumberOrFileNumber() != null) {
  //      String pattern = "%" + searchInput.documentNumberOrFileNumber().toLowerCase() + "%";
  //      Predicate documentNumberLike =
  //          builder.like(builder.lower(root.get("documentNumber")), pattern);
  //      Predicate firstFileNumberLike =
  //          builder.like(builder.lower(root.get("firstFileNumber")), pattern);
  //      restrictions.add(builder.or(documentNumberLike, firstFileNumberLike));
  //    }
  //
  //    if (searchInput.courtLocation() != null) {
  //      restrictions.add(builder.equal(root.get("courtLocation"), searchInput.courtLocation()));
  //    }
  //
  //    if (searchInput.courtType() != null) {
  //      restrictions.add(builder.equal(root.get("courtType"), searchInput.courtType()));
  //    }
  //
  //    if (searchInput.decisionDate() != null) {
  //      if (searchInput.decisionDateEnd() != null) {
  //        restrictions.add(
  //            builder.between(
  //                root.get("decisionDate"),
  //                searchInput.decisionDate(),
  //                searchInput.decisionDateEnd()));
  //      } else {
  //        restrictions.add(builder.equal(root.get("decisionDate"), searchInput.decisionDate()));
  //      }
  //    }
  //
  //    Predicate myDocOffice =
  //        builder.equal(root.get("documentationOfficeId"), documentationOfficeDTO.getId());
  //
  //    if (searchInput.status() != null && searchInput.status().withError()) {
  //      Predicate statusWithError = builder.equal(root.get("withError"), true);
  //      restrictions.add(builder.and(myDocOffice, statusWithError));
  //    }
  //
  //    if (searchInput.status() != null && searchInput.status().publicationStatus() != null) {
  //      Predicate status =
  //          builder.equal(root.get("publicationStatus"),
  // searchInput.status().publicationStatus());
  //      if (searchInput.status().publicationStatus() == PublicationStatus.PUBLISHED) {
  //        status =
  //            root.get("publicationStatus")
  //                .in(PublicationStatus.PUBLISHED, PublicationStatus.JURIS_PUBLISHED);
  //      }
  //      if (searchInput.myDocOfficeOnly()
  //          || searchInput.status().publicationStatus() == PublicationStatus.UNPUBLISHED) {
  //        restrictions.add(builder.and(myDocOffice, status));
  //      } else {
  //        restrictions.add(status);
  //      }
  //    } else {
  //      Predicate status =
  //          root.get("publicationStatus")
  //              .in(
  //                  PublicationStatus.PUBLISHED,
  //                  PublicationStatus.PUBLISHING,
  //                  PublicationStatus.JURIS_PUBLISHED);
  //      if (searchInput.myDocOfficeOnly()) {
  //        restrictions.add(myDocOffice);
  //      } else {
  //        restrictions.add(builder.or(myDocOffice, status));
  //      }
  //    }
  //
  //    return restrictions.toArray(new Predicate[0]);
  //  }
}
