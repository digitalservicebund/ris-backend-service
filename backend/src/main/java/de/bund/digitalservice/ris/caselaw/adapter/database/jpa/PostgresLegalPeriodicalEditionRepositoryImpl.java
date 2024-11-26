package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.DependentLiteratureTransformer;
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
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class PostgresLegalPeriodicalEditionRepositoryImpl
    implements LegalPeriodicalEditionRepository {
  private final DatabaseLegalPeriodicalEditionRepository repository;
  private final DatabaseDocumentationUnitRepository documentationUnitRepository;
  private final DatabaseReferenceRepository referenceRepository;
  private final DependentLiteratureCitationRepository dependentLiteratureCitationRepository;

  public PostgresLegalPeriodicalEditionRepositoryImpl(
      DatabaseLegalPeriodicalEditionRepository repository,
      DatabaseDocumentationUnitRepository documentationUnitRepository,
      DatabaseReferenceRepository referenceRepository,
      DependentLiteratureCitationRepository dependentLiteratureCitationRepository) {
    this.repository = repository;
    this.documentationUnitRepository = documentationUnitRepository;
    this.referenceRepository = referenceRepository;
    this.dependentLiteratureCitationRepository = dependentLiteratureCitationRepository;
  }

  @Transactional(transactionManager = "jpaTransactionManager")
  public Optional<LegalPeriodicalEdition> findById(UUID id) {
    return repository
        .findById(id)
        .map(
            edition ->
                LegalPeriodicalEditionTransformer.transformToDomain(edition).toBuilder()
                    .references(addReferences(edition))
                    .build());
  }

  @Transactional(transactionManager = "jpaTransactionManager")
  public List<LegalPeriodicalEdition> findAllByLegalPeriodicalId(UUID legalPeriodicalId) {
    return repository.findAllByLegalPeriodicalIdOrderByCreatedAtDesc(legalPeriodicalId).stream()
        .map(
            edition ->
                LegalPeriodicalEditionTransformer.transformToDomain(edition).toBuilder()
                    .references(addReferences(edition))
                    .build())
        .toList();
  }

  @Transactional(transactionManager = "jpaTransactionManager")
  public LegalPeriodicalEdition save(LegalPeriodicalEdition legalPeriodicalEdition) {

    List<ReferenceDTO> referenceDTOS = createReferenceDTOs(legalPeriodicalEdition);
    List<DependentLiteratureCitationDTO> dependentLiteratureCitationDTOS =
        createLiteratureReferenceDTOs(legalPeriodicalEdition);
    deleteDocUnitLinksForDeletedReferences(legalPeriodicalEdition);

    var editionDTO = LegalPeriodicalEditionTransformer.transformToDTO(legalPeriodicalEdition);
    editionDTO.setReferences(referenceDTOS); // Add the new references
    editionDTO.setLiteratureCitations(
        dependentLiteratureCitationDTOS); // Add the new literature references

    return LegalPeriodicalEditionTransformer.transformToDomain(repository.save(editionDTO))
        .toBuilder()
        .references(addReferences(editionDTO))
        .build();
  }

  private ArrayList<Reference> addReferences(LegalPeriodicalEditionDTO editionDTO) {
    ArrayList<Reference> references = new ArrayList<>();

    if (editionDTO.getReferences() != null) {
      references.addAll(
          editionDTO.getReferences().stream()
              .map(id -> referenceRepository.findById(id).orElse(null))
              .map(ReferenceTransformer::transformToDomain)
              .toList());
    }

    if (editionDTO.getLiteratureCitations() != null) {
      references.addAll(
          editionDTO.getLiteratureCitations().stream()
              .map(id -> dependentLiteratureCitationRepository.findById(id).orElse(null))
              .map(DependentLiteratureTransformer::transformToDomain)
              .toList());
    }
    return references;
  }

  private void deleteDocUnitLinksForDeletedReferences(LegalPeriodicalEdition updatedEdition) {
    var oldEdition = repository.findById(updatedEdition.id());
    if (oldEdition.isEmpty()) {
      return;
    }
    // Ensure it's removed from DocumentationUnit's references
    for (UUID reference : oldEdition.get().getReferences()) {
      // skip all existing references
      if (updatedEdition.references().stream()
          .anyMatch(newReference -> newReference.id().equals(reference))) {
        continue;
      }

      var referenceDTO = referenceRepository.findById(reference);
      if (referenceDTO.isEmpty()) {
        continue;
      }
      // delete all deleted references and possible source reference
      documentationUnitRepository
          .findById(referenceDTO.get().getDocumentationUnit().getId())
          .ifPresent(
              docUnit -> {
                docUnit.getReferences().remove(referenceDTO.get());
                if (docUnit.getSource().stream()
                    .findFirst()
                    .map(SourceDTO::getReference)
                    .filter(ref -> ref.getId().equals(reference))
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

      if (!reference.referenceType().equals(ReferenceType.CASELAW)) {
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

      referenceDTOS.add(referenceRepository.save(newReference));
    }

    return referenceDTOS;
  }

  private List<DependentLiteratureCitationDTO> createLiteratureReferenceDTOs(
      LegalPeriodicalEdition legalPeriodicalEdition) {
    List<DependentLiteratureCitationDTO> dependentLiteratureCitationDTOS = new ArrayList<>();
    if (legalPeriodicalEdition.references() == null) {
      return dependentLiteratureCitationDTOS;
    }
    for (Reference reference : legalPeriodicalEdition.references()) {
      var docUnit =
          documentationUnitRepository.findByDocumentNumber(
              reference.documentationUnit().getDocumentNumber());
      if (docUnit.isEmpty()) {
        // don't add references to non-existing documentation units
        continue;
      }

      if (!reference.referenceType().equals(ReferenceType.LITERATURE)) {
        continue;
      }

      var newReference = DependentLiteratureTransformer.transformToDTO(reference);
      newReference.setDocumentationUnit(docUnit.get());
      // keep rank for existing references and set to max rank +1 for new references
      newReference.setRank(
          docUnit.get().getReferences().stream()
              .filter(referenceDTO -> referenceDTO.getId().equals(reference.id()))
              .findFirst()
              .map(ReferenceDTO::getRank)
              .orElseGet(
                  () ->
                      docUnit.get().getDependentLiteratureCitations().stream()
                              .map(DependentLiteratureCitationDTO::getRank)
                              .max(Comparator.naturalOrder())
                              .orElse(0)
                          + 1));
      dependentLiteratureCitationDTOS.add(dependentLiteratureCitationRepository.save(newReference));
    }
    return dependentLiteratureCitationDTOS;
  }

  @Override
  public void delete(LegalPeriodicalEdition legalPeriodicalEdition) {
    repository.deleteById(legalPeriodicalEdition.id());
  }
}
