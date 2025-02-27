package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.DecisionTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentTypeTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationOfficeTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationUnitListItemTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.PendingProceedingTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ReferenceTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.StatusTransformer;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.Documentable;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitListItem;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitSearchInput;
import de.bund.digitalservice.ris.caselaw.domain.Procedure;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.Reference;
import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationType;
import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.SourceValue;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.StringUtils;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitException;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import org.jetbrains.annotations.Nullable;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.repository.query.Param;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Implementation of the DocumentationUnitRepository for the Postgres database */
@Repository
@Slf4j
@Primary
public class PostgresDocumentationUnitRepositoryImpl implements DocumentationUnitRepository {
  private final DatabaseDocumentationUnitRepository repository;
  private final DatabaseCourtRepository databaseCourtRepository;
  private final DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  private final DatabaseKeywordRepository keywordRepository;
  private final DatabaseFieldOfLawRepository fieldOfLawRepository;
  private final DatabaseProcedureRepository procedureRepository;
  private final DatabaseRelatedDocumentationRepository relatedDocumentationRepository;
  private final UserService userService;
  private final EntityManager entityManager;

  public PostgresDocumentationUnitRepositoryImpl(
      DatabaseDocumentationUnitRepository repository,
      DatabaseCourtRepository databaseCourtRepository,
      DatabaseDocumentationOfficeRepository documentationOfficeRepository,
      DatabaseRelatedDocumentationRepository relatedDocumentationRepository,
      DatabaseKeywordRepository keywordRepository,
      DatabaseProcedureRepository procedureRepository,
      DatabaseFieldOfLawRepository fieldOfLawRepository,
      UserService userService,
      EntityManager entityManager) {

    this.repository = repository;
    this.databaseCourtRepository = databaseCourtRepository;
    this.documentationOfficeRepository = documentationOfficeRepository;
    this.keywordRepository = keywordRepository;
    this.relatedDocumentationRepository = relatedDocumentationRepository;
    this.fieldOfLawRepository = fieldOfLawRepository;
    this.procedureRepository = procedureRepository;
    this.userService = userService;
    this.entityManager = entityManager;
  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public Documentable findByDocumentNumber(String documentNumber)
      throws DocumentationUnitNotExistsException {
    var documentationUnit =
        repository
            .findByDocumentNumber(documentNumber)
            .orElseThrow(() -> new DocumentationUnitNotExistsException(documentNumber));
    return getDocumentationUnit(documentationUnit);
  }

  @Nullable
  private static Documentable getDocumentationUnit(DocumentationUnitDTO documentationUnit) {
    if (documentationUnit instanceof DecisionDTO decisionDTO) {
      return DecisionTransformer.transformToDomain(decisionDTO);
    }
    if (documentationUnit instanceof PendingProceedingDTO pendingProceedingDTO) {
      return PendingProceedingTransformer.transformToDomain(pendingProceedingDTO);
    }
    return null;
  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public DocumentationUnitListItem findDocumentationUnitListItemByDocumentNumber(
      String documentNumber) throws DocumentationUnitNotExistsException {
    DocumentationUnitListItemDTO documentationUnitListItemDTO =
        repository
            .findDocumentationUnitListItemByDocumentNumber(documentNumber)
            .orElseThrow(() -> new DocumentationUnitNotExistsException(documentNumber));
    return DocumentationUnitListItemTransformer.transformToDomain(documentationUnitListItemDTO);
  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public Documentable findByUuid(UUID uuid) throws DocumentationUnitNotExistsException {
    var documentationUnit =
        repository.findById(uuid).orElseThrow(() -> new DocumentationUnitNotExistsException(uuid));
    return getDocumentationUnit(documentationUnit);
  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public DocumentationUnit createNewDocumentationUnit(
      DocumentationUnit docUnit, Status status, Reference createdFromReference) {

    var documentationUnitDTO =
        repository.save(
            DecisionTransformer.transformToDTO(
                DecisionDTO.builder()
                    .documentationOffice(
                        DocumentationOfficeTransformer.transformToDTO(
                            docUnit.coreData().documentationOffice()))
                    .creatingDocumentationOffice(
                        DocumentationOfficeTransformer.transformToDTO(
                            docUnit.coreData().creatingDocOffice()))
                    .build(),
                docUnit));

    List<SourceDTO> sources = new ArrayList<>();
    if (createdFromReference != null) {
      ReferenceDTO referenceDTO = ReferenceTransformer.transformToDTO(createdFromReference);
      referenceDTO.setDocumentationUnitRank(0);
      referenceDTO.setDocumentationUnit(documentationUnitDTO);

      // if created from reference, the source is always 'Z' (Zeitschrift)
      sources.add(SourceDTO.builder().rank(1).value(SourceValue.Z).reference(referenceDTO).build());
    }

    DecisionDTO.DecisionDTOBuilder<?, ?> builder =
        documentationUnitDTO.toBuilder()
            .source(sources)
            .status(
                StatusTransformer.transformToDTO(status).toBuilder()
                    .documentationUnit(documentationUnitDTO)
                    .createdAt(Instant.now())
                    .build());

    // saving a second time is necessary because status and reference need a reference to a
    // persisted documentation unit
    DecisionDTO savedDocUnit = repository.save(builder.build());

    return DecisionTransformer.transformToDomain(savedDocUnit);
  }

  @Transactional(transactionManager = "jpaTransactionManager")
  @Override
  public void save(Documentable documentable) {

    DocumentationUnitDTO documentationUnitDTO =
        repository.findById(documentable.uuid()).orElse(null);
    if (documentationUnitDTO == null) {
      log.info("Can't save non-existing docUnit with id = " + documentable.uuid());
      return;
    }

    // ---
    // Doing database-related (pre) transformation

    if (documentable.coreData() != null) {
      documentationUnitDTO.getRegions().clear();
      if (documentable.coreData().court() != null && documentable.coreData().court().id() != null) {
        Optional<CourtDTO> court =
            databaseCourtRepository.findById(documentable.coreData().court().id());
        if (court.isPresent() && court.get().getRegion() != null) {
          documentationUnitDTO.getRegions().add(court.get().getRegion());
        }
        // delete leading decision norm references if court is not BGH
        if (documentable instanceof DocumentationUnit documentationUnit
            && court.isPresent()
            && !court.get().getType().equals("BGH")) {
          documentable =
              documentationUnit.toBuilder()
                  .coreData(
                      documentationUnit.coreData().toBuilder()
                          .leadingDecisionNormReferences(List.of())
                          .build())
                  .build();
        }
      }
    }

    // Transform non-database-related properties
    if (documentationUnitDTO instanceof DecisionDTO decisionDTO) {
      documentationUnitDTO =
          DecisionTransformer.transformToDTO(decisionDTO, (DocumentationUnit) documentable);
      repository.save(documentationUnitDTO);
    }
    // TODO pending proceeding
  }

  @Override
  public void saveKeywords(Documentable documentationUnit) {
    if (documentationUnit == null || documentationUnit.contentRelatedIndexing() == null) {
      return;
    }

    repository
        .findById(documentationUnit.uuid())
        .ifPresent(
            documentationUnitDTO -> {
              ContentRelatedIndexing contentRelatedIndexing =
                  documentationUnit.contentRelatedIndexing();

              if (contentRelatedIndexing.keywords() != null) {
                List<DocumentationUnitKeywordDTO> documentationUnitKeywordDTOs = new ArrayList<>();

                List<String> keywords = contentRelatedIndexing.keywords();
                for (int i = 0; i < keywords.size(); i++) {
                  String value = StringUtils.normalizeSpace(keywords.get(i));

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
  public void saveFieldsOfLaw(Documentable documentationUnit) {
    if (documentationUnit == null || documentationUnit.contentRelatedIndexing() == null) {
      return;
    }

    repository
        .findById(documentationUnit.uuid())
        .ifPresent(
            documentationUnitDTO -> {
              ContentRelatedIndexing contentRelatedIndexing =
                  documentationUnit.contentRelatedIndexing();

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
  public void saveProcedures(Documentable documentationUnit) {
    if (documentationUnit == null
        || documentationUnit.coreData() == null
        || documentationUnit.coreData().procedure() == null
        || !(documentationUnit instanceof DocumentationUnit)) {
      return;
    }

    var documentationUnitDTOOptional = repository.findById(documentationUnit.uuid());
    if (documentationUnitDTOOptional.isEmpty()
        || documentationUnitDTOOptional.get() instanceof PendingProceedingDTO) {
      return; // Pending Proceedings don't have procedures
    }
    DecisionDTO decisionDTO = (DecisionDTO) documentationUnitDTOOptional.get();
    Procedure procedure = documentationUnit.coreData().procedure();

    ProcedureDTO procedureDTO =
        getOrCreateProcedure(decisionDTO.getDocumentationOffice(), procedure);

    boolean sameAsLast =
        decisionDTO.getProcedure() != null && decisionDTO.getProcedure().equals(procedureDTO);

    // add the previous procedure to the history
    if (procedureDTO != null && !sameAsLast) {
      decisionDTO.getProcedureHistory().add(procedureDTO);
    }
    // set new procedure
    decisionDTO.setProcedure(procedureDTO);

    repository.save(decisionDTO);
  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public void saveLastPublicationDateTime(UUID uuid) {
    if (uuid == null) {
      return;
    }

    var documentationUnitDTOOptional = repository.findById(uuid);
    if (documentationUnitDTOOptional.isEmpty()) {
      return;
    }
    var documentationUnitDTO = documentationUnitDTOOptional.get();
    documentationUnitDTO.setLastPublicationDateTime(LocalDateTime.now());
    repository.save(documentationUnitDTO);
  }

  private ProcedureDTO getOrCreateProcedure(
      DocumentationOfficeDTO documentationOfficeDTO, Procedure procedure) {
    if (procedure.id() == null) {
      Optional<ProcedureDTO> existingProcedure =
          procedureRepository.findAllByLabelAndDocumentationOffice(
              StringUtils.normalizeSpace(procedure.label()), documentationOfficeDTO);

      return existingProcedure.orElseGet(
          () ->
              procedureRepository.save(
                  ProcedureDTO.builder()
                      .label(StringUtils.normalizeSpace(procedure.label()))
                      .createdAt(Instant.now())
                      .documentationOffice(documentationOfficeDTO)
                      .build()));
    }
    return procedureRepository.findById(procedure.id()).orElse(null);
  }

  @Override
  public void delete(Documentable documentationUnit) {
    repository.deleteById(documentationUnit.uuid());
  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public Slice<RelatedDocumentationUnit> searchLinkableDocumentationUnits(
      RelatedDocumentationUnit relatedDocumentationUnit,
      DocumentationOffice documentationOffice,
      String documentNumberToExclude,
      Pageable pageable) {

    // CriteriaBuilder and CriteriaQuery setup
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<DocumentationUnitDTO> criteriaQuery =
        criteriaBuilder.createQuery(DocumentationUnitDTO.class);
    Root<DocumentationUnitDTO> root = criteriaQuery.from(DocumentationUnitDTO.class);

    // Conditions setup
    Predicate conditions = criteriaBuilder.conjunction(); // Start with an empty conjunction (AND)
    String courtType =
        Optional.ofNullable(relatedDocumentationUnit.getCourt()).map(Court::type).orElse(null);
    String courtLocation =
        Optional.ofNullable(relatedDocumentationUnit.getCourt()).map(Court::location).orElse(null);
    LocalDate decisionDate = relatedDocumentationUnit.getDecisionDate();
    String fileNumber = relatedDocumentationUnit.getFileNumber();
    DocumentType documentType = relatedDocumentationUnit.getDocumentType();
    DocumentationOfficeDTO documentationOfficeDTO =
        documentationOfficeRepository.findByAbbreviation(documentationOffice.abbreviation());

    // 1. Filter by document number
    if (documentNumberToExclude != null) {
      Predicate documentNumberPredicate =
          criteriaBuilder.notEqual(root.get("documentNumber"), documentNumberToExclude);
      conditions = criteriaBuilder.and(conditions, documentNumberPredicate);
    }

    // 2. Filter by court type
    if (courtType != null) {
      Predicate courtTypePredicate =
          criteriaBuilder.like(
              criteriaBuilder.upper(root.get("court").get("type")),
              "%" + courtType.toUpperCase() + "%");
      conditions = criteriaBuilder.and(conditions, courtTypePredicate);
    }

    // 3. Filter by court location
    if (courtLocation != null) {
      Predicate courtLocationPredicate =
          criteriaBuilder.like(
              criteriaBuilder.upper(root.get("court").get("location")),
              "%" + courtLocation.toUpperCase() + "%");
      conditions = criteriaBuilder.and(conditions, courtLocationPredicate);
    }

    // 4. Filter by decision date
    if (decisionDate != null) {
      Predicate decisionDatePredicate = criteriaBuilder.equal(root.get("date"), decisionDate);
      conditions = criteriaBuilder.and(conditions, decisionDatePredicate);
    }

    // 5. Filter by file number
    if (fileNumber != null) {
      Join<DocumentationUnitDTO, String> fileNumberJoin = root.join("fileNumbers", JoinType.LEFT);
      Predicate fileNumberPredicate =
          criteriaBuilder.like(
              criteriaBuilder.upper(fileNumberJoin.get("value")), fileNumber.toUpperCase() + "%");
      conditions = criteriaBuilder.and(conditions, fileNumberPredicate);
    }

    // 6. Filter by document type
    if (documentType != null) {
      Predicate documentTypePredicate =
          criteriaBuilder.equal(
              root.get("documentType"), DocumentTypeTransformer.transformToDTO(documentType));
      conditions = criteriaBuilder.and(conditions, documentTypePredicate);
    }

    // 7. Filter by publication status
    final String PUBLICATION_STATUS = "publicationStatus";
    final String STATUS = "status";
    Predicate documentationOfficeIdPredicate =
        criteriaBuilder.equal(
            root.get("documentationOffice").get("id"), documentationOfficeDTO.getId());

    Predicate publicationStatusPredicate =
        criteriaBuilder.or(
            criteriaBuilder.equal(
                root.get(STATUS).get(PUBLICATION_STATUS), PublicationStatus.PUBLISHED),
            criteriaBuilder.equal(
                root.get(STATUS).get(PUBLICATION_STATUS), PublicationStatus.PUBLISHING));

    Predicate externalHandoverPendingPredicate =
        criteriaBuilder.and(
            criteriaBuilder.equal(
                root.get(STATUS).get(PUBLICATION_STATUS),
                PublicationStatus.EXTERNAL_HANDOVER_PENDING),
            criteriaBuilder.equal(
                root.get("creatingDocumentationOffice").get("id"), documentationOfficeDTO.getId()));

    Predicate finalPredicate =
        criteriaBuilder.or(
            documentationOfficeIdPredicate,
            publicationStatusPredicate,
            externalHandoverPendingPredicate);

    conditions = criteriaBuilder.and(conditions, finalPredicate);

    // Apply conditions to query
    criteriaQuery.where(conditions);

    // Apply pagination
    TypedQuery<DocumentationUnitDTO> query = entityManager.createQuery(criteriaQuery);
    query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
    query.setMaxResults(pageable.getPageSize());

    // Get results and create Slice
    List<DocumentationUnitDTO> resultList = query.getResultList();
    boolean hasNext = resultList.size() == pageable.getPageSize();

    SliceImpl<DocumentationUnitDTO> allResults = new SliceImpl<>(resultList, pageable, hasNext);
    return allResults.map(DocumentationUnitListItemTransformer::transformToRelatedDocumentation);
  }

  @NotNull
  @SuppressWarnings("java:S107")
  private Slice<DocumentationUnitListItemDTO> getDocumentationUnitSearchResultDTOS(
      Pageable pageable,
      String courtType,
      String courtLocation,
      String documentNumber,
      String fileNumber,
      LocalDate decisionDate,
      LocalDate decisionDateEnd,
      LocalDate publicationDate,
      Boolean scheduledOnly,
      PublicationStatus status,
      Boolean withError,
      Boolean myDocOfficeOnly,
      Boolean withDuplicateWarning,
      DocumentationOfficeDTO documentationOfficeDTO) {
    if ((fileNumber == null || fileNumber.trim().isEmpty())) {
      return repository.searchByDocumentationUnitSearchInput(
          documentationOfficeDTO.getId(),
          documentNumber,
          courtType,
          courtLocation,
          decisionDate,
          decisionDateEnd,
          publicationDate,
          scheduledOnly,
          status,
          withError,
          myDocOfficeOnly,
          withDuplicateWarning,
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
          repository.searchByDocumentationUnitSearchInputFileNumber(
              documentationOfficeDTO.getId(),
              documentNumber,
              fileNumber.trim(),
              courtType,
              courtLocation,
              decisionDate,
              decisionDateEnd,
              publicationDate,
              scheduledOnly,
              status,
              withError,
              myDocOfficeOnly,
              withDuplicateWarning,
              fixedPageRequest);

      deviatingFileNumberResults =
          repository.searchByDocumentationUnitSearchInputDeviatingFileNumber(
              documentationOfficeDTO.getId(),
              documentNumber,
              fileNumber.trim(),
              courtType,
              courtLocation,
              decisionDate,
              decisionDateEnd,
              publicationDate,
              scheduledOnly,
              status,
              withError,
              myDocOfficeOnly,
              withDuplicateWarning,
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
                  if (o1.getDate() != null && o2.getDate() != null) {
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
      OidcUser oidcUser,
      @Param("searchInput") DocumentationUnitSearchInput searchInput) {

    DocumentationOffice documentationOffice = userService.getDocumentationOffice(oidcUser);
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
            searchInput.fileNumber(),
            searchInput.decisionDate(),
            searchInput.decisionDateEnd(),
            searchInput.publicationDate(),
            searchInput.scheduledOnly(),
            searchInput.status() != null ? searchInput.status().publicationStatus() : null,
            withError,
            searchInput.myDocOfficeOnly(),
            searchInput.withDuplicateWarning(),
            documentationOfficeDTO);

    return allResults.map(DocumentationUnitListItemTransformer::transformToDomain);
  }

  @Override
  public Map<RelatedDocumentationType, Long> getAllRelatedDocumentationUnitsByDocumentNumber(
      String documentNumber) {
    return relatedDocumentationRepository.findAllByDocumentNumber(documentNumber).stream()
        .collect(Collectors.groupingBy(RelatedDocumentationDTO::getType, Collectors.counting()));
  }

  @Override
  public List<UUID> getRandomDocumentationUnitIds() {
    return repository.getRandomDocumentationUnitIds();
  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public List<DocumentationUnit> getScheduledDocumentationUnitsDueNow() {
    return repository.getScheduledDocumentationUnitsDueNow().stream()
        .limit(50)
        .filter(DecisionDTO.class::isInstance) // TODO transform pending proceedings as well
        .map(decision -> DecisionTransformer.transformToDomain((DecisionDTO) decision))
        .toList();
  }
}
