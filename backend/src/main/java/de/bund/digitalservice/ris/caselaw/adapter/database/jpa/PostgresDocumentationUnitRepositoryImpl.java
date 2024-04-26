package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentTypeTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationUnitListItemTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationUnitTransformer;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitException;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitListItem;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitSearchInput;
import de.bund.digitalservice.ris.caselaw.domain.Procedure;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationType;
import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

/** Implementation of the DocumentUnitRepository for the Postgres database */
@Repository
@Slf4j
@Primary
public class PostgresDocumentationUnitRepositoryImpl implements DocumentUnitRepository {
  private final DatabaseDocumentationUnitRepository repository;
  private final DatabaseCourtRepository databaseCourtRepository;
  private final DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  private final DatabaseKeywordRepository keywordRepository;
  private final DatabaseFieldOfLawRepository fieldOfLawRepository;
  private final DatabaseProcedureRepository procedureRepository;
  private final DatabaseRelatedDocumentationRepository relatedDocumentationRepository;

  public PostgresDocumentationUnitRepositoryImpl(
      DatabaseDocumentationUnitRepository repository,
      DatabaseCourtRepository databaseCourtRepository,
      DatabaseDocumentationOfficeRepository documentationOfficeRepository,
      DatabaseRelatedDocumentationRepository relatedDocumentationRepository,
      DatabaseKeywordRepository keywordRepository,
      DatabaseProcedureRepository procedureRepository,
      DatabaseFieldOfLawRepository fieldOfLawRepository) {

    this.repository = repository;
    this.databaseCourtRepository = databaseCourtRepository;
    this.documentationOfficeRepository = documentationOfficeRepository;
    this.keywordRepository = keywordRepository;
    this.relatedDocumentationRepository = relatedDocumentationRepository;
    this.fieldOfLawRepository = fieldOfLawRepository;
    this.procedureRepository = procedureRepository;
  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public Optional<DocumentUnit> findByDocumentNumber(String documentNumber) {
    try {
      var documentUnit =
          repository
              .findByDocumentNumber(documentNumber)
              .orElseThrow(() -> new DocumentationUnitNotExistsException(documentNumber));
      return Optional.of(DocumentationUnitTransformer.transformToDomain(documentUnit));
    } catch (Exception ex) {
      log.error("Error to get a documentation unit by document number.", ex);
      return Optional.empty();
    }
  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public Optional<DocumentUnit> findByUuid(UUID uuid) {
    try {
      var documentUnit =
          repository
              .findById(uuid)
              .orElseThrow(() -> new DocumentationUnitNotExistsException(uuid));
      return Optional.of(DocumentationUnitTransformer.transformToDomain(documentUnit));
    } catch (Exception ex) {
      log.error("Error to get a documentation unit by uuid.", ex);
      return Optional.empty();
    }
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
                            .legalEffect(LegalEffectDTO.KEINE_ANGABE)
                            .build())))
        .map(DocumentationUnitTransformer::transformToDomain);
  }

  @Transactional(transactionManager = "jpaTransactionManager")
  @Override
  public void save(DocumentUnit documentUnit, boolean featureActive) {

    DocumentationUnitDTO documentationUnitDTO =
        repository.findById(documentUnit.uuid()).orElse(null);
    if (documentationUnitDTO == null) {
      log.info("Can't save non-existing docUnit with id = " + documentUnit.uuid());
      return;
    }

    // ---
    // Doing database-related (pre) transformation

    if (documentUnit.coreData() != null) {
      documentationUnitDTO.getRegions().clear();
      if (documentUnit.coreData().court() != null && documentUnit.coreData().court().id() != null) {
        Optional<CourtDTO> court =
            databaseCourtRepository.findById(documentUnit.coreData().court().id());
        if (court.isPresent() && court.get().getRegion() != null) {
          documentationUnitDTO.getRegions().add(court.get().getRegion());
        }
        // delete leading decision norm references if court is not BGH
        if (court.isPresent() && !court.get().getType().equals("BGH")) {
          documentUnit.coreData().leadingDecisionNormReferences().clear();
        }
      }
    }
    // ---

    // Transform non-database-related properties
    documentationUnitDTO =
        DocumentationUnitTransformer.transformToDTO(
            documentationUnitDTO, documentUnit, featureActive);
    repository.save(documentationUnitDTO);
  }

  @Override
  public void saveKeywords(DocumentUnit documentUnit) {
    if (documentUnit == null || documentUnit.contentRelatedIndexing() == null) {
      return;
    }

    repository
        .findById(documentUnit.uuid())
        .ifPresent(
            documentationUnitDTO -> {
              ContentRelatedIndexing contentRelatedIndexing = documentUnit.contentRelatedIndexing();

              if (contentRelatedIndexing.keywords() != null) {
                List<DocumentationUnitKeywordDTO> documentationUnitKeywordDTOs = new ArrayList<>();

                List<String> keywords = contentRelatedIndexing.keywords();
                for (int i = 0; i < keywords.size(); i++) {
                  String value = keywords.get(i);

                  KeywordDTO keywordDTO =
                      keywordRepository
                          .findByValue(value)
                          .orElseGet(
                              () ->
                                  keywordRepository.save(
                                      KeywordDTO.builder().value(value).build()));

                  DocumentationUnitKeywordDTO documentationUnitKeywordDTO =
                      DocumentationUnitKeywordDTO.builder()
                          .primaryKey(
                              new DocumentationUnitKeywordId(
                                  documentationUnitDTO.getId(), keywordDTO.getId()))
                          .documentationUnit(documentationUnitDTO)
                          .keyword(keywordDTO)
                          .rank(i + 1)
                          .build();

                  documentationUnitKeywordDTOs.add(documentationUnitKeywordDTO);
                }

                documentationUnitDTO.setDocumentationUnitKeywordDTOs(documentationUnitKeywordDTOs);

                repository.save(documentationUnitDTO);
              }
            });
  }

  @Override
  public void saveFieldsOfLaw(DocumentUnit documentUnit) {
    if (documentUnit == null || documentUnit.contentRelatedIndexing() == null) {
      return;
    }

    repository
        .findById(documentUnit.uuid())
        .ifPresent(
            documentationUnitDTO -> {
              ContentRelatedIndexing contentRelatedIndexing = documentUnit.contentRelatedIndexing();

              if (contentRelatedIndexing.fieldsOfLaw() != null) {
                List<DocumentationUnitFieldOfLawDTO> documentationUnitFieldOfLawDTOs =
                    new ArrayList<>();

                List<FieldOfLaw> fieldsOfLaw = contentRelatedIndexing.fieldsOfLaw();
                for (int i = 0; i < fieldsOfLaw.size(); i++) {
                  FieldOfLaw fieldOfLaw = fieldsOfLaw.get(i);

                  Optional<FieldOfLawDTO> fieldOfLawDTOOptional =
                      fieldOfLawRepository.findById(fieldOfLaw.id());

                  if (fieldOfLawDTOOptional.isPresent()) {
                    DocumentationUnitFieldOfLawDTO documentationUnitFieldOfLawDTO =
                        DocumentationUnitFieldOfLawDTO.builder()
                            .primaryKey(
                                new DocumentationUnitFieldOfLawId(
                                    documentationUnitDTO.getId(),
                                    fieldOfLawDTOOptional.get().getId()))
                            .rank(i + 1)
                            .build();
                    documentationUnitFieldOfLawDTO.setDocumentationUnit(documentationUnitDTO);
                    documentationUnitFieldOfLawDTO.setFieldOfLaw(fieldOfLawDTOOptional.get());

                    documentationUnitFieldOfLawDTOs.add(documentationUnitFieldOfLawDTO);
                  } else {
                    throw new DocumentationUnitException(
                        "field of law with id: '" + fieldOfLaw.id() + "' not found.");
                  }
                }

                documentationUnitDTO.setDocumentationUnitFieldsOfLaw(
                    documentationUnitFieldOfLawDTOs);

                repository.save(documentationUnitDTO);
              }
            });
  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public void saveProcedures(DocumentUnit documentUnit) {
    if (documentUnit == null
        || documentUnit.coreData() == null
        || documentUnit.coreData().procedure() == null) {
      return;
    }

    repository
        .findById(documentUnit.uuid())
        .ifPresent(
            documentationUnitDTO -> {
              Procedure procedure = documentUnit.coreData().procedure();

              List<DocumentationUnitProcedureDTO> documentationUnitProcedureDTOs =
                  new ArrayList<>();

              ProcedureDTO procedureDTO = null;
              if (procedure.id() == null) {
                Optional<ProcedureDTO> existingProcedure =
                    procedureRepository.findAllByLabelAndDocumentationOffice(
                        procedure.label().trim(), documentationUnitDTO.getDocumentationOffice());

                if (existingProcedure.isPresent()) {
                  procedureDTO = existingProcedure.get();
                } else {
                  procedureDTO =
                      ProcedureDTO.builder()
                          .label(procedure.label().trim())
                          .createdAt(Instant.now())
                          .documentationOffice(documentationUnitDTO.getDocumentationOffice())
                          .build();

                  procedureDTO = procedureRepository.save(procedureDTO);
                }
              } else {
                Optional<ProcedureDTO> optionalProcedureDTO =
                    procedureRepository.findById(procedure.id());
                if (optionalProcedureDTO.isPresent()) {
                  procedureDTO = optionalProcedureDTO.get();
                }
              }
              boolean sameAsLast =
                  !documentationUnitDTO.getProcedures().isEmpty()
                      && documentationUnitDTO
                          .getProcedures()
                          .get(documentationUnitDTO.getProcedures().size() - 1)
                          .getProcedure()
                          .equals(procedureDTO);

              documentationUnitDTO
                  .getProcedures()
                  .forEach(
                      documentationUnitProcedureDTO -> {
                        DocumentationUnitProcedureDTO newLink =
                            DocumentationUnitProcedureDTO.builder()
                                .primaryKey(
                                    new DocumentationUnitProcedureId(
                                        documentationUnitDTO.getId(),
                                        documentationUnitProcedureDTO.getProcedure().getId()))
                                .documentationUnit(documentationUnitDTO)
                                .procedure(documentationUnitProcedureDTO.getProcedure())
                                .build();
                        documentationUnitProcedureDTOs.add(newLink);
                      });

              if (procedureDTO != null && !sameAsLast) {
                DocumentationUnitProcedureDTO documentationUnitProcedureDTO =
                    DocumentationUnitProcedureDTO.builder()
                        .primaryKey(
                            new DocumentationUnitProcedureId(
                                documentationUnitDTO.getId(), procedureDTO.getId()))
                        .documentationUnit(documentationUnitDTO)
                        .procedure(procedureDTO)
                        .build();
                documentationUnitProcedureDTOs.add(documentationUnitProcedureDTO);
              }

              updateProcedureRank(documentationUnitProcedureDTOs);

              documentationUnitDTO.getProcedures().clear();
              documentationUnitDTO.getProcedures().addAll(documentationUnitProcedureDTOs);

              repository.save(documentationUnitDTO);
            });
  }

  private void updateProcedureRank(
      List<DocumentationUnitProcedureDTO> documentationUnitProcedureDTOs) {
    for (int i = 0; i < documentationUnitProcedureDTOs.size(); i++) {
      DocumentationUnitProcedureDTO documentationUnitProcedureDTO =
          documentationUnitProcedureDTOs.get(i);
      documentationUnitProcedureDTO.setRank(i + 1);
    }
  }

  @Override
  public void delete(DocumentUnit documentUnit) {
    repository.deleteById(documentUnit.uuid());
  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public Slice<RelatedDocumentationUnit> searchLinkableDocumentationUnits(
      RelatedDocumentationUnit relatedDocumentationUnit,
      DocumentationOffice documentationOffice,
      String documentNumberToExclude,
      Pageable pageable) {
    String courtType =
        Optional.ofNullable(relatedDocumentationUnit.getCourt()).map(Court::type).orElse(null);
    String courtLocation =
        Optional.ofNullable(relatedDocumentationUnit.getCourt()).map(Court::location).orElse(null);

    DocumentationOfficeDTO documentationOfficeDTO =
        documentationOfficeRepository.findByAbbreviation(documentationOffice.abbreviation());

    Slice<DocumentationUnitListItemDTO> allResults =
        getDocumentationUnitSearchResultDTOS(
            pageable,
            courtType,
            courtLocation,
            null,
            documentNumberToExclude,
            relatedDocumentationUnit.getFileNumber(),
            relatedDocumentationUnit.getDecisionDate(),
            null,
            null,
            false,
            false,
            relatedDocumentationUnit.getDocumentType(),
            documentationOfficeDTO);

    return allResults.map(DocumentationUnitListItemTransformer::transformToRelatedDocumentation);
  }

  @NotNull
  private Slice<DocumentationUnitListItemDTO> getDocumentationUnitSearchResultDTOS(
      Pageable pageable,
      String courtType,
      String courtLocation,
      String documentNumber,
      String documentNumberToExclude,
      String fileNumber,
      LocalDate decisionDate,
      LocalDate decisionDateEnd,
      PublicationStatus status,
      Boolean withError,
      Boolean myDocOfficeOnly,
      DocumentType documentType,
      DocumentationOfficeDTO documentationOfficeDTO) {
    if ((fileNumber == null || fileNumber.trim().isEmpty())) {
      return repository.searchByDocumentUnitSearchInput(
          documentationOfficeDTO.getId(),
          documentNumber,
          documentNumberToExclude,
          courtType,
          courtLocation,
          decisionDate,
          decisionDateEnd,
          status,
          withError,
          myDocOfficeOnly,
          DocumentTypeTransformer.transformToDTO(documentType),
          pageable);
    }

    // The highest possible number of results - For page 0: 30, for page 1: 60, for page 2: 90, etc.
    int maxResultsUpToCurrentPage = (pageable.getPageNumber() + 1) * pageable.getPageSize();

    // We need to start with index 0 because we collect 3 results sets, each of the desired size of
    // the page. Then we sort the full ist and cut it to the page size (possibly leaving 2x page
    // size results behind). If we don't always start with index 0, we might miss results.
    // This approach could even be better if we replace the next/previous with a "load more" button
    Pageable fixedPageRequest = PageRequest.of(0, maxResultsUpToCurrentPage);

    Slice<DocumentationUnitListItemDTO> fileNumberResults = new SliceImpl<>(List.of());
    Slice<DocumentationUnitListItemDTO> deviatingFileNumberResults = new SliceImpl<>(List.of());

    if (!fileNumber.trim().isEmpty()) {
      fileNumberResults =
          repository.searchByDocumentUnitSearchInputFileNumber(
              documentationOfficeDTO.getId(),
              documentNumber,
              documentNumberToExclude,
              fileNumber.trim(),
              courtType,
              courtLocation,
              decisionDate,
              decisionDateEnd,
              status,
              withError,
              myDocOfficeOnly,
              DocumentTypeTransformer.transformToDTO(documentType),
              fixedPageRequest);

      deviatingFileNumberResults =
          repository.searchByDocumentUnitSearchInputDeviatingFileNumber(
              documentationOfficeDTO.getId(),
              documentNumber,
              documentNumberToExclude,
              fileNumber.trim(),
              courtType,
              courtLocation,
              decisionDate,
              decisionDateEnd,
              status,
              withError,
              myDocOfficeOnly,
              DocumentTypeTransformer.transformToDTO(documentType),
              fixedPageRequest);
    }

    Set<DocumentationUnitListItemDTO> allResults = new HashSet<>();
    allResults.addAll(fileNumberResults.getContent());
    allResults.addAll(deviatingFileNumberResults.getContent());

    // We can provide entries for a next page if ...
    // A) we already have collected more results than fit on the current page, or
    // B) at least one of the queries has more results
    boolean hasNext =
        allResults.size() >= maxResultsUpToCurrentPage
            || fileNumberResults.hasNext()
            || deviatingFileNumberResults.hasNext();

    return new SliceImpl<>(
        allResults.stream()
            .sorted(
                (o1, o2) -> {
                  if (o1.getDocumentNumber() != null && o2.getDocumentNumber() != null) {
                    return o1.getDocumentNumber().compareTo(o2.getDocumentNumber());
                  }
                  return 0;
                })
            .toList()
            .subList(
                pageable.getPageNumber() * pageable.getPageSize(),
                Math.min(allResults.size(), maxResultsUpToCurrentPage)),
        pageable,
        hasNext);
  }

  @Transactional(transactionManager = "jpaTransactionManager")
  public Slice<DocumentationUnitListItem> searchByDocumentationUnitSearchInput(
      Pageable pageable,
      DocumentationOffice documentationOffice,
      @Param("searchInput") DocumentationUnitSearchInput searchInput) {
    log.debug("Find by overview search: {}, {}", documentationOffice.abbreviation(), searchInput);

    DocumentationOfficeDTO documentationOfficeDTO =
        documentationOfficeRepository.findByAbbreviation(documentationOffice.abbreviation());

    Boolean withError =
        Optional.ofNullable(searchInput.status()).map(Status::withError).orElse(false);

    Slice<DocumentationUnitListItemDTO> allResults =
        getDocumentationUnitSearchResultDTOS(
            pageable,
            searchInput.courtType(),
            searchInput.courtLocation(),
            searchInput.documentNumber(),
            null,
            searchInput.fileNumber(),
            searchInput.decisionDate(),
            searchInput.decisionDateEnd(),
            searchInput.status() != null ? searchInput.status().publicationStatus() : null,
            withError,
            searchInput.myDocOfficeOnly(),
            null,
            documentationOfficeDTO);

    return allResults.map(DocumentationUnitListItemTransformer::transformToDomain);
  }

  @Override
  public Map<RelatedDocumentationType, Long> getAllDocumentationUnitWhichLink(
      UUID documentationUnitId) {
    return relatedDocumentationRepository
        .findAllByReferencedDocumentationUnitId(documentationUnitId)
        .stream()
        .collect(Collectors.groupingBy(RelatedDocumentationDTO::getType, Collectors.counting()));
  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public void updateECLI(UUID uuid, String ecli) {
    Optional<DocumentationUnitDTO> documentationUnitDTOOptional = repository.findById(uuid);
    if (documentationUnitDTOOptional.isPresent()) {
      DocumentationUnitDTO dto = documentationUnitDTOOptional.get();
      if (dto.getEcli() == null) {
        dto.setEcli(ecli);
        repository.save(dto);
      }
    }
  }
}
