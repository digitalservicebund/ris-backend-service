package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.FieldOfLawTransformer;
import de.bund.digitalservice.ris.caselaw.domain.FieldOfLawRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import java.util.Collections;
import java.util.List;
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
        .map(fieldOfLawDTO -> FieldOfLawTransformer.transformToDomain(fieldOfLawDTO, false, true))
        .toList();
  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public List<FieldOfLaw> findAllByParentIdentifierOrderByIdentifierAsc(String identifier) {
    return repository.findByIdentifier(identifier).getChildren().stream()
        .map(fieldOfLawDTO -> FieldOfLawTransformer.transformToDomain(fieldOfLawDTO, false, true))
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
      parent = FieldOfLawTransformer.transformToDomain(parentDTO, false, true);
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
    return childDTO
        .map(fieldOfLawDTO -> FieldOfLawTransformer.transformToDomain(fieldOfLawDTO, false, true))
        .orElse(null);
  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public Slice<FieldOfLaw> findAllByOrderByIdentifierAsc(Pageable pageable) {
    return repository
        .findAllByOrderByIdentifierAsc(pageable)
        .map(item -> FieldOfLawTransformer.transformToDomain(item, false, true));
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
          .map(item -> FieldOfLawTransformer.transformToDomain(item, false, true))
          .toList();
    }

    return listWithFirstSearchTerm.stream()
        .filter(
            fieldOfLawDTO -> {
              for (int i = 1; i < searchTerms.length; i++) {
                if (StringUtils.containsIgnoreCase(fieldOfLawDTO.getIdentifier(), searchTerms[i])) {
                  return true;
                }

                if (StringUtils.containsIgnoreCase(fieldOfLawDTO.getText(), searchTerms[i])) {
                  return true;
                }
              }
              return false;
            })
        .map(fol -> FieldOfLawTransformer.transformToDomain(fol, false, true))
        .toList();
  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public List<FieldOfLaw> findByNormStr(String normStr) {
    List<FieldOfLawNormDTO> list = getNormDTOs(normStr);
    return list.stream()
        .map(FieldOfLawNormDTO::getFieldOfLaw)
        .distinct()
        .map(item -> FieldOfLawTransformer.transformToDomain(item, false, true))
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
        .filter(
            fieldOfLawNormDTO -> {
              for (String searchTerm : searchTerms) {
                FieldOfLawDTO fieldOfLawDTO = fieldOfLawNormDTO.getFieldOfLaw();
                if (fieldOfLawDTO == null) {
                  return false;
                }

                if (StringUtils.containsIgnoreCase(fieldOfLawDTO.getIdentifier(), searchTerm)) {
                  return true;
                }

                if (StringUtils.containsIgnoreCase(fieldOfLawDTO.getText(), searchTerm)) {
                  return true;
                }
              }

              return false;
            })
        .map(FieldOfLawNormDTO::getFieldOfLaw)
        .distinct()
        .map(item -> FieldOfLawTransformer.transformToDomain(item, false, true))
        .toList();
  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public List<FieldOfLaw> getFirst30OrderByIdentifier() {
    return repository.findAllByOrderByIdentifierAsc(PageRequest.of(0, 30)).stream()
        .map(item -> FieldOfLawTransformer.transformToDomain(item, false, true))
        .toList();
  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public List<FieldOfLaw> findByIdentifierSearch(String searchStr) {
    return repository
        .findAllByIdentifierStartsWithIgnoreCaseOrderByIdentifier(searchStr, PageRequest.of(0, 30))
        .stream()
        .map(item -> FieldOfLawTransformer.transformToDomain(item, false, true))
        .toList();
  }
}
