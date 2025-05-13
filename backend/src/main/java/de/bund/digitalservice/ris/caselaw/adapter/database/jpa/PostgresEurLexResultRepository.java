package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.eurlex.EurLexResultStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresEurLexResultRepository implements EurLexResultRepository {
  private final DatabaseEurLexResultRepository repository;
  private final EntityManager entityManager;

  public PostgresEurLexResultRepository(
      DatabaseEurLexResultRepository repository, EntityManager entityManager) {
    this.repository = repository;
    this.entityManager = entityManager;
  }

  @Override
  public Optional<EurLexResultDTO> findTopByOrderByCreatedAtDesc() {
    return repository.findTopByOrderByCreatedAtDesc();
  }

  @Override
  public Page<EurLexResultDTO> findAllBySearchParameters(
      Pageable pageable,
      Optional<String> fileNumber,
      Optional<String> celex,
      Optional<String> court,
      Optional<LocalDate> startDate,
      Optional<LocalDate> endDate) {

    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<EurLexResultDTO> builderQuery = builder.createQuery(EurLexResultDTO.class);

    Root<EurLexResultDTO> root = builderQuery.from(EurLexResultDTO.class);
    Join<EurLexResultDTO, CourtDTO> courtJoin = root.join("court", JoinType.INNER);

    List<Predicate> predicates =
        generatePredicates(builder, root, courtJoin, fileNumber, celex, court, startDate, endDate);

    if (!predicates.isEmpty()) {
      builderQuery.where(predicates.toArray(new Predicate[0]));
    }

    builderQuery.orderBy(builder.desc(root.get("createdAt")));

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

    if (fileNumber.isPresent() && Strings.isNotBlank(fileNumber.get())) {
      predicates.add(builder.like(root.get("fileNumber"), fileNumber.get() + "%"));
    }

    if (celex.isPresent() && Strings.isNotBlank(celex.get())) {
      predicates.add(builder.like(root.get("celex"), celex.get() + "%"));
    }

    if (court.isPresent() && Strings.isNotBlank(court.get())) {
      predicates.add(builder.like(courtJoin.get("type"), court.get() + "%"));
    }

    startDate.ifPresent(
        date -> predicates.add(builder.greaterThanOrEqualTo(root.get("createdAt"), date)));

    endDate.ifPresent(
        date -> predicates.add(builder.lessThanOrEqualTo(root.get("createdAt"), date)));

    predicates.add(builder.equal(root.get("status"), EurLexResultStatus.NEW));

    return predicates;
  }

  @Override
  public void saveAll(List<EurLexResultDTO> transformedList) {
    repository.saveAll(transformedList);
  }
}
