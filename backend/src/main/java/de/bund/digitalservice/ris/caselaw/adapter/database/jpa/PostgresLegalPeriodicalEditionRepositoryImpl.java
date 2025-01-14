package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.LegalPeriodicalEditionTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ReferenceTransformer;
import de.bund.digitalservice.ris.caselaw.domain.LegalPeriodicalEdition;
import de.bund.digitalservice.ris.caselaw.domain.LegalPeriodicalEditionRepository;
import de.bund.digitalservice.ris.caselaw.domain.Reference;
import de.bund.digitalservice.ris.caselaw.domain.ReferenceType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class PostgresLegalPeriodicalEditionRepositoryImpl
    implements LegalPeriodicalEditionRepository {
  private final DatabaseLegalPeriodicalEditionRepository repository;
  private final DatabaseDocumentationUnitRepository documentationUnitRepository;

  public PostgresLegalPeriodicalEditionRepositoryImpl(
      DatabaseLegalPeriodicalEditionRepository repository,
      DatabaseDocumentationUnitRepository documentationUnitRepository) {
    this.repository = repository;
    this.documentationUnitRepository = documentationUnitRepository;
  }

  @Transactional(transactionManager = "jpaTransactionManager")
  public Optional<LegalPeriodicalEdition> findById(UUID id) {
    return repository.findById(id).map(LegalPeriodicalEditionTransformer::transformToDomain);
  }

  @Transactional(transactionManager = "jpaTransactionManager")
  public List<LegalPeriodicalEdition> findAllByLegalPeriodicalId(UUID legalPeriodicalId) {
    return repository.findAllByLegalPeriodicalIdOrderByCreatedAtDesc(legalPeriodicalId).stream()
        .map(LegalPeriodicalEditionTransformer::transformToDomain)
        .toList();
  }

  @Transactional(transactionManager = "jpaTransactionManager")
  public LegalPeriodicalEdition save(LegalPeriodicalEdition legalPeriodicalEdition) {

    var references = new ArrayList<ReferenceDTO>();
    var editionDTO = LegalPeriodicalEditionTransformer.transformToDTO(legalPeriodicalEdition);
    AtomicInteger editionRank = new AtomicInteger(0);
    for (Reference reference :
        Optional.ofNullable(legalPeriodicalEdition.references()).orElse(new ArrayList<>())) {

      var documentationUnitOptional =
          documentationUnitRepository.findByDocumentNumber(
              reference.documentationUnit().getDocumentNumber());
      if (documentationUnitOptional.isEmpty()) {
        continue;
      }
      var documentationUnitDTO = documentationUnitOptional.get();
      ReferenceDTO referenceDTO = ReferenceTransformer.transformToDTO(reference);
      referenceDTO.setDocumentationUnitRank(
          calculateDocumentationUnitRankForReference(reference, documentationUnitDTO));
      referenceDTO.setDocumentationUnit(documentationUnitDTO);
      referenceDTO.setEditionRank(editionRank.getAndIncrement());
      referenceDTO.setEdition(editionDTO);
      references.add(referenceDTO);
    }

    editionDTO.setReferences(references);
    return LegalPeriodicalEditionTransformer.transformToDomain(repository.save(editionDTO));
  }

  /**
   * Keep doc unit's rank for existing references and set to max rank +1 for new references
   *
   * @param reference
   * @param docUnit
   * @return
   */
  private Integer calculateDocumentationUnitRankForReference(
      Reference reference, DocumentationUnitDTO docUnit) {
    if (reference.referenceType().equals(ReferenceType.CASELAW)) {
      return docUnit.getCaselawReferences().stream()
          .filter(referenceDTO -> referenceDTO.getId().equals(reference.id()))
          .findFirst()
          .map(ReferenceDTO::getDocumentationUnitRank)
          .orElseGet(
              () ->
                  docUnit.getCaselawReferences().stream()
                          .map(ReferenceDTO::getDocumentationUnitRank)
                          .max(Comparator.naturalOrder())
                          .orElse(0)
                      + 1);
    } else if (reference.referenceType().equals(ReferenceType.LITERATURE)) {
      return docUnit.getLiteratureReferences().stream()
          .filter(referenceDTO -> referenceDTO.getId().equals(reference.id()))
          .findFirst()
          .map(ReferenceDTO::getDocumentationUnitRank)
          .orElseGet(
              () ->
                  docUnit.getLiteratureReferences().stream()
                          .map(ReferenceDTO::getDocumentationUnitRank)
                          .max(Comparator.naturalOrder())
                          .orElse(0)
                      + 1);
    }
    return null;
  }

  @Override
  public void delete(LegalPeriodicalEdition legalPeriodicalEdition) {
    repository.deleteById(legalPeriodicalEdition.id());
  }
}
