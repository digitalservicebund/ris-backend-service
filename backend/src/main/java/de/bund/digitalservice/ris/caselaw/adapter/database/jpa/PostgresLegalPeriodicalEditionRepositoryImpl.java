package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.LegalPeriodicalEditionTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ReferenceTransformer;
import de.bund.digitalservice.ris.caselaw.domain.LegalPeriodicalEdition;
import de.bund.digitalservice.ris.caselaw.domain.LegalPeriodicalEditionRepository;
import de.bund.digitalservice.ris.caselaw.domain.Reference;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

    List<ReferenceDTO> referenceDTOS = new ArrayList<>();
    if (legalPeriodicalEdition.references() != null) {

      // save references
      for (Reference reference : legalPeriodicalEdition.references()) {
        var docUnit =
            documentationUnitRepository.findByDocumentNumber(
                reference.documentationUnit().getDocumentNumber());
        if (docUnit.isPresent()) {
          var existingReference =
              docUnit.get().getReferences().stream()
                  .filter(referenceDTO -> referenceDTO.getId().equals(reference.id()))
                  .findFirst();
          var newReference = ReferenceTransformer.transformToDTO(reference);
          newReference.setDocumentationUnit(docUnit.get());
          newReference.setRank(docUnit.get().getReferences().size());
          existingReference.ifPresent(referenceDTO -> newReference.setRank(referenceDTO.getRank()));
          referenceDTOS.add(newReference);
        }
      }
    }

    var edition = LegalPeriodicalEditionTransformer.transformToDTO(legalPeriodicalEdition);
    edition.setReferences(referenceDTOS);

    return LegalPeriodicalEditionTransformer.transformToDomain(repository.save(edition));
  }

  @Override
  public void delete(LegalPeriodicalEdition legalPeriodicalEdition) {
    repository.deleteById(legalPeriodicalEdition.id());
  }
}
