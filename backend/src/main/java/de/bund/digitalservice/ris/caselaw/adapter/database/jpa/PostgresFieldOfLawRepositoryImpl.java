package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.FieldOfLawTransformer;
import de.bund.digitalservice.ris.caselaw.domain.FieldOfLawRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class PostgresFieldOfLawRepositoryImpl implements FieldOfLawRepository {
  private final DatabaseFieldOfLawRepository repository;
  private final DatabaseFieldOfLawNormRepository normRepository;

  public PostgresFieldOfLawRepositoryImpl(
      DatabaseFieldOfLawRepository repository, DatabaseFieldOfLawNormRepository normRepository) {

    this.repository = repository;
    this.normRepository = normRepository;
  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public List<FieldOfLaw> getTopLevelNodes() {
    return repository.findAllByParentIsNullAndNotationOrderByIdentifier().stream()
        .map(PostgresFieldOfLawRepositoryImpl::getWithNormsWithoutChildren)
        .toList();
  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public List<FieldOfLaw> findAllByParentIdentifierOrderByIdentifierAsc(String identifier) {
    return repository.findByIdentifier(identifier).getChildren().stream()
        .map(PostgresFieldOfLawRepositoryImpl::getWithNormsWithoutChildren)
        .toList();
  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
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
  @Transactional(transactionManager = "jpaTransactionManager")
  public FieldOfLaw findParentByChild(FieldOfLaw child) {
    Optional<FieldOfLawDTO> childDTO = repository.findById(child.id());
    return childDTO.map(PostgresFieldOfLawRepositoryImpl::getWithNormsWithoutChildren).orElse(null);
  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public Slice<FieldOfLaw> findAllByOrderByIdentifierAsc(Pageable pageable) {
    return repository
        .findAllByOrderByIdentifierAsc(pageable)
        .map(PostgresFieldOfLawRepositoryImpl::getWithNormsWithoutChildren);
  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public List<FieldOfLaw> findBySearchTerms(String[] searchTerms) {
    if (searchTerms == null || searchTerms.length == 0) {
      return Collections.emptyList();
    }

    List<FieldOfLawDTO> listWithFirstSearchTerm =
        repository.findAllByNotationAndIdentifierContainingIgnoreCaseOrTextContainingIgnoreCase(
            searchTerms[0]);

    if (searchTerms.length == 1) {
      return listWithFirstSearchTerm.stream()
          .map(PostgresFieldOfLawRepositoryImpl::getWithNormsWithoutChildren)
          .toList();
    }

    return listWithFirstSearchTerm.stream()
        .filter(fieldOfLawDTO -> returnTrueIfInTextOrIdentifier(fieldOfLawDTO, searchTerms))
        .map(PostgresFieldOfLawRepositoryImpl::getWithNormsWithoutChildren)
        .toList();
  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public List<FieldOfLaw> findByNormStr(String normStr) {
    List<FieldOfLawNormDTO> list = getNormDTOs(normStr);
    return list.stream()
        .map(FieldOfLawNormDTO::getFieldOfLaw)
        .distinct()
        .map(PostgresFieldOfLawRepositoryImpl::getWithNormsWithoutChildren)
        .toList();
  }

  private List<FieldOfLawNormDTO> getNormDTOs(String normStr) {
    String correctedNormStr = getNormQueryStrings(normStr);

    return normRepository.findByAbbreviationAndSingleNormDescriptionContainingIgnoreCase(
        correctedNormStr);
  }

  private String getNormQueryStrings(String normString) {
    return normString.replaceAll("§(\\w)", "§ $1");
  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public List<FieldOfLaw> findByNormStrAndSearchTerms(String normStr, String[] searchTerms) {
    List<FieldOfLawNormDTO> listWithNormStr = getNormDTOs(normStr);

    return listWithNormStr.stream()
        .map(FieldOfLawNormDTO::getFieldOfLaw)
        .filter(Objects::nonNull)
        .filter(fieldOfLawDTO -> returnTrueIfInTextOrIdentifier(fieldOfLawDTO, searchTerms))
        .distinct()
        .map(PostgresFieldOfLawRepositoryImpl::getWithNormsWithoutChildren)
        .toList();
  }

  public static boolean returnTrueIfInTextOrIdentifier(
      FieldOfLawDTO fieldOfLawDTO, String[] searchTerms) {
    return Arrays.stream(searchTerms)
        .anyMatch(
            searchTerm ->
                StringUtils.containsIgnoreCase(fieldOfLawDTO.getIdentifier(), searchTerm)
                    || StringUtils.containsIgnoreCase(fieldOfLawDTO.getText(), searchTerm));
  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public List<FieldOfLaw> findByIdentifierSearch(String searchStr) {
    return repository
        .findAllByIdentifierStartsWithIgnoreCaseOrderByIdentifier(searchStr, PageRequest.of(0, 30))
        .stream()
        .map(PostgresFieldOfLawRepositoryImpl::getWithNormsWithoutChildren)
        .toList();
  }

  static FieldOfLaw getWithNormsWithoutChildren(FieldOfLawDTO fieldOfLawDTO) {
    return FieldOfLawTransformer.transformToDomain(fieldOfLawDTO, false, true);
  }
}
