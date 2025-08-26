package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationUnitListItemTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitListItem;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitSearchInput;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitSearchRepository;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateRelationStatus;
import de.bund.digitalservice.ris.caselaw.domain.InboxStatus;
import de.bund.digitalservice.ris.caselaw.domain.Kind;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.metamodel.SingularAttribute;
import java.security.InvalidParameterException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.NullPrecedence;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaPredicate;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Repository
public class PostgresDocumentationUnitSearchRepositoryImpl
    implements DocumentationUnitSearchRepository {

  private final UserService userService;
  private static final Set<PublicationStatus> PublicStatusSet =
      Set.of(PublicationStatus.PUBLISHED, PublicationStatus.PUBLISHING);
  private final DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @PersistenceContext private EntityManager entityManager;

  public PostgresDocumentationUnitSearchRepositoryImpl(
      UserService userService,
      DatabaseDocumentationOfficeRepository documentationOfficeRepository) {
    this.userService = userService;
    this.documentationOfficeRepository = documentationOfficeRepository;
  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager", readOnly = true)
  public Slice<DocumentationUnitListItem> searchByDocumentationUnitSearchInput(
      DocumentationUnitSearchInput searchInput, Pageable pageable, OidcUser oidcUser) {
    SearchParameters parameters = getSearchParameters(searchInput, oidcUser);

    HibernateCriteriaBuilder cb = (HibernateCriteriaBuilder) entityManager.getCriteriaBuilder();
    CriteriaQuery<DocumentationUnitListItemDTO> cq =
        cb.createQuery(DocumentationUnitListItemDTO.class);

    Root<DocumentationUnitDTO> root = cq.from(DocumentationUnitDTO.class);

    List<Predicate> predicates = new ArrayList<>();
    predicates.addAll(getDocNumberPredicates(parameters, cq, cb, root));
    predicates.addAll(getCourtTypePredicates(parameters, cb, root));
    predicates.addAll(getCourtLocationPredicates(parameters, cb, root));
    predicates.addAll(getDecisionDatePredicates(parameters, cb, root));
    predicates.addAll(getMyDocOfficePredicates(parameters, cb, root));
    predicates.addAll(getScheduledOnlyPredicates(parameters, cb, root));
    predicates.addAll(getErrorPredicates(parameters, cb, root));
    predicates.addAll(getStatusPredicates(parameters, cb, root));
    predicates.addAll(getPublicationDatePredicates(parameters, cb, root));
    predicates.addAll(getInboxStatusPredicates(parameters, cb, root));
    predicates.addAll(getDuplicateWarningPredicates(parameters, cq, cb, root));
    predicates.addAll(getResolutionDatePredicates(parameters, cb, root));
    predicates.addAll(getIsResolvedPredicates(parameters, cb, root));
    predicates.addAll(getFileNumberPredicates(parameters, cq, cb, root));
    predicates.addAll(getDocUnitKindPredicates(parameters, cb, root));
    predicates.addAll(getProcessStepPredicates(parameters, cb, root));

    // Use cb.construct() to only select the DTO projection instead of the full entity
    cq.select(root).where(predicates.toArray(new Predicate[0]));

    List<Order> orderCriteria = getOrderCriteria(parameters, cb, root);
    cq.orderBy(orderCriteria);

    TypedQuery<DocumentationUnitListItemDTO> query = entityManager.createQuery(cq);
    query.setFirstResult((int) pageable.getOffset());
    query.setMaxResults(pageable.getPageSize() + 1); // +1 to check hasNext

    List<DocumentationUnitListItemDTO> resultList = query.getResultList();
    boolean hasNext = resultList.size() > pageable.getPageSize();
    if (hasNext) {
      resultList = resultList.subList(0, pageable.getPageSize());
    }

    List<DocumentationUnitListItem> docUnitDomainResults =
        resultList.stream().map(DocumentationUnitListItemTransformer::transformToDomain).toList();

    return new SliceImpl<>(docUnitDomainResults, pageable, hasNext);
  }

  /**
   * We strip of the time part of the timestamp, so that we can search for the date only ignoring
   * the time.
   */
  private Expression<Date> getDateOnly(
      HibernateCriteriaBuilder cb, Path<LocalDateTime> dateTimeColumn) {
    return cb.function("date", Date.class, dateTimeColumn);
  }

  private List<Predicate> getDocNumberPredicates(
      SearchParameters parameters,
      CriteriaQuery<DocumentationUnitListItemDTO> cq,
      HibernateCriteriaBuilder cb,
      Root<DocumentationUnitDTO> root) {
    List<Predicate> predicates = new ArrayList<>();
    if (parameters.documentNumber.isPresent()
        && !parameters.documentNumber.get().trim().isEmpty()) {
      String documentNumberLike = "%" + parameters.documentNumber.get().trim().toUpperCase() + "%";
      Predicate documentNumberPredicate =
          cb.like(cb.upper(root.get(DocumentationUnitDTO_.documentNumber)), documentNumberLike);

      Subquery<UUID> subqueryDeviatingFileNumber = cq.subquery(UUID.class);
      Root<DeviatingDocumentNumberDTO> subRootDeviatingFileNumber =
          subqueryDeviatingFileNumber.from(DeviatingDocumentNumberDTO.class);
      subqueryDeviatingFileNumber.select(
          subRootDeviatingFileNumber
              .get(DeviatingDocumentNumberDTO_.documentationUnit)
              .get(DocumentationUnitDTO_.id));
      subqueryDeviatingFileNumber.where(
          cb.like(
              cb.upper(subRootDeviatingFileNumber.get(DeviatingDocumentNumberDTO_.value)),
              documentNumberLike));
      Predicate deviatingDocumentNumberPredicate =
          root.get(DocumentationUnitDTO_.id).in(subqueryDeviatingFileNumber);

      Predicate anyDocumentNumberPredicate =
          cb.or(documentNumberPredicate, deviatingDocumentNumberPredicate);
      predicates.add(anyDocumentNumberPredicate);
    }
    return predicates;
  }

  private List<Predicate> getCourtTypePredicates(
      SearchParameters parameters, HibernateCriteriaBuilder cb, Root<DocumentationUnitDTO> root) {
    List<Predicate> predicates = new ArrayList<>();
    if (parameters.courtType.isPresent() && !parameters.courtType.get().trim().isEmpty()) {
      Predicate courtTypePredicate =
          cb.like(
              cb.upper(root.get(DocumentationUnitDTO_.court).get(CourtDTO_.type)),
              parameters.courtType.get().trim().toUpperCase());
      predicates.add(courtTypePredicate);
    }
    return predicates;
  }

  private List<Predicate> getCourtLocationPredicates(
      SearchParameters parameters, HibernateCriteriaBuilder cb, Root<DocumentationUnitDTO> root) {
    List<Predicate> predicates = new ArrayList<>();
    if (parameters.courtLocation.isPresent() && !parameters.courtLocation.get().trim().isEmpty()) {
      Predicate courtTypePredicate =
          cb.like(
              cb.upper(root.get(DocumentationUnitDTO_.court).get(CourtDTO_.location)),
              parameters.courtLocation.get().trim().toUpperCase());
      predicates.add(courtTypePredicate);
    }
    return predicates;
  }

  private List<Predicate> getDecisionDatePredicates(
      SearchParameters parameters, HibernateCriteriaBuilder cb, Root<DocumentationUnitDTO> root) {
    List<Predicate> predicates = new ArrayList<>();
    if (parameters.decisionDate.isPresent()) {
      Predicate decisionDatePredicate;
      if (parameters.decisionDateEnd.isPresent()) {
        decisionDatePredicate =
            cb.between(
                root.get(DocumentationUnitDTO_.date),
                parameters.decisionDate.get(),
                parameters.decisionDateEnd.get());
      } else {
        decisionDatePredicate =
            cb.equal(root.get(DocumentationUnitDTO_.date), parameters.decisionDate.get());
      }
      predicates.add(decisionDatePredicate);
    }
    return predicates;
  }

  private List<Predicate> getResolutionDatePredicates(
      SearchParameters parameters, HibernateCriteriaBuilder cb, Root<DocumentationUnitDTO> root) {
    List<Predicate> predicates = new ArrayList<>();

    if (parameters.resolutionDate.isPresent()) {
      // Use cb.treat() to downcast the root to PendingProceedingDTO
      Root<PendingProceedingDTO> pendingProceedingRoot = cb.treat(root, PendingProceedingDTO.class);

      Predicate resolutionDatePredicate;
      if (parameters.resolutionDateEnd.isPresent()) {
        resolutionDatePredicate =
            cb.between(
                pendingProceedingRoot.get(PendingProceedingDTO_.resolutionDate),
                parameters.resolutionDate.get(),
                parameters.resolutionDateEnd.get());
      } else {
        resolutionDatePredicate =
            cb.equal(
                pendingProceedingRoot.get(PendingProceedingDTO_.resolutionDate),
                parameters.resolutionDate.get());
      }
      predicates.add(resolutionDatePredicate);
    }
    return predicates;
  }

  private List<Predicate> getIsResolvedPredicates(
      SearchParameters parameters, HibernateCriteriaBuilder cb, Root<DocumentationUnitDTO> root) {
    List<Predicate> predicates = new ArrayList<>();
    if (parameters.isResolved) {
      // Use cb.treat() to downcast the root to PendingProceedingDTO
      Root<PendingProceedingDTO> pendingProceedingRoot = cb.treat(root, PendingProceedingDTO.class);
      Predicate isResolvedPredicate =
          cb.equal(pendingProceedingRoot.get(PendingProceedingDTO_.isResolved), true);
      predicates.add(isResolvedPredicate);
    }
    return predicates;
  }

  private List<Predicate> getMyDocOfficePredicates(
      SearchParameters parameters, HibernateCriteriaBuilder cb, Root<DocumentationUnitDTO> root) {
    List<Predicate> predicates = new ArrayList<>();
    if (parameters.myDocOfficeOnly) {
      Predicate myDocOfficePredicate =
          cb.equal(
              root.get(DocumentationUnitDTO_.documentationOffice),
              parameters.documentationOfficeDTO);
      predicates.add(myDocOfficePredicate);
    }
    return predicates;
  }

  private List<Predicate> getScheduledOnlyPredicates(
      SearchParameters parameters, HibernateCriteriaBuilder cb, Root<DocumentationUnitDTO> root) {
    List<Predicate> predicates = new ArrayList<>();
    if (parameters.scheduledOnly) {
      Predicate scheduledPublicationDatePredicate =
          cb.isNotNull(root.get(DocumentationUnitDTO_.scheduledPublicationDateTime));
      predicates.add(scheduledPublicationDatePredicate);
    }
    return predicates;
  }

  private List<Predicate> getErrorPredicates(
      SearchParameters parameters, HibernateCriteriaBuilder cb, Root<DocumentationUnitDTO> root) {
    List<Predicate> predicates = new ArrayList<>();
    if (parameters.withError) {
      Predicate errorStatusPredicate =
          cb.equal(root.get(DocumentationUnitDTO_.status).get(StatusDTO_.withError), true);
      predicates.add(errorStatusPredicate);
      if (!parameters.myDocOfficeOnly) {
        Predicate myDocOfficePredicate =
            cb.equal(
                root.get(DocumentationUnitDTO_.documentationOffice),
                parameters.documentationOfficeDTO);
        predicates.add(myDocOfficePredicate);
      }
    }
    return predicates;
  }

  private List<Predicate> getStatusPredicates(
      SearchParameters parameters, HibernateCriteriaBuilder cb, Root<DocumentationUnitDTO> root) {
    List<Predicate> predicates = new ArrayList<>();
    if (parameters.publicationStatus.isPresent()) {
      Predicate publicationStatusPredicate =
          cb.equal(
              root.get(DocumentationUnitDTO_.status).get(StatusDTO_.publicationStatus),
              parameters.publicationStatus.get());
      predicates.add(publicationStatusPredicate);
      if (!parameters.myDocOfficeOnly
          && !PublicStatusSet.contains(parameters.publicationStatus.get())) {
        // If user selects a non-public status, we will only show documents from the user's doc
        // office
        Predicate myDocOfficePredicate =
            cb.equal(
                root.get(DocumentationUnitDTO_.documentationOffice),
                parameters.documentationOfficeDTO);
        predicates.add(myDocOfficePredicate);
      }
    } else if (!parameters.myDocOfficeOnly) {
      // User may only see published documents from other doc offices
      Predicate publicationStatusPredicate =
          cb.in(
              root.get(DocumentationUnitDTO_.status).get(StatusDTO_.publicationStatus),
              PublicStatusSet);
      Predicate myDocOfficePredicate =
          cb.equal(
              root.get(DocumentationUnitDTO_.documentationOffice),
              parameters.documentationOfficeDTO);
      predicates.add(cb.or(publicationStatusPredicate, myDocOfficePredicate));
    }
    return predicates;
  }

  private List<Predicate> getPublicationDatePredicates(
      SearchParameters parameters, HibernateCriteriaBuilder cb, Root<DocumentationUnitDTO> root) {
    List<Predicate> predicates = new ArrayList<>();
    if (parameters.publicationDate.isPresent()) {
      Expression<Date> scheduledPublicationDateTime =
          getDateOnly(cb, root.get(DocumentationUnitDTO_.scheduledPublicationDateTime));
      Predicate scheduledDatePredicate =
          cb.equal(scheduledPublicationDateTime, parameters.publicationDate.get());

      Expression<Date> lastPublicationDateTime =
          getDateOnly(cb, root.get(DocumentationUnitDTO_.lastHandoverDateTime));
      Predicate lastDatePredicate =
          cb.equal(lastPublicationDateTime, parameters.publicationDate.get());

      Predicate publicationDatePredicate = cb.or(scheduledDatePredicate, lastDatePredicate);
      predicates.add(publicationDatePredicate);
    }
    return predicates;
  }

  private List<Predicate> getInboxStatusPredicates(
      SearchParameters parameters, HibernateCriteriaBuilder cb, Root<DocumentationUnitDTO> root) {
    List<Predicate> predicates = new ArrayList<>();
    if (parameters.inboxStatus.isPresent()) {
      Predicate inboxStatusPredicate =
          cb.equal(root.get(DocumentationUnitDTO_.inboxStatus), parameters.inboxStatus.get());
      predicates.add(inboxStatusPredicate);
    }
    return predicates;
  }

  private List<Predicate> getDocUnitKindPredicates(
      SearchParameters parameters, HibernateCriteriaBuilder cb, Root<DocumentationUnitDTO> root) {
    List<Predicate> predicates = new ArrayList<>();
    if (parameters.kind.isPresent()) {
      Class<? extends DocumentationUnitDTO> kindClass =
          switch (parameters.kind.get()) {
            case DECISION -> DecisionDTO.class;
            case PENDING_PROCEEDING -> PendingProceedingDTO.class;
            default -> throw new InvalidParameterException("Unsupported documentation unit kind");
          };
      Predicate kindPredicate = cb.equal(root.type(), kindClass);
      predicates.add(kindPredicate);
    }
    return predicates;
  }

  private List<Predicate> getDuplicateWarningPredicates(
      SearchParameters parameters,
      CriteriaQuery<DocumentationUnitListItemDTO> cq,
      HibernateCriteriaBuilder cb,
      Root<DocumentationUnitDTO> root) {
    List<Predicate> predicates = new ArrayList<>();
    if (parameters.withDuplicateWarning) {
      Predicate matchLeft =
          getDuplicateRelationPredicate(cq, cb, root, DuplicateRelationDTO_.documentationUnit1);
      Predicate matchRight =
          getDuplicateRelationPredicate(cq, cb, root, DuplicateRelationDTO_.documentationUnit2);
      predicates.add(cb.or(matchLeft, matchRight));
      if (!parameters.myDocOfficeOnly()) {
        // If not already explicitly filtered by user, we will add it.
        Predicate myDocOfficePredicate =
            cb.equal(
                root.get(DocumentationUnitDTO_.documentationOffice),
                parameters.documentationOfficeDTO());
        predicates.add(myDocOfficePredicate);
      }
    }
    return predicates;
  }

  /**
   * Duplicate relations are between two doc units. This produces a predicate that finds pending
   * duplicate relations where the given doc unit (root) matches the given side of the relationship
   * (targetColumn).
   */
  private Predicate getDuplicateRelationPredicate(
      CriteriaQuery<DocumentationUnitListItemDTO> cq,
      HibernateCriteriaBuilder cb,
      Root<DocumentationUnitDTO> root,
      SingularAttribute<DuplicateRelationDTO, DecisionDTO> targetColumn) {
    Subquery<Integer> subquery = cq.subquery(Integer.class);
    Root<DuplicateRelationDTO> subRoot = subquery.from(DuplicateRelationDTO.class);
    subquery.select(cb.literal(1));
    JpaPredicate docUnitMatch =
        cb.equal(
            subRoot.get(targetColumn).get(DocumentationUnitDTO_.id),
            root.get(DocumentationUnitDTO_.id));
    JpaPredicate statusMatch =
        cb.equal(
            subRoot.get(DuplicateRelationDTO_.relationStatus), DuplicateRelationStatus.PENDING);
    subquery.where(docUnitMatch, statusMatch);
    return cb.exists(subquery);
  }

  /*
   * Postgres cannot properly optimize the IN subquery.
   * It would be more performant to use ANY(ARRAY(subquery)) instead of IN(subquery). (600ms -> 20ms)
   * However, this is currently not possible with the JPA Criteria API,
   * see https://discourse.hibernate.org/t/using-any-array-with-hibernate-6-4-4-on-postgresql-16/10977
   */
  private List<Predicate> getFileNumberPredicates(
      SearchParameters parameters,
      CriteriaQuery<DocumentationUnitListItemDTO> cq,
      HibernateCriteriaBuilder cb,
      Root<DocumentationUnitDTO> root) {
    List<Predicate> predicates = new ArrayList<>();
    if (parameters.fileNumber.isPresent() && !parameters.fileNumber.get().trim().isEmpty()) {
      String fileNumberLike = parameters.fileNumber.get().trim().toUpperCase() + "%";
      Subquery<UUID> subqueryFileNumber = cq.subquery(UUID.class);
      Root<FileNumberDTO> subRootFileNumber = subqueryFileNumber.from(FileNumberDTO.class);
      subqueryFileNumber.select(
          subRootFileNumber.get(FileNumberDTO_.documentationUnit).get(DocumentationUnitDTO_.id));
      subqueryFileNumber.where(
          cb.like(cb.upper(subRootFileNumber.get(FileNumberDTO_.value)), fileNumberLike));

      Subquery<UUID> subqueryDeviatingFileNumber = cq.subquery(UUID.class);
      Root<DeviatingFileNumberDTO> subRootDeviatingFileNumber =
          subqueryDeviatingFileNumber.from(DeviatingFileNumberDTO.class);
      subqueryDeviatingFileNumber.select(
          subRootDeviatingFileNumber
              .get(DeviatingFileNumberDTO_.documentationUnit)
              .get(DocumentationUnitDTO_.id));
      subqueryDeviatingFileNumber.where(
          cb.like(
              cb.upper(subRootDeviatingFileNumber.get(DeviatingFileNumberDTO_.value)),
              fileNumberLike));
      Predicate fileNumberPredicate = root.get(DocumentationUnitDTO_.id).in(subqueryFileNumber);
      Predicate deviatingFileNumberPredicate =
          root.get(DocumentationUnitDTO_.id).in(subqueryDeviatingFileNumber);
      Predicate anyFileNumberPredicate = cb.or(fileNumberPredicate, deviatingFileNumberPredicate);
      predicates.add(anyFileNumberPredicate);
    }
    return predicates;
  }

  private List<Predicate> getProcessStepPredicates(
      SearchParameters parameters, HibernateCriteriaBuilder cb, Root<DocumentationUnitDTO> root) {
    List<Predicate> predicates = new ArrayList<>();
    if (parameters.processStepId.isPresent()) {
      Predicate processStepPredicate =
          cb.equal(
              root.get(DocumentationUnitDTO_.currentProcessStep)
                  .get(DocumentationUnitProcessStepDTO_.processStep)
                  .get(ProcessStepDTO_.id),
              parameters.processStepId.get());
      predicates.add(processStepPredicate);
    }
    return predicates;
  }

  @NotNull
  private List<Order> getOrderCriteria(
      SearchParameters parameters, HibernateCriteriaBuilder cb, Root<DocumentationUnitDTO> root) {
    List<Order> orderCriteria = new ArrayList<>();
    if (parameters.scheduledOnly || parameters.publicationDate.isPresent()) {
      orderCriteria.add(
          cb.desc(root.get(DocumentationUnitDTO_.scheduledPublicationDateTime))
              .nullPrecedence(NullPrecedence.LAST));
      orderCriteria.add(
          cb.desc(root.get(DocumentationUnitDTO_.lastHandoverDateTime))
              .nullPrecedence(NullPrecedence.LAST));
    }

    orderCriteria.add(
        cb.desc(root.get(DocumentationUnitDTO_.date)).nullPrecedence(NullPrecedence.LAST));
    orderCriteria.add(cb.desc(root.get(DocumentationUnitDTO_.documentNumber)));
    return orderCriteria;
  }

  private SearchParameters getSearchParameters(
      DocumentationUnitSearchInput searchInput, OidcUser oidcUser) {
    DocumentationOffice documentationOffice = userService.getDocumentationOffice(oidcUser);
    log.debug("Find by overview search: {}, {}", documentationOffice.abbreviation(), searchInput);

    DocumentationOfficeDTO documentationOfficeDTO =
        documentationOfficeRepository.findByAbbreviation(documentationOffice.abbreviation());

    PublicationStatus status =
        searchInput.status() != null ? searchInput.status().publicationStatus() : null;
    Boolean withError =
        Optional.ofNullable(searchInput.status()).map(Status::withError).orElse(false);
    return SearchParameters.builder()
        .courtType(Optional.ofNullable(searchInput.courtType()))
        .courtLocation(Optional.ofNullable(searchInput.courtLocation()))
        .documentNumber(Optional.ofNullable(searchInput.documentNumber()))
        .fileNumber(Optional.ofNullable(searchInput.fileNumber()))
        .decisionDate(Optional.ofNullable(searchInput.decisionDate()))
        .decisionDateEnd(Optional.ofNullable(searchInput.decisionDateEnd()))
        .publicationDate(Optional.ofNullable(searchInput.publicationDate()))
        .publicationStatus(Optional.ofNullable(status))
        .scheduledOnly(searchInput.scheduledOnly())
        .withError(withError)
        .myDocOfficeOnly(searchInput.myDocOfficeOnly())
        .withDuplicateWarning(searchInput.withDuplicateWarning())
        .resolutionDate(Optional.ofNullable(searchInput.resolutionDate()))
        .resolutionDateEnd(Optional.ofNullable(searchInput.resolutionDateEnd()))
        .isResolved(searchInput.isResolved())
        .inboxStatus(Optional.ofNullable(searchInput.inboxStatus()))
        .documentationOfficeDTO(documentationOfficeDTO)
        .kind(Optional.ofNullable(searchInput.kind()))
        .processStepId(Optional.ofNullable(searchInput.processStepId()))
        .build();
  }

  @Builder
  private record SearchParameters(
      Optional<String> courtType,
      Optional<String> courtLocation,
      Optional<String> documentNumber,
      Optional<String> fileNumber,
      Optional<LocalDate> decisionDate,
      Optional<LocalDate> decisionDateEnd,
      Optional<LocalDate> publicationDate,
      Optional<PublicationStatus> publicationStatus,
      boolean scheduledOnly,
      boolean withError,
      boolean myDocOfficeOnly,
      boolean withDuplicateWarning,
      Optional<LocalDate> resolutionDate,
      Optional<LocalDate> resolutionDateEnd,
      boolean isResolved,
      Optional<InboxStatus> inboxStatus,
      DocumentationOfficeDTO documentationOfficeDTO,
      Optional<Kind> kind,
      Optional<UUID> processStepId) {}
}
