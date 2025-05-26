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
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.hibernate.query.NullPrecedence;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

/* S1192: TODO: Use hibernate-jpamodelgen for typesafe criteria queries
Once we are type-safe, we do not need string constants anymore */
// S3655: Sonarqube does not understand that Optional.isPresent() is called on params
@SuppressWarnings({"java:S1192", "java:S3655"})
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

  // FIXME: Do I need to left join status explicitly? It implicitly joins the status table (inner
  // join)
  @Override
  public Slice<DocumentationUnitListItemDTO> search(
      SearchParameters parameters, Pageable pageable) {
    HibernateCriteriaBuilder cb = (HibernateCriteriaBuilder) entityManager.getCriteriaBuilder();
    CriteriaQuery<DocumentationUnitListItemDTO> cq =
        cb.createQuery(DocumentationUnitListItemDTO.class);

    Root<DocumentationUnitDTO> root = cq.from(DocumentationUnitDTO.class);
    // TODO: Fetch first batch already in paginated main query?
    root.fetch("managementData", JoinType.LEFT);

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
      // TODO: Find out: Is fetching OneToOne together quicker as with ManyToOne together?
      List<UUID> docUnitIds = resultList.stream().map(DocumentationUnitListItemDTO::getId).toList();
      fetchRelationships(docUnitIds);
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
  private Expression<Date> getDateOnly(HibernateCriteriaBuilder cb, Path<Object> dateTimeColumn) {
    return cb.function("date", Date.class, dateTimeColumn);
  }

  private void fetchRelationships(Collection<UUID> ids) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<DocumentationUnitListItemDTO> cq =
        cb.createQuery(DocumentationUnitListItemDTO.class);
    Root<DocumentationUnitDTO> root = cq.from(DocumentationUnitDTO.class);
    root.fetch("fileNumbers", JoinType.LEFT);
    // No need to fetch deviating file numbers as we do not display them in the list
    root.fetch("court", JoinType.LEFT);
    root.fetch("documentType", JoinType.LEFT);
    root.fetch("status", JoinType.LEFT);
    root.fetch("documentationOffice", JoinType.LEFT);
    root.fetch("managementData", JoinType.LEFT);

    cq.select(root).where(root.get("id").in(ids));

    entityManager.createQuery(cq).getResultList();
  }

  private void fetchSources(Collection<UUID> ids) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<DocumentationUnitListItemDTO> cq =
        cb.createQuery(DocumentationUnitListItemDTO.class);
    Root<DecisionDTO> root = cq.from(DecisionDTO.class);
    root.fetch("source", JoinType.LEFT);
    root.fetch("procedure", JoinType.LEFT);
    root.fetch("creatingDocumentationOffice", JoinType.LEFT);

    cq.select(root).where(root.get("id").in(ids));

    entityManager.createQuery(cq).getResultList();
  }

  private void fetchAttachments(Collection<UUID> ids) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<DocumentationUnitListItemDTO> cq =
        cb.createQuery(DocumentationUnitListItemDTO.class);
    Root<DecisionDTO> root = cq.from(DecisionDTO.class);
    root.fetch("attachments", JoinType.LEFT);

    cq.select(root).where(root.get("id").in(ids));

    entityManager.createQuery(cq).getResultList();
  }

  private List<Predicate> getDocNumberPredicates(
      SearchParameters parameters, HibernateCriteriaBuilder cb, Root<DocumentationUnitDTO> root) {
    List<Predicate> predicates = new ArrayList<>();
    if (parameters.documentNumber().isPresent()
        && !parameters.documentNumber().get().trim().isEmpty()) {
      Predicate documentNumberPredicate =
          cb.like(
              cb.upper(root.get("documentNumber")),
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
              cb.upper(root.get("court").get("type")),
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
              cb.upper(root.get("court").get("location")),
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
                root.get("date"),
                parameters.decisionDate().get(),
                parameters.decisionDateEnd().get());
      } else {
        decisionDatePredicate = cb.equal(root.get("date"), parameters.decisionDate().get());
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
          cb.equal(root.get("documentationOffice"), parameters.documentationOfficeDTO());
      predicates.add(myDocOfficePredicate);
    }
    return predicates;
  }

  private List<Predicate> getScheduledOnlyPredicates(
      SearchParameters parameters, HibernateCriteriaBuilder cb, Root<DocumentationUnitDTO> root) {
    List<Predicate> predicates = new ArrayList<>();
    if (parameters.scheduledOnly()) {
      Predicate scheduledPublicationDatePredicate =
          cb.isNotNull(root.get("scheduledPublicationDateTime"));
      predicates.add(scheduledPublicationDatePredicate);
    }
    return predicates;
  }

  private List<Predicate> getErrorPredicates(
      SearchParameters parameters, HibernateCriteriaBuilder cb, Root<DocumentationUnitDTO> root) {
    List<Predicate> predicates = new ArrayList<>();
    if (parameters.withError()) {
      Predicate errorStatusPredicate = cb.equal(root.get("status").get("withError"), true);
      predicates.add(errorStatusPredicate);
      if (!parameters.myDocOfficeOnly()) {
        Predicate myDocOfficePredicate =
            cb.equal(root.get("documentationOffice"), parameters.documentationOfficeDTO());
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
              root.get("status").get("publicationStatus"), parameters.publicationStatus().get());
      predicates.add(publicationStatusPredicate);
      if (!parameters.myDocOfficeOnly()
          && !PublicStatusSet.contains(parameters.publicationStatus().get())) {
        // If user selects a non-public status, we will only show documents from the user's doc
        // office
        Predicate myDocOfficePredicate =
            cb.equal(root.get("documentationOffice"), parameters.documentationOfficeDTO());
        predicates.add(myDocOfficePredicate);
      }
    } else if (!parameters.myDocOfficeOnly()) {
      // User may only see published documents from other doc offices
      Predicate publicationStatusPredicate =
          cb.in(root.get("status").get("publicationStatus"), PublicStatusSet);
      Predicate myDocOfficePredicate =
          cb.equal(root.get("documentationOffice"), parameters.documentationOfficeDTO());
      predicates.add(cb.or(publicationStatusPredicate, myDocOfficePredicate));
    }
    return predicates;
  }

  private List<Predicate> getPublicationDatePredicates(
      SearchParameters parameters, HibernateCriteriaBuilder cb, Root<DocumentationUnitDTO> root) {
    List<Predicate> predicates = new ArrayList<>();
    if (parameters.publicationDate().isPresent()) {
      Expression<Date> scheduledPublicationDateTime =
          getDateOnly(cb, root.get("scheduledPublicationDateTime"));
      Predicate scheduledDatePredicate =
          cb.equal(scheduledPublicationDateTime, parameters.publicationDate().get());

      Expression<Date> lastPublicationDateTime =
          getDateOnly(cb, root.get("lastPublicationDateTime"));
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
          cb.equal(root.get("inboxStatus"), parameters.inboxStatus().get());
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
      // FIXME: Do two separate exists queries -> much quicker
      Subquery<Integer> subquery = cq.subquery(Integer.class);
      Root<DuplicateRelationDTO> subRoot = subquery.from(DuplicateRelationDTO.class);
      subquery.select(cb.literal(1));
      subquery.where(
          cb.or(
              cb.equal(subRoot.get("documentationUnit1").get("id"), root.get("id")),
              cb.equal(subRoot.get("documentationUnit2").get("id"), root.get("id"))),
          cb.equal(subRoot.get("relationStatus"), DuplicateRelationStatus.PENDING));
      Predicate withDuplicateWarningPredicate = cb.exists(subquery);
      predicates.add(withDuplicateWarningPredicate);
    }
    return predicates;
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
      subqueryFileNumber.select(subRootFileNumber.get("documentationUnit").get("id"));
      subqueryFileNumber.where(cb.like(cb.upper(subRootFileNumber.get("value")), fileNumberLike));

      Subquery<UUID> subqueryDeviatingFileNumber = cq.subquery(UUID.class);
      Root<DeviatingFileNumberDTO> subRootDeviatingFileNumber =
          subqueryDeviatingFileNumber.from(DeviatingFileNumberDTO.class);
      subqueryDeviatingFileNumber.select(
          subRootDeviatingFileNumber.get("documentationUnit").get("id"));
      subqueryDeviatingFileNumber.where(
          cb.like(cb.upper(subRootDeviatingFileNumber.get("value")), fileNumberLike));
      Predicate fileNumberPredicate = root.get("id").in(subqueryFileNumber);
      Predicate deviatingFileNumberPredicate = root.get("id").in(subqueryDeviatingFileNumber);
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
          cb.desc(root.get("scheduledPublicationDateTime")).nullPrecedence(NullPrecedence.LAST));
      orderCriteria.add(
          cb.desc(root.get("lastPublicationDateTime")).nullPrecedence(NullPrecedence.LAST));
    }

    orderCriteria.add(cb.desc(root.get("date")).nullPrecedence(NullPrecedence.LAST));
    orderCriteria.add(cb.desc(root.get("documentNumber")));
    return orderCriteria;
  }
}
