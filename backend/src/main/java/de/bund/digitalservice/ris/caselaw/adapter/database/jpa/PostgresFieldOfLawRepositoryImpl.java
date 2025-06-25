package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.FieldOfLawTransformer;
import de.bund.digitalservice.ris.caselaw.domain.FieldOfLawRepository;
import de.bund.digitalservice.ris.caselaw.domain.StringUtils;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class PostgresFieldOfLawRepositoryImpl implements FieldOfLawRepository {
  private static final String NORMS = "norms";
  private final DatabaseFieldOfLawRepository repository;
  private final EntityManager entityManager;

  public PostgresFieldOfLawRepositoryImpl(
      DatabaseFieldOfLawRepository repository, EntityManager entityManager) {

    this.repository = repository;
    this.entityManager = entityManager;
  }

  @Override
  @Transactional
  public List<FieldOfLaw> getTopLevelNodes() {
    return repository.findAllByParentIsNullAndNotationOrderByIdentifier().stream()
        .map(PostgresFieldOfLawRepositoryImpl::getWithNormsWithoutChildren)
        .toList();
  }

  @Override
  @Transactional
  public List<FieldOfLaw> findAllByParentIdentifierOrderByIdentifierAsc(String identifier) {
    return repository.findByIdentifier(identifier).getChildren().stream()
        .map(PostgresFieldOfLawRepositoryImpl::getWithNormsWithoutChildren)
        .toList();
  }

  @Override
  @Transactional
  public FieldOfLaw findTreeByIdentifier(String identifier) {
    FieldOfLawDTO childDTO = repository.findByIdentifier(identifier);
    FieldOfLaw child = FieldOfLawTransformer.transformToDomain(childDTO, false, true);

    FieldOfLawDTO parentDTO;
    FieldOfLaw parent = child;
    while (childDTO.getParent() != null) {
      parentDTO = childDTO.getParent();
      parent = PostgresFieldOfLawRepositoryImpl.getWithNormsWithoutChildren(parentDTO);
      parent = parent.toBuilder().children(List.of(child)).build();

      childDTO = parentDTO;
      child = parent;
    }

    return parent;
  }

  @Override
  @Transactional
  public Slice<FieldOfLaw> findAllByOrderByIdentifierAsc(Pageable pageable) {
    return repository
        .findAllByOrderByIdentifierAsc(pageable)
        .map(PostgresFieldOfLawRepositoryImpl::getWithNormsWithoutChildren);
  }

  @Override
  @Transactional
  public List<FieldOfLaw> findByCombinedCriteria(
      String identifier, String descriptionSearchTerms, String norm) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<FieldOfLawDTO> cq = cb.createQuery(FieldOfLawDTO.class);
    Root<FieldOfLawDTO> fieldOfLawRoot = cq.from(FieldOfLawDTO.class);
    ArrayList<Predicate> predicates = new ArrayList<>();

    Predicate notationPredicate =
        cb.equal(fieldOfLawRoot.get("notation"), cb.literal(Notation.NEW));
    predicates.add(notationPredicate);

    if (descriptionSearchTerms != null) {
      for (String searchTerm : StringUtils.splitSearchTerms(descriptionSearchTerms)) {
        predicates.add(
            cb.like(cb.lower(fieldOfLawRoot.get("text")), "%" + searchTerm.toLowerCase() + "%"));
      }
    }
    if (identifier != null) {
      Predicate identifierPredicate =
          cb.like(fieldOfLawRoot.get("identifier"), identifier.toUpperCase() + "%");
      predicates.add(identifierPredicate);
    }

    if (!StringUtils.isNullOrBlank(norm)) {
      if (StringUtils.isExactQuoted(norm)) {
        String unquoted = norm.substring(1, norm.length() - 1);
        predicates.add(getExactNormPredicate(unquoted, fieldOfLawRoot, cb));
      } else {
        var normSearchs = StringUtils.splitSearchTerms(norm.replace("§", "").trim());
        predicates.addAll(getAllFieldsSearchNormsPredicates(fieldOfLawRoot, cb, normSearchs));
      }
    }

    cq.select(fieldOfLawRoot).where(predicates.toArray(Predicate[]::new));
    cq.orderBy(cb.asc(fieldOfLawRoot.get("identifier")));

    TypedQuery<FieldOfLawDTO> query = entityManager.createQuery(cq);
    List<FieldOfLawDTO> result = query.getResultList();

    return result.stream()
        .filter(Objects::nonNull)
        .distinct()
        .map(PostgresFieldOfLawRepositoryImpl::getWithNormsWithoutChildren)
        .toList();
  }

  private static List<Predicate> getAllFieldsSearchNormsPredicates(
      Root<FieldOfLawDTO> fieldOfLawRoot, CriteriaBuilder cb, String[] normSearchTerms) {

    if (normSearchTerms != null && normSearchTerms.length > 0) {
      List<Predicate> predicates = new ArrayList<>();

      fieldOfLawRoot.fetch(NORMS, JoinType.LEFT);
      for (String searchTerm : normSearchTerms) {
        Predicate normAbbreviationPredicate =
            cb.like(
                cb.lower(fieldOfLawRoot.get(NORMS).get("abbreviation")),
                "%" + searchTerm.toLowerCase() + "%");
        Predicate singleNormPredicate =
            cb.like(
                cb.lower(fieldOfLawRoot.get(NORMS).get("singleNormDescription")),
                "%" + searchTerm.toLowerCase() + "%");
        Predicate combined = cb.or(normAbbreviationPredicate, singleNormPredicate);
        predicates.add(combined);
      }
      return predicates;
    }
    return null;
  }

  private Predicate getExactNormPredicate(
      String normsString, Root<FieldOfLawDTO> fieldOfLawRoot, CriteriaBuilder cb) {
    Join<FieldOfLawDTO, ?> normsJoin = fieldOfLawRoot.join(NORMS, JoinType.LEFT);

    Expression<String> combinedNorm =
        cb.concat(
            cb.concat(cb.lower(normsJoin.get("abbreviation")), " "),
            cb.lower(normsJoin.get("singleNormDescription")));

    return cb.like(combinedNorm, normsString.toLowerCase() + "%");
  }

  @Override
  @Transactional
  public List<FieldOfLaw> findByIdentifier(String searchStr, Pageable pageable) {
    return repository.findAllByIdentifierStartsWithIgnoreCaseOrderByIdentifier(searchStr).stream()
        .map(PostgresFieldOfLawRepositoryImpl::getWithNormsWithoutChildren)
        .toList();
  }

  static FieldOfLaw getWithNormsWithoutChildren(FieldOfLawDTO fieldOfLawDTO) {
    return FieldOfLawTransformer.transformToDomain(fieldOfLawDTO, false, true);
  }
}
