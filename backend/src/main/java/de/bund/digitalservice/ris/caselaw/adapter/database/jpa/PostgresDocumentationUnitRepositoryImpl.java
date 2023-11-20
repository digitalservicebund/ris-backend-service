package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationUnitSearchResultTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationUnitTransformer;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitSearchInput;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitSearchResult;
import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationType;
import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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
  private final DatabaseRelatedDocumentationRepository relatedDocumentationRepository;
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
      DatabaseRelatedDocumentationRepository relatedDocumentationRepository,
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
    this.relatedDocumentationRepository = relatedDocumentationRepository;
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

    //      if (documentUnit.coreData().documentType() != null) {
    //
    // documentationUnitDTO.setDocumentType(getDbDocType(documentUnit.coreData().documentType()));
    //      }

    //      if (documentUnit.coreData().procedure() != null) {
    //        documentationUnitDTO.setProcedures(getDbProcedures(documentUnit,
    // documentationUnitDTO));
    //      }

    documentationUnitDTO = saveKeywords(documentationUnitDTO, documentUnit);

    if (documentUnit.coreData() != null) {
      documentationUnitDTO.getRegions().clear();
      if (documentUnit.coreData().court() != null && documentUnit.coreData().court().id() != null) {
        Optional<CourtDTO> court =
            databaseCourtRepository.findById(documentUnit.coreData().court().id());
        if (court.isPresent() && court.get().getRegion() != null) {
          documentationUnitDTO.getRegions().add(court.get().getRegion());
        }
      }
    }
    // ---

    // Transform non-database-related properties
    documentationUnitDTO =
        DocumentationUnitTransformer.transformToDTO(documentationUnitDTO, documentUnit);
    documentationUnitDTO = repository.save(documentationUnitDTO);

    return Mono.just(findByUuid(documentationUnitDTO.getId()));
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

  //  private DocumentTypeDTO getDbDocType(DocumentType documentType) {
  //    if (documentType == null) {
  //      return null;
  //    }
  //    // TODO cache category at application start
  //    DocumentTypeDTO docTypeDTO =
  //        databaseDocumentTypeRepository.findFirstByAbbreviationAndCategory(
  //            documentType.jurisShortcut(),
  // databaseDocumentCategoryRepository.findFirstByLabel("R"));
  //
  //    if (docTypeDTO == null) {
  //      throw new DocumentationUnitException(
  //          "no document type for the shortcut '" + documentType.jurisShortcut() + "' found.");
  //    }
  //    return docTypeDTO;
  //  }
  //
  //  private List<ProcedureDTO> getDbProcedures(
  //      DocumentUnit documentUnit, DocumentationUnitDTO documentationUnitDTO) {
  //    Stream<String> procedureLabels = Stream.of(documentUnit.coreData().procedure().label());
  //    if (documentUnit.coreData().previousProcedures() != null)
  //      procedureLabels =
  //          Stream.concat(procedureLabels, documentUnit.coreData().previousProcedures().stream());
  //
  //    return procedureLabels
  //        .map(procedureLabel -> findOrCreateProcedureDTO(procedureLabel, documentationUnitDTO))
  //        .toList();
  //  }
  //
  //  private ProcedureDTO findOrCreateProcedureDTO(
  //      String procedureLabel, DocumentationUnitDTO documentationUnitDTO) {
  //    return Optional.ofNullable(
  //            databaseProcedureRepository.findByLabelAndDocumentationOffice(
  //                procedureLabel, documentationUnitDTO.getDocumentationOffice()))
  //        .orElse(
  //            ProcedureDTO.builder()
  //                .label(procedureLabel)
  //                .documentationOffice(documentationUnitDTO.getDocumentationOffice())
  //                .documentationUnits(List.of(documentationUnitDTO))
  //                .build());
  //  }

  @Override
  public Mono<DocumentUnit> attachFile(
      UUID documentUnitUuid, String fileUuid, String type, String fileName) {
    var docUnitDto = repository.findById(documentUnitUuid).orElseThrow();
    docUnitDto.setOriginalFileDocument(
        OriginalFileDocumentDTO.builder()
            .id(UUID.randomUUID())
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
  public void delete(DocumentUnit documentUnit) {
    repository.deleteById(documentUnit.uuid());
  }

  @Override
  public Page<DocumentationUnitSearchResult> searchByRelatedDocumentationUnit(
      RelatedDocumentationUnit relatedDocumentationUnit, Pageable pageable) {
    String courtType =
        Optional.ofNullable(relatedDocumentationUnit.getCourt()).map(Court::type).orElse(null);
    String courtLocation =
        Optional.ofNullable(relatedDocumentationUnit.getCourt()).map(Court::location).orElse(null);

    Page<DocumentationUnitSearchResultDTO> documentationUnitSearchResultDTOPage =
        repository.searchByDocumentUnitSearchInput(
            courtType,
            courtLocation,
            relatedDocumentationUnit.getFileNumber(),
            relatedDocumentationUnit.getDecisionDate(),
            pageable);

    List<DocumentationUnitSearchResult> list =
        documentationUnitSearchResultDTOPage.stream()
            .map(DocumentationUnitSearchResultTransformer::transformToDomain)
            .toList();

    return new PageImpl<>(list, pageable, documentationUnitSearchResultDTOPage.getTotalElements());
  }

  public Page<DocumentationUnitSearchResult> searchByDocumentationUnitSearchInput(
      Pageable pageable,
      DocumentationOffice documentationOffice,
      DocumentationUnitSearchInput searchInput) {
    if (log.isDebugEnabled()) {
      log.debug("Find by overview search: {}, {}", documentationOffice, searchInput);
    }

    DocumentationOfficeDTO documentationOfficeDTO =
        documentationOfficeRepository.findByAbbreviation(documentationOffice.abbreviation());

    String publicationStatus =
        Optional.ofNullable(searchInput.status())
            .flatMap(
                status ->
                    Optional.ofNullable(status.publicationStatus())
                        .map(
                            notNullPublicationStatus ->
                                String.valueOf(notNullPublicationStatus.ordinal())))
            .orElse(null);

    Boolean withError =
        Optional.ofNullable(searchInput.status()).map(Status::withError).orElse(false);

    Page<DocumentationUnitSearchResultDTO> documentationUnitSearchResultDTOPage =
        repository.searchByDocumentUnitSearchInput(
            documentationOfficeDTO.getId(),
            searchInput.documentNumberOrFileNumber(),
            searchInput.courtType(),
            searchInput.courtLocation(),
            searchInput.decisionDate(),
            searchInput.decisionDateEnd(),
            publicationStatus,
            withError,
            searchInput.myDocOfficeOnly(),
            pageable);

    List<DocumentationUnitSearchResult> list =
        documentationUnitSearchResultDTOPage.stream()
            .map(DocumentationUnitSearchResultTransformer::transformToDomain)
            .toList();

    return new PageImpl<>(list, pageable, documentationUnitSearchResultDTOPage.getTotalElements());
  }

  @Override
  public Map<RelatedDocumentationType, Long> getAllDocumentationUnitWhichLink(
      UUID documentationUnitId) {
    // TODO: activate after referenced documentation unit id in related documentation
    return Collections.emptyMap();
    //    return
    // relatedDocumentationRepository.findAllByReferencedDocumentUnitId(documentationUnitId).stream()
    //            .collect(Collectors.groupingBy(
    //              RelatedDocumentationDTO::getType,
    //              Collectors.counting()
    //            )
    //    );
  }
}
