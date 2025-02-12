package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.DecisionTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentTypeTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationOfficeTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationUnitListItemTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ReferenceTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.StatusTransformer;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitListItem;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitSearchInput;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateRelationStatus;
import de.bund.digitalservice.ris.caselaw.domain.Procedure;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.Reference;
import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationType;
import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationUnit;
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
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.annotation.Primary;
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
@SuppressWarnings("java:S6539")
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

  private static final String STATUS = "status";
  private static final String PUBLICATION_STATUS = "publicationStatus";
  private static final String DOCUMENTATION_OFFICE = "documentationOffice";
  private static final String DOCUMENT_NUMBER = "documentNumber";
  private static final String SCHEDULED_PUBLICATION_DATE_TIME = "scheduledPublicationDateTime";

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
  public DocumentationUnit findByDocumentNumber(String documentNumber)
      throws DocumentationUnitNotExistsException {
    var documentationUnit =
        repository
            .findByDocumentNumber(documentNumber)
            .orElseThrow(() -> new DocumentationUnitNotExistsException(documentNumber));
    return getDocumentationUnit(documentationUnit);
  }

  @Nullable
  private static DocumentationUnit getDocumentationUnit(DocumentationUnitDTO documentationUnit) {
    if (documentationUnit instanceof DecisionDTO decisionDTO) {
      return DecisionTransformer.transformToDomain(decisionDTO);
    }
    // TODO other transformer
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
  public DocumentationUnit findByUuid(UUID uuid) throws DocumentationUnitNotExistsException {
    var documentationUnit =
        repository.findById(uuid).orElseThrow(() -> new DocumentationUnitNotExistsException(uuid));
    return getDocumentationUnit(documentationUnit);
  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public DocumentationUnit createNewDocumentationUnit(
      DocumentationUnit docUnit, Status status, Reference createdFromReference, String source) {

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

    ReferenceDTO referenceDTO = null;
    if (createdFromReference != null) {
      referenceDTO = ReferenceTransformer.transformToDTO(createdFromReference);
      referenceDTO.setDocumentationUnitRank(0);
      referenceDTO.setDocumentationUnit(documentationUnitDTO);
    }

    DecisionDTO.DecisionDTOBuilder<?, ?> builder =
        documentationUnitDTO.toBuilder()
            .source(
                source == null
                    ? new ArrayList<>()
                    : new ArrayList<>(
                        List.of(
                            SourceDTO.builder()
                                .rank(1)
                                .value(source)
                                .reference(referenceDTO)
                                .build())));

    builder.status(
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
  public void save(DocumentationUnit documentationUnit) {

    DocumentationUnitDTO documentationUnitDTO =
        repository.findById(documentationUnit.uuid()).orElse(null);
    if (documentationUnitDTO == null) {
      log.info("Can't save non-existing docUnit with id = " + documentationUnit.uuid());
      return;
    }

    // ---
    // Doing database-related (pre) transformation

    if (documentationUnit.coreData() != null) {
      documentationUnitDTO.getRegions().clear();
      if (documentationUnit.coreData().court() != null
          && documentationUnit.coreData().court().id() != null) {
        Optional<CourtDTO> court =
            databaseCourtRepository.findById(documentationUnit.coreData().court().id());
        if (court.isPresent() && court.get().getRegion() != null) {
          documentationUnitDTO.getRegions().add(court.get().getRegion());
        }
        // delete leading decision norm references if court is not BGH
        if (court.isPresent() && !court.get().getType().equals("BGH")) {
          documentationUnit =
              documentationUnit.toBuilder()
                  .coreData(
                      documentationUnit.coreData().toBuilder()
                          .leadingDecisionNormReferences(List.of())
                          .build())
                  .build();
        }
      }
    }

    // ---

    // Transform non-database-related properties
    if (documentationUnitDTO instanceof DecisionDTO decisionDTO) {
      documentationUnitDTO = DecisionTransformer.transformToDTO(decisionDTO, documentationUnit);
      repository.save(documentationUnitDTO);
    }
    // TODO pending proceeding
  }

  @Override
  public void saveKeywords(DocumentationUnit documentationUnit) {
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
  public void saveFieldsOfLaw(DocumentationUnit documentationUnit) {
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
  public void saveProcedures(DocumentationUnit documentationUnit) {
    if (documentationUnit == null
        || documentationUnit.coreData() == null
        || documentationUnit.coreData().procedure() == null) {
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
  public void delete(DocumentationUnit documentationUnit) {
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

    // 1. Filter by excluded document number
    if (documentNumberToExclude != null) {
      Predicate documentNumberPredicate =
          criteriaBuilder.notEqual(root.get(DOCUMENT_NUMBER), documentNumberToExclude);
      conditions = criteriaBuilder.and(conditions, documentNumberPredicate);
    }

    // 2. Filter by court
    if (courtType != null || courtLocation != null) {
      conditions = addCourtFilter(criteriaBuilder, root, courtType, courtLocation, conditions);
    }

    // 3. Filter by decision date
    if (decisionDate != null) {
      conditions = addDecisionDateFilter(criteriaBuilder, root, decisionDate, null, conditions);
    }

    // 4. Filter by file number
    if (fileNumber != null) {
      conditions = addFileNumberFilter(criteriaBuilder, root, fileNumber, conditions);
    }

    // 5. Filter by document type
    if (documentType != null) {
      conditions = addDocumentTypeFilter(criteriaBuilder, root, documentType, conditions);
    }

    // 6. Filter by documentation office
    Predicate documentationOfficeIdPredicate =
        getDocumentationOfficePredicate(criteriaBuilder, root, documentationOfficeDTO);

    // 7. Filter by publication status
    Predicate publicationStatusPredicate = getPublicationStatusPredicate(criteriaBuilder, root);

    // 8. Filter by external handover
    Predicate externalHandoverPendingPredicate =
        getExternalHandoverPendingPredicate(criteriaBuilder, root, documentationOfficeDTO);

    Predicate finalPredicate =
        criteriaBuilder.or(
            documentationOfficeIdPredicate,
            publicationStatusPredicate,
            externalHandoverPendingPredicate);

    conditions = criteriaBuilder.and(conditions, finalPredicate);

    // Apply conditions to query
    SliceImpl<DocumentationUnitDTO> allResults =
        getPaginatedResults(pageable, criteriaQuery, conditions);
    return allResults.map(DocumentationUnitListItemTransformer::transformToRelatedDocumentation);
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

    return getDocumentationUnitSearchResultDTOS(
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

  @NotNull
  @SuppressWarnings("java:S107")
  private Slice<DocumentationUnitListItem> getDocumentationUnitSearchResultDTOS(
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

    // CriteriaBuilder and CriteriaQuery setup
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<DocumentationUnitDTO> criteriaQuery =
        criteriaBuilder.createQuery(DocumentationUnitDTO.class);
    Root<DocumentationUnitDTO> root = criteriaQuery.from(DocumentationUnitDTO.class);

    // Conditions setup
    Predicate conditions = criteriaBuilder.conjunction(); // Start with an empty conjunction (AND)

    // file number
    if (fileNumber != null) {
      conditions = addFileNumberFilter(criteriaBuilder, root, fileNumber, conditions);
    }

    // court
    if (courtType != null || courtLocation != null) {
      conditions = addCourtFilter(criteriaBuilder, root, courtType, courtLocation, conditions);
    }

    // decision date
    if (decisionDate != null) {
      conditions =
          addDecisionDateFilter(criteriaBuilder, root, decisionDate, decisionDateEnd, conditions);
    }

    // documentNumber
    if (documentNumber != null) {
      conditions = addDocumentNumberFilter(criteriaBuilder, root, documentNumber, conditions);
    }

    // status
    if (status != null) {
      conditions =
          addStatusFilter(
              criteriaBuilder, root, status, documentationOfficeDTO.getId(), conditions);
    } else {
      // permission
      conditions =
          addPermissionFilter(criteriaBuilder, root, documentationOfficeDTO.getId(), conditions);
    }

    // my docOffice only
    if (Boolean.TRUE.equals(myDocOfficeOnly)) {
      conditions =
          addDocOfficeFilter(criteriaBuilder, root, documentationOfficeDTO.getId(), conditions);
    }

    // scheduled only
    if (Boolean.TRUE.equals(scheduledOnly)) {
      conditions = addScheduledFilter(criteriaBuilder, root, conditions);
    }

    // publicationDate filter
    if (publicationDate != null) {
      conditions = addPublicationDateFilter(criteriaBuilder, root, publicationDate, conditions);
    }

    // with error
    if (Boolean.TRUE.equals(withError)) {
      conditions =
          addWithErrorFilter(criteriaBuilder, root, documentationOfficeDTO.getId(), conditions);
    }

    // duplicate warning
    if (Boolean.TRUE.equals(withDuplicateWarning)) {
      conditions =
          addDuplicateFilter(criteriaBuilder, root, documentationOfficeDTO.getId(), conditions);
    }

    // ORDER
    orderResults(publicationDate, scheduledOnly, criteriaBuilder, root, criteriaQuery);

    SliceImpl<DocumentationUnitDTO> allResults =
        getPaginatedResults(pageable, criteriaQuery, conditions);
    return allResults.map(DocumentationUnitListItemTransformer::transformToDomain);
  }

  private static Predicate addCourtFilter(
      CriteriaBuilder criteriaBuilder,
      Root<DocumentationUnitDTO> root,
      String courtType,
      String courtLocation,
      Predicate conditions) {
    // Filter by court type
    if (courtType != null) {
      conditions = addCourtTypeFilter(criteriaBuilder, root, courtType, conditions);
    }
    // Filter by court location
    if (courtLocation != null) {
      conditions = addCourtLocationFilter(criteriaBuilder, root, courtLocation, conditions);
    }
    return conditions;
  }

  private static Predicate addCourtTypeFilter(
      CriteriaBuilder criteriaBuilder,
      Root<DocumentationUnitDTO> root,
      String courtType,
      Predicate conditions) {
    Predicate courtTypePredicate =
        criteriaBuilder.like(
            criteriaBuilder.upper(root.get("court").get("type")), courtType.toUpperCase());
    conditions = criteriaBuilder.and(conditions, courtTypePredicate);
    return conditions;
  }

  private static Predicate addCourtLocationFilter(
      CriteriaBuilder criteriaBuilder,
      Root<DocumentationUnitDTO> root,
      String courtLocation,
      Predicate conditions) {
    Predicate courtLocationPredicate =
        criteriaBuilder.like(
            criteriaBuilder.upper(root.get("court").get("location")), courtLocation.toUpperCase());
    conditions = criteriaBuilder.and(conditions, courtLocationPredicate);
    return conditions;
  }

  private static Predicate getExternalHandoverPendingPredicate(
      CriteriaBuilder criteriaBuilder,
      Root<DocumentationUnitDTO> root,
      DocumentationOfficeDTO documentationOfficeDTO) {
    return criteriaBuilder.and(
        criteriaBuilder.equal(
            root.get(STATUS).get(PUBLICATION_STATUS), PublicationStatus.EXTERNAL_HANDOVER_PENDING),
        criteriaBuilder.equal(
            root.get("creatingDocumentationOffice").get("id"), documentationOfficeDTO.getId()));
  }

  private static Predicate getPublicationStatusPredicate(
      CriteriaBuilder criteriaBuilder, Root<DocumentationUnitDTO> root) {
    return criteriaBuilder.or(
        criteriaBuilder.equal(
            root.get(STATUS).get(PUBLICATION_STATUS), PublicationStatus.PUBLISHED),
        criteriaBuilder.equal(
            root.get(STATUS).get(PUBLICATION_STATUS), PublicationStatus.PUBLISHING));
  }

  private static Predicate getDocumentationOfficePredicate(
      CriteriaBuilder criteriaBuilder,
      Root<DocumentationUnitDTO> root,
      DocumentationOfficeDTO documentationOfficeDTO) {
    return criteriaBuilder.equal(
        root.get(DOCUMENTATION_OFFICE).get("id"), documentationOfficeDTO.getId());
  }

  private static Predicate addDocumentTypeFilter(
      CriteriaBuilder criteriaBuilder,
      Root<DocumentationUnitDTO> root,
      DocumentType documentType,
      Predicate conditions) {
    Predicate documentTypePredicate =
        criteriaBuilder.equal(
            root.get("documentType"), DocumentTypeTransformer.transformToDTO(documentType));
    conditions = criteriaBuilder.and(conditions, documentTypePredicate);
    return conditions;
  }

  private static Predicate addFileNumberFilter(
      CriteriaBuilder criteriaBuilder,
      Root<DocumentationUnitDTO> root,
      String fileNumber,
      Predicate conditions) {
    Join<DocumentationUnitDTO, DeviatingFileNumberDTO> deviatingFileNumberJoin =
        root.join("deviatingFileNumbers", JoinType.LEFT);
    Join<DocumentationUnitDTO, String> fileNumberJoin = root.join("fileNumbers", JoinType.LEFT);
    Predicate fileNumberPredicate =
        criteriaBuilder.or(
            criteriaBuilder.like(
                criteriaBuilder.upper(fileNumberJoin.get("value")), fileNumber.toUpperCase() + "%"),
            criteriaBuilder.like(
                criteriaBuilder.upper(deviatingFileNumberJoin.get("value")),
                fileNumber.toUpperCase() + "%"));
    conditions = criteriaBuilder.and(conditions, fileNumberPredicate);
    return conditions;
  }

  private static Predicate addStatusFilter(
      CriteriaBuilder criteriaBuilder,
      Root<DocumentationUnitDTO> root,
      PublicationStatus status,
      UUID documentationOfficeId,
      Predicate conditions) {
    Predicate statusPredicate =
        criteriaBuilder.and(
            criteriaBuilder.equal(
                criteriaBuilder.upper(root.get(STATUS).get(PUBLICATION_STATUS)), status.toString()),
            criteriaBuilder.or(
                criteriaBuilder
                    .in(root.get(STATUS).get(PUBLICATION_STATUS))
                    .value(PublicationStatus.PUBLISHED)
                    .value(PublicationStatus.PUBLISHING),
                criteriaBuilder.equal(
                    root.get(DOCUMENTATION_OFFICE).get("id"), documentationOfficeId)));
    conditions = criteriaBuilder.and(conditions, statusPredicate);
    return conditions;
  }

  private static Predicate addPermissionFilter(
      CriteriaBuilder criteriaBuilder,
      Root<DocumentationUnitDTO> root,
      UUID documentationOfficeId,
      Predicate conditions) {
    Predicate permissionPredicate =
        criteriaBuilder.or(
            criteriaBuilder.equal(root.get(DOCUMENTATION_OFFICE).get("id"), documentationOfficeId),
            criteriaBuilder
                .in(root.get(STATUS).get(PUBLICATION_STATUS))
                .value(PublicationStatus.PUBLISHED)
                .value(PublicationStatus.PUBLISHING));
    conditions = criteriaBuilder.and(conditions, permissionPredicate);
    return conditions;
  }

  private static Predicate addDecisionDateFilter(
      CriteriaBuilder criteriaBuilder,
      Root<DocumentationUnitDTO> root,
      LocalDate decisionDate,
      LocalDate decisionDateEnd,
      Predicate conditions) {
    Predicate decisionDatePredicate = criteriaBuilder.equal(root.get("date"), decisionDate);
    if (decisionDateEnd != null) {
      decisionDatePredicate =
          criteriaBuilder.between(root.get("date"), decisionDate, decisionDateEnd);
    }
    conditions = criteriaBuilder.and(conditions, decisionDatePredicate);
    return conditions;
  }

  private static Predicate addDocOfficeFilter(
      CriteriaBuilder criteriaBuilder,
      Root<DocumentationUnitDTO> root,
      UUID docOfficeId,
      Predicate conditions) {
    Predicate documentOfficePredicate =
        criteriaBuilder.equal(root.get(DOCUMENTATION_OFFICE).get("id"), docOfficeId);
    conditions = criteriaBuilder.and(conditions, documentOfficePredicate);
    return conditions;
  }

  private static Predicate addScheduledFilter(
      CriteriaBuilder criteriaBuilder, Root<DocumentationUnitDTO> root, Predicate conditions) {
    Predicate scheduledPredicate =
        criteriaBuilder.isNotNull(root.get(SCHEDULED_PUBLICATION_DATE_TIME));
    conditions = criteriaBuilder.and(conditions, scheduledPredicate);
    return conditions;
  }

  private static Predicate addWithErrorFilter(
      CriteriaBuilder criteriaBuilder,
      Root<DocumentationUnitDTO> root,
      UUID documentationOfficeId,
      Predicate conditions) {
    Predicate errorPredicate =
        criteriaBuilder.and(
            criteriaBuilder.equal(root.get(DOCUMENTATION_OFFICE).get("id"), documentationOfficeId),
            criteriaBuilder.isTrue(root.get(STATUS).get("withError")));
    conditions = criteriaBuilder.and(conditions, errorPredicate);
    return conditions;
  }

  private static Predicate addDuplicateFilter(
      CriteriaBuilder criteriaBuilder,
      Root<DocumentationUnitDTO> root,
      UUID documentationOfficeId,
      Predicate conditions) {
    Join<DocumentationUnitDTO, DuplicateRelationDTO> duplicateRelation1 =
        root.join("duplicateRelations1", JoinType.LEFT);
    Join<DocumentationUnitDTO, DuplicateRelationDTO> duplicateRelation2 =
        root.join("duplicateRelations2", JoinType.LEFT);
    Predicate duplicatePredicate =
        criteriaBuilder.and(
            criteriaBuilder.equal(root.get(DOCUMENTATION_OFFICE).get("id"), documentationOfficeId),
            criteriaBuilder.or(
                criteriaBuilder.equal(
                    duplicateRelation1.get(STATUS),
                    criteriaBuilder.literal(DuplicateRelationStatus.PENDING)),
                criteriaBuilder.equal(
                    duplicateRelation2.get(STATUS),
                    criteriaBuilder.literal(DuplicateRelationStatus.PENDING))));
    conditions = criteriaBuilder.and(conditions, duplicatePredicate);
    return conditions;
  }

  private static Predicate addPublicationDateFilter(
      CriteriaBuilder criteriaBuilder,
      Root<DocumentationUnitDTO> root,
      LocalDate publicationDate,
      Predicate conditions) {
    Predicate publicationDatePredicate =
        criteriaBuilder.or(
            criteriaBuilder.equal(
                criteriaBuilder.function(
                    "DATE", LocalDate.class, root.get(SCHEDULED_PUBLICATION_DATE_TIME)),
                publicationDate),
            criteriaBuilder.equal(
                criteriaBuilder.function(
                    "DATE", LocalDate.class, root.get("lastPublicationDateTime")),
                publicationDate));
    conditions = criteriaBuilder.and(conditions, publicationDatePredicate);
    return conditions;
  }

  private static Predicate addDocumentNumberFilter(
      CriteriaBuilder criteriaBuilder,
      Root<DocumentationUnitDTO> root,
      String documentNumber,
      Predicate conditions) {
    Predicate documentNumberPredicate =
        criteriaBuilder.like(
            criteriaBuilder.upper(root.get(DOCUMENT_NUMBER)),
            "%" + documentNumber.toUpperCase() + "%");
    conditions = criteriaBuilder.and(conditions, documentNumberPredicate);
    return conditions;
  }

  private static void orderResults(
      LocalDate publicationDate,
      Boolean scheduledOnly,
      CriteriaBuilder criteriaBuilder,
      Root<DocumentationUnitDTO> root,
      CriteriaQuery<DocumentationUnitDTO> criteriaQuery) {
    List<Order> orderList = new ArrayList<>();
    final Date minDate = new Date(0L);

    if (Boolean.TRUE.equals(scheduledOnly) || publicationDate != null) {
      orderList.add(
          criteriaBuilder.desc(
              // NULL values - last - WORKAROUND
              criteriaBuilder.coalesce(root.get(SCHEDULED_PUBLICATION_DATE_TIME), minDate)));
      orderList.add(
          criteriaBuilder.desc(
              // NULL values - last - WORKAROUND
              criteriaBuilder.coalesce(root.get("lastPublicationDateTime"), minDate)));
    }
    orderList.add(
        criteriaBuilder.desc(
            // NULL values - last - WORKAROUND
            criteriaBuilder.coalesce(root.get("date"), minDate)));
    orderList.add(criteriaBuilder.asc(root.get(DOCUMENT_NUMBER)));
    criteriaQuery.orderBy(orderList);
  }

  @NotNull
  private SliceImpl<DocumentationUnitDTO> getPaginatedResults(
      Pageable pageable, CriteriaQuery<DocumentationUnitDTO> criteriaQuery, Predicate conditions) {
    // Apply conditions to query
    criteriaQuery.where(conditions);

    // Apply pagination
    TypedQuery<DocumentationUnitDTO> query = entityManager.createQuery(criteriaQuery);
    query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
    query.setMaxResults(pageable.getPageSize());

    // Get results and create Slice
    List<DocumentationUnitDTO> resultList = query.getResultList();
    boolean hasNext = resultList.size() == pageable.getPageSize();

    return new SliceImpl<>(resultList, pageable, hasNext);
  }
}
