package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentTypeTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentTypeCategory;
import de.bund.digitalservice.ris.caselaw.domain.DocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class PostgresDocumentTypeRepositoryImpl implements DocumentTypeRepository {
  private final DatabaseDocumentTypeRepository repository;
  private final DatabaseDocumentCategoryRepository categoryRepository;

  public PostgresDocumentTypeRepositoryImpl(
      DatabaseDocumentTypeRepository repository,
      DatabaseDocumentCategoryRepository categoryRepository) {
    this.repository = repository;
    this.categoryRepository = categoryRepository;
  }

  @Override
  public Optional<DocumentType> findUniqueCaselawBySearchStr(String searchString) {
    return repository
        .findUniqueCaselawBySearchStrAndCategory(
            searchString, categoryRepository.findFirstByLabel("R").getId())
        .map(DocumentTypeTransformer::transformToDomain);
  }

  @Override
  public DocumentType findPendingProceeding() {
    var pendingProceedingCategory = categoryRepository.findFirstByLabel("A");
    return DocumentTypeTransformer.transformToDomain(
        repository.findFirstByAbbreviationAndCategory("Anh", pendingProceedingCategory));
  }

  @Override
  public List<DocumentType> findDocumentTypesBySearchStrAndCategory(
      String searchStr, DocumentTypeCategory category) {
    List<UUID> targetCategoryIds = resolveCategoryIds(category);

    return repository.findBySearchStrAndCategoryId(searchStr, targetCategoryIds).stream()
        .map(DocumentTypeTransformer::transformToDomain)
        .toList();
  }

  @Override
  public List<DocumentType> findAllDocumentTypesByCategory(DocumentTypeCategory category) {
    List<UUID> targetCategoryIds = resolveCategoryIds(category);

    return repository
        .findAllByCategoryIdInOrderByAbbreviationAscLabelAsc(targetCategoryIds)
        .stream()
        .map(DocumentTypeTransformer::transformToDomain)
        .toList();
  }

  public List<UUID> resolveCategoryIds(DocumentTypeCategory category) {
    try {
      return switch (category) {
        case CASELAW -> getCategoryIdsForLabels(List.of("R"));
        case CASELAW_PENDING_PROCEEDING -> getCategoryIdsForLabels(List.of("R", "A"));
        case DEPENDENT_LITERATURE -> getCategoryIdsForLabels(List.of("U"));
      };
    } catch (IllegalStateException e) {
      log.error("Failed to resolve category IDs for category {}: {}", category, e.getMessage());
      return Collections.emptyList();
    }
  }

  private List<UUID> getCategoryIdsForLabels(List<String> labels) {
    return labels.stream()
        .map(
            label ->
                Optional.ofNullable(categoryRepository.findFirstByLabel(label))
                    .map(DocumentCategoryDTO::getId)
                    .orElseThrow(
                        () -> new IllegalStateException("Category '" + label + "' not found.")))
        .toList();
  }
}
