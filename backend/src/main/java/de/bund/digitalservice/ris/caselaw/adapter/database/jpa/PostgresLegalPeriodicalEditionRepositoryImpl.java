package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.LegalPeriodicalEditionTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ReferenceTransformer;
import de.bund.digitalservice.ris.caselaw.domain.LegalPeriodicalEdition;
import de.bund.digitalservice.ris.caselaw.domain.LegalPeriodicalEditionRepository;
import de.bund.digitalservice.ris.caselaw.domain.Reference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
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

    List<ReferenceDTO> referenceDTOS = createReferenceDTOs(legalPeriodicalEdition);
    deleteDocUnitLinksForDeletedReferences(legalPeriodicalEdition);

    var edition = LegalPeriodicalEditionTransformer.transformToDTO(legalPeriodicalEdition);
    edition.setReferences(referenceDTOS); // Add the new references

    return LegalPeriodicalEditionTransformer.transformToDomain(repository.save(edition));
  }

  private void deleteDocUnitLinksForDeletedReferences(LegalPeriodicalEdition updatedEdition) {
    var oldEdition = repository.findById(updatedEdition.id());
    if (oldEdition.isEmpty()) {
      return;
    }
    // Ensure it's removed from DocumentationUnit's references
    for (ReferenceDTO reference : oldEdition.get().getReferences()) {
      // skip all existing references
      if (updatedEdition.references().stream()
          .anyMatch(newReference -> newReference.id().equals(reference.getId()))) {
        continue;
      }
      // delete all deleted references and possible source reference
      documentationUnitRepository
          .findById(reference.getDocumentationUnit().getId())
          .ifPresent(
              docUnit -> {
                docUnit.getReferences().remove(reference);
                if (docUnit.getSource().stream()
                    .findFirst()
                    .map(SourceDTO::getReference)
                    .filter(ref -> ref.getId().equals(reference.getId()))
                    .isPresent()) {
                  docUnit.getSource().removeFirst();
                }
                documentationUnitRepository.save(docUnit);
              });
    }
  }

  @NotNull
  private List<ReferenceDTO> createReferenceDTOs(LegalPeriodicalEdition legalPeriodicalEdition) {
    List<ReferenceDTO> referenceDTOS = new ArrayList<>();
    if (legalPeriodicalEdition.references() == null) {
      return referenceDTOS;
    }
    for (Reference reference : legalPeriodicalEdition.references()) {
      var docUnit =
          documentationUnitRepository.findByDocumentNumber(
              reference.documentationUnit().getDocumentNumber());
      if (docUnit.isEmpty()) {
        // don't add references to non-existing documentation units
        continue;
      }

      var newReference = ReferenceTransformer.transformToDTO(reference);
      newReference.setDocumentationUnit(docUnit.get());

      // keep rank for existing references and set to max rank +1 for new references
      newReference.setRank(
          docUnit.get().getReferences().stream()
              .filter(referenceDTO -> referenceDTO.getId().equals(reference.id()))
              .findFirst()
              .map(ReferenceDTO::getRank)
              .orElseGet(
                  () ->
                      docUnit.get().getReferences().stream()
                              .map(ReferenceDTO::getRank)
                              .max(Comparator.naturalOrder())
                              .orElse(0)
                          + 1));

      referenceDTOS.add(newReference);
    }
    return referenceDTOS;
  }

  @Override
  public void delete(LegalPeriodicalEdition legalPeriodicalEdition) {
    repository.deleteById(legalPeriodicalEdition.id());
  }
}
