package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.eurlex.EurLexResultRepository;
import de.bund.digitalservice.ris.caselaw.adapter.eurlex.EurLexResultStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Nulls;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.util.Strings;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresEurLexResultRepositoryImpl implements EurLexResultRepository {
  private final DatabaseEurLexResultRepository repository;
  private final EntityManager entityManager;

  public PostgresEurLexResultRepositoryImpl(
      DatabaseEurLexResultRepository repository, EntityManager entityManager) {
    this.repository = repository;
    this.entityManager = entityManager;
  }

  @Override
  public Page<EurLexResultDTO> findAllNewWithUriBySearchParameters(
      Pageable pageable,
      Optional<String> fileNumber,
      Optional<String> celex,
      Optional<String> court,
      Optional<LocalDate> startDate,
      Optional<LocalDate> endDate) {

    HibernateCriteriaBuilder builder =
        (HibernateCriteriaBuilder) entityManager.getCriteriaBuilder();
    CriteriaQuery<EurLexResultDTO> builderQuery = builder.createQuery(EurLexResultDTO.class);

    Root<EurLexResultDTO> root = builderQuery.from(EurLexResultDTO.class);
    Join<EurLexResultDTO, CourtDTO> courtJoin = root.join("court", JoinType.INNER);

    List<Predicate> predicates =
        generatePredicates(builder, root, courtJoin, fileNumber, celex, court, startDate, endDate);

    if (!predicates.isEmpty()) {
      builderQuery.where(predicates.toArray(new Predicate[0]));
    }

    // Order by updatedAt with nulls last, then by created at and finally by decision date.
    List<Order> dateTimeDescOrder =
        List.of(
            builder.desc(root.get(EurLexResultDTO_.updatedAt)).nullPrecedence(Nulls.LAST),
            builder.desc(root.get(EurLexResultDTO_.createdAt)),
            builder.desc(root.get(EurLexResultDTO_.date)));
    builderQuery.orderBy(dateTimeDescOrder);

    Query query =
        entityManager
            .createQuery(builderQuery)
            .setFirstResult((int) pageable.getOffset())
            .setMaxResults(pageable.getPageSize());

    // total count
    CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
    Root<EurLexResultDTO> countRoot = countQuery.from(EurLexResultDTO.class);
    Join<EurLexResultDTO, CourtDTO> countCourtJoin = countRoot.join("court", JoinType.INNER);
    countQuery.select(builder.count(countRoot));

    List<Predicate> countPredicates =
        generatePredicates(
            builder, countRoot, countCourtJoin, fileNumber, celex, court, startDate, endDate);

    if (!countPredicates.isEmpty()) {
      countQuery.where(countPredicates.toArray(new Predicate[0]));
    }

    Long count = entityManager.createQuery(countQuery).getSingleResult();

    return new PageImpl<>(query.getResultList(), pageable, count);
  }

  @Override
  public List<EurLexResultDTO> findAllByCelexNumbers(List<String> celexNumbers) {
    return repository.findAllByCelexIn(celexNumbers);
  }

  @Override
  public List<EurLexResultDTO> deleteAllByCelexNumbers(List<String> celexNumbers) {
    return repository.deleteAllByCelexIn(celexNumbers);
  }

  @Override
  public Optional<EurLexResultDTO> findByCelexNumber(String celexNumber) {
    return repository.findByCelex(celexNumber);
  }

  @SuppressWarnings("java:S107")
  private List<Predicate> generatePredicates(
      CriteriaBuilder builder,
      Root<EurLexResultDTO> root,
      Join<EurLexResultDTO, CourtDTO> courtJoin,
      Optional<String> fileNumber,
      Optional<String> celex,
      Optional<String> court,
      Optional<LocalDate> startDate,
      Optional<LocalDate> endDate) {

    List<Predicate> predicates = new ArrayList<>();

    predicates.add(builder.isNotNull(root.get("uri")));
    predicates.add(builder.like(root.get("status"), "NEW"));

    if (fileNumber.isPresent() && Strings.isNotBlank(fileNumber.get())) {
      predicates.add(
          builder.like(
              builder.upper(root.get("fileNumber")), fileNumber.get().toUpperCase() + "%"));
    }

    if (celex.isPresent() && Strings.isNotBlank(celex.get())) {
      predicates.add(
          builder.like(builder.upper(root.get("celex")), celex.get().toUpperCase() + "%"));
    }

    if (court.isPresent() && Strings.isNotBlank(court.get())) {
      predicates.add(
          builder.equal(builder.upper(courtJoin.get("type")), court.get().toUpperCase()));
    }

    startDate.ifPresent(
        date -> predicates.add(builder.greaterThanOrEqualTo(root.get("date"), date)));

    endDate.ifPresent(date -> predicates.add(builder.lessThanOrEqualTo(root.get("date"), date)));

    predicates.add(builder.equal(root.get("status"), EurLexResultStatus.NEW));

    return predicates;
  }

  @Override
  public void saveAll(List<EurLexResultDTO> transformedList) {
    repository.saveAll(transformedList);
  }
}
