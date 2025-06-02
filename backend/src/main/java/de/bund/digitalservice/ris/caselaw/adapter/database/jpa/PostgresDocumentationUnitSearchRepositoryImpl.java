package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.database.DocumentationUnitSearchRepository;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateRelationStatus;
import de.bund.digitalservice.ris.caselaw.domain.FeatureToggleService;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.metamodel.SingularAttribute;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.hibernate.query.NullPrecedence;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaPredicate;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

// S3655: Sonarqube does not understand that Optional.isPresent() is called on params
@SuppressWarnings({"java:S3655"})
@Repository
public class PostgresDocumentationUnitSearchRepositoryImpl
    implements DocumentationUnitSearchRepository {

  private static final Set<PublicationStatus> PublicStatusSet =
      Set.of(PublicationStatus.PUBLISHED, PublicationStatus.PUBLISHING);
  private final FeatureToggleService featureToggleService;
  @PersistenceContext private EntityManager entityManager;

  public PostgresDocumentationUnitSearchRepositoryImpl(FeatureToggleService featureToggleService) {
    this.featureToggleService = featureToggleService;
  }

  @Override
  public Slice<DocumentationUnitListItemDTO> search(
      SearchParameters parameters, Pageable pageable) {
    HibernateCriteriaBuilder cb = (HibernateCriteriaBuilder) entityManager.getCriteriaBuilder();
    CriteriaQuery<DocumentationUnitListItemDTO> cq =
        cb.createQuery(DocumentationUnitListItemDTO.class);

    Root<DocumentationUnitDTO> root = cq.from(DocumentationUnitDTO.class);
    if (featureToggleService.isEnabled("neuris.search-fetch-relationships")) {
      root.fetch(DocumentationUnitDTO_.managementData, JoinType.LEFT);
      root.fetch(DocumentationUnitDTO_.court, JoinType.LEFT);
      root.fetch(DocumentationUnitDTO_.documentType, JoinType.LEFT);
      root.fetch(DocumentationUnitDTO_.status, JoinType.LEFT);
      root.fetch(DocumentationUnitDTO_.documentationOffice, JoinType.LEFT);
    }
    List<Predicate> predicates = new ArrayList<>();

    predicates.addAll(getDocNumberPredicates(parameters, cb, root));
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
    predicates.addAll(getFileNumberPredicates(parameters, cq, cb, root));

    // TODO: Use cb.construct() to actually only select the DTO projection
    //    cq.select(cb.construct(DocumentationUnitListItemDTO.class, root.get("id"),
    // root.get("documentNumber"))).where(predicates.toArray(new Predicate[0]));
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

    if (featureToggleService.isEnabled("neuris.search-fetch-relationships")) {
      // Fetching relationships
      List<UUID> docUnitIds = resultList.stream().map(DocumentationUnitListItemDTO::getId).toList();
      fetchFileNumbers(docUnitIds);
      // TODO: Filter for decisions?
      fetchSources(docUnitIds);
      fetchAttachments(docUnitIds);
    }

    return new SliceImpl<>(resultList, pageable, hasNext);
  }

  /**
   * We strip of the time part of the timestamp, so that we can search for the date only ignoring
   * the time.
   */
  private Expression<Date> getDateOnly(
      HibernateCriteriaBuilder cb, Path<LocalDateTime> dateTimeColumn) {
    return cb.function("date", Date.class, dateTimeColumn);
  }

  private void fetchFileNumbers(Collection<UUID> ids) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<DocumentationUnitListItemDTO> cq =
        cb.createQuery(DocumentationUnitListItemDTO.class);
    Root<DocumentationUnitDTO> root = cq.from(DocumentationUnitDTO.class);
    root.fetch(DocumentationUnitDTO_.fileNumbers, JoinType.LEFT);
    // No need to fetch deviating file numbers as we do not display them in the list

    cq.select(root).where(root.get(DocumentationUnitDTO_.id).in(ids));

    entityManager.createQuery(cq).getResultList();
  }

  private void fetchSources(Collection<UUID> ids) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<DocumentationUnitListItemDTO> cq =
        cb.createQuery(DocumentationUnitListItemDTO.class);
    Root<DecisionDTO> root = cq.from(DecisionDTO.class);
    Fetch<DecisionDTO, SourceDTO> sourceFetch = root.fetch(DecisionDTO_.source, JoinType.LEFT);
    Fetch<SourceDTO, ReferenceDTO> referenceFetch =
        sourceFetch.fetch(SourceDTO_.reference, JoinType.LEFT);
    referenceFetch.fetch(ReferenceDTO_.edition, JoinType.LEFT);
    root.fetch(DecisionDTO_.procedure, JoinType.LEFT);
    root.fetch(DecisionDTO_.creatingDocumentationOffice, JoinType.LEFT);

    cq.select(root).where(root.get(DocumentationUnitDTO_.id).in(ids));

    entityManager.createQuery(cq).getResultList();
  }

  private void fetchAttachments(Collection<UUID> ids) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<DocumentationUnitListItemDTO> cq =
        cb.createQuery(DocumentationUnitListItemDTO.class);
    Root<DecisionDTO> root = cq.from(DecisionDTO.class);
    root.fetch(DocumentationUnitDTO_.attachments, JoinType.LEFT);

    cq.select(root).where(root.get(DocumentationUnitDTO_.id).in(ids));

    entityManager.createQuery(cq).getResultList();
  }

  private List<Predicate> getDocNumberPredicates(
      SearchParameters parameters, HibernateCriteriaBuilder cb, Root<DocumentationUnitDTO> root) {
    List<Predicate> predicates = new ArrayList<>();
    if (parameters.documentNumber().isPresent()
        && !parameters.documentNumber().get().trim().isEmpty()) {
      Predicate documentNumberPredicate =
          cb.like(
              cb.upper(root.get(DocumentationUnitDTO_.documentNumber)),
              "%" + parameters.documentNumber().get().trim().toUpperCase() + "%");
      predicates.add(documentNumberPredicate);
    }
    return predicates;
  }

  private List<Predicate> getCourtTypePredicates(
      SearchParameters parameters, HibernateCriteriaBuilder cb, Root<DocumentationUnitDTO> root) {
    List<Predicate> predicates = new ArrayList<>();
    if (parameters.courtType().isPresent() && !parameters.courtType().get().trim().isEmpty()) {
      Predicate courtTypePredicate =
          cb.like(
              cb.upper(root.get(DocumentationUnitDTO_.court).get(CourtDTO_.type)),
              parameters.courtType().get().trim().toUpperCase());
      predicates.add(courtTypePredicate);
    }
    return predicates;
  }

  private List<Predicate> getCourtLocationPredicates(
      SearchParameters parameters, HibernateCriteriaBuilder cb, Root<DocumentationUnitDTO> root) {
    List<Predicate> predicates = new ArrayList<>();
    if (parameters.courtLocation().isPresent()
        && !parameters.courtLocation().get().trim().isEmpty()) {
      Predicate courtTypePredicate =
          cb.like(
              cb.upper(root.get(DocumentationUnitDTO_.court).get(CourtDTO_.location)),
              parameters.courtLocation().get().trim().toUpperCase());
      predicates.add(courtTypePredicate);
    }
    return predicates;
  }

  private List<Predicate> getDecisionDatePredicates(
      SearchParameters parameters, HibernateCriteriaBuilder cb, Root<DocumentationUnitDTO> root) {
    List<Predicate> predicates = new ArrayList<>();
    if (parameters.decisionDate().isPresent()) {
      Predicate decisionDatePredicate;
      if (parameters.decisionDateEnd().isPresent()) {
        decisionDatePredicate =
            cb.between(
                root.get(DocumentationUnitDTO_.date),
                parameters.decisionDate().get(),
                parameters.decisionDateEnd().get());
      } else {
        decisionDatePredicate =
            cb.equal(root.get(DocumentationUnitDTO_.date), parameters.decisionDate().get());
      }
      predicates.add(decisionDatePredicate);
    }
    return predicates;
  }

  private List<Predicate> getMyDocOfficePredicates(
      SearchParameters parameters, HibernateCriteriaBuilder cb, Root<DocumentationUnitDTO> root) {
    List<Predicate> predicates = new ArrayList<>();
    if (parameters.myDocOfficeOnly()) {
      Predicate myDocOfficePredicate =
          cb.equal(
              root.get(DocumentationUnitDTO_.documentationOffice),
              parameters.documentationOfficeDTO());
      predicates.add(myDocOfficePredicate);
    }
    return predicates;
  }

  private List<Predicate> getScheduledOnlyPredicates(
      SearchParameters parameters, HibernateCriteriaBuilder cb, Root<DocumentationUnitDTO> root) {
    List<Predicate> predicates = new ArrayList<>();
    if (parameters.scheduledOnly()) {
      Predicate scheduledPublicationDatePredicate =
          cb.isNotNull(root.get(DocumentationUnitDTO_.scheduledPublicationDateTime));
      predicates.add(scheduledPublicationDatePredicate);
    }
    return predicates;
  }

  private List<Predicate> getErrorPredicates(
      SearchParameters parameters, HibernateCriteriaBuilder cb, Root<DocumentationUnitDTO> root) {
    List<Predicate> predicates = new ArrayList<>();
    if (parameters.withError()) {
      Predicate errorStatusPredicate =
          cb.equal(root.get(DocumentationUnitDTO_.status).get(StatusDTO_.withError), true);
      predicates.add(errorStatusPredicate);
      if (!parameters.myDocOfficeOnly()) {
        Predicate myDocOfficePredicate =
            cb.equal(
                root.get(DocumentationUnitDTO_.documentationOffice),
                parameters.documentationOfficeDTO());
        predicates.add(myDocOfficePredicate);
      }
    }
    return predicates;
  }

  private List<Predicate> getStatusPredicates(
      SearchParameters parameters, HibernateCriteriaBuilder cb, Root<DocumentationUnitDTO> root) {
    List<Predicate> predicates = new ArrayList<>();
    if (parameters.publicationStatus().isPresent()) {
      Predicate publicationStatusPredicate =
          cb.equal(
              root.get(DocumentationUnitDTO_.status).get(StatusDTO_.publicationStatus),
              parameters.publicationStatus().get());
      predicates.add(publicationStatusPredicate);
      if (!parameters.myDocOfficeOnly()
          && !PublicStatusSet.contains(parameters.publicationStatus().get())) {
        // If user selects a non-public status, we will only show documents from the user's doc
        // office
        Predicate myDocOfficePredicate =
            cb.equal(
                root.get(DocumentationUnitDTO_.documentationOffice),
                parameters.documentationOfficeDTO());
        predicates.add(myDocOfficePredicate);
      }
    } else if (!parameters.myDocOfficeOnly()) {
      // User may only see published documents from other doc offices
      Predicate publicationStatusPredicate =
          cb.in(
              root.get(DocumentationUnitDTO_.status).get(StatusDTO_.publicationStatus),
              PublicStatusSet);
      Predicate myDocOfficePredicate =
          cb.equal(
              root.get(DocumentationUnitDTO_.documentationOffice),
              parameters.documentationOfficeDTO());
      predicates.add(cb.or(publicationStatusPredicate, myDocOfficePredicate));
    }
    return predicates;
  }

  private List<Predicate> getPublicationDatePredicates(
      SearchParameters parameters, HibernateCriteriaBuilder cb, Root<DocumentationUnitDTO> root) {
    List<Predicate> predicates = new ArrayList<>();
    if (parameters.publicationDate().isPresent()) {
      Expression<Date> scheduledPublicationDateTime =
          getDateOnly(cb, root.get(DocumentationUnitDTO_.scheduledPublicationDateTime));
      Predicate scheduledDatePredicate =
          cb.equal(scheduledPublicationDateTime, parameters.publicationDate().get());

      Expression<Date> lastPublicationDateTime =
          getDateOnly(cb, root.get(DocumentationUnitDTO_.lastPublicationDateTime));
      Predicate lastDatePredicate =
          cb.equal(lastPublicationDateTime, parameters.publicationDate().get());

      Predicate publicationDatePredicate = cb.or(scheduledDatePredicate, lastDatePredicate);
      predicates.add(publicationDatePredicate);
    }
    return predicates;
  }

  private List<Predicate> getInboxStatusPredicates(
      SearchParameters parameters, HibernateCriteriaBuilder cb, Root<DocumentationUnitDTO> root) {
    List<Predicate> predicates = new ArrayList<>();
    if (parameters.inboxStatus().isPresent()) {
      Predicate inboxStatusPredicate =
          cb.equal(root.get(DocumentationUnitDTO_.inboxStatus), parameters.inboxStatus().get());
      predicates.add(inboxStatusPredicate);
    }
    return predicates;
  }

  private List<Predicate> getDuplicateWarningPredicates(
      SearchParameters parameters,
      CriteriaQuery<DocumentationUnitListItemDTO> cq,
      HibernateCriteriaBuilder cb,
      Root<DocumentationUnitDTO> root) {
    List<Predicate> predicates = new ArrayList<>();
    if (parameters.withDuplicateWarning()) {
      Predicate matchLeft =
          getDuplicateRelationPredicate(cq, cb, root, DuplicateRelationDTO_.documentationUnit1);
      Predicate matchRight =
          getDuplicateRelationPredicate(cq, cb, root, DuplicateRelationDTO_.documentationUnit2);
      predicates.add(cb.or(matchLeft, matchRight));
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

  private List<Predicate> getFileNumberPredicates(
      SearchParameters parameters,
      CriteriaQuery<DocumentationUnitListItemDTO> cq,
      HibernateCriteriaBuilder cb,
      Root<DocumentationUnitDTO> root) {
    List<Predicate> predicates = new ArrayList<>();
    if (parameters.fileNumber().isPresent() && !parameters.fileNumber().get().trim().isEmpty()) {
      String fileNumberLike = parameters.fileNumber().get().trim().toUpperCase() + "%";
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

  @NotNull
  private List<Order> getOrderCriteria(
      SearchParameters parameters, HibernateCriteriaBuilder cb, Root<DocumentationUnitDTO> root) {
    List<Order> orderCriteria = new ArrayList<>();
    if (parameters.scheduledOnly() || parameters.publicationDate().isPresent()) {
      orderCriteria.add(
          cb.desc(root.get(DocumentationUnitDTO_.scheduledPublicationDateTime))
              .nullPrecedence(NullPrecedence.LAST));
      orderCriteria.add(
          cb.desc(root.get(DocumentationUnitDTO_.lastPublicationDateTime))
              .nullPrecedence(NullPrecedence.LAST));
    }

    orderCriteria.add(
        cb.desc(root.get(DocumentationUnitDTO_.date)).nullPrecedence(NullPrecedence.LAST));
    orderCriteria.add(cb.desc(root.get(DocumentationUnitDTO_.documentNumber)));
    return orderCriteria;
  }
}
