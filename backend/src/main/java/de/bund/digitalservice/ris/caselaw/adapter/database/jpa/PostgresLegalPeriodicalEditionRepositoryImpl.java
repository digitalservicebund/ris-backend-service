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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
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

    List<ReferenceDTO> referenceDTOS = new ArrayList<>();
    List<DependentLiteratureCitationDTO> dependentLiteratureCitationDTOS = new ArrayList<>();

    AtomicInteger editionRank = new AtomicInteger(1);
    for (Reference reference :
        Optional.ofNullable(legalPeriodicalEdition.references()).orElse(List.of())) {

      var docUnit =
          documentationUnitRepository.findByDocumentNumber(
              reference.documentationUnit().getDocumentNumber());

      if (docUnit.isEmpty()) {
        continue;
      }
      if (reference.referenceType().equals(ReferenceType.CASELAW)) {
        referenceDTOS.add(createReferenceDTO(reference, docUnit.get(), editionRank));
      }
      if (reference.referenceType().equals(ReferenceType.LITERATURE)) {
        dependentLiteratureCitationDTOS.add(
            createLiteratureCitationDTO(reference, docUnit.get(), editionRank));
      }
    }

    var editionDTO = LegalPeriodicalEditionTransformer.transformToDTO(legalPeriodicalEdition);
    editionDTO.setReferences(referenceDTOS);
    editionDTO.setLiteratureCitations(dependentLiteratureCitationDTOS);

    deleteDocUnitLinksForDeletedReferences(legalPeriodicalEdition);

    return LegalPeriodicalEditionTransformer.transformToDomain(repository.save(editionDTO))
        .toBuilder()
        .references(addReferences(editionDTO))
        .build();
  }

  private ArrayList<Reference> addReferences(LegalPeriodicalEditionDTO editionDTO) {
    ArrayList<Reference> references = new ArrayList<>();

    if (editionDTO.getReferences() != null) {
      references.addAll(
          editionDTO.getReferences().entrySet().stream()
              .map(
                  entry ->
                      referenceRepository
                          .findById(entry.getKey())
                          .map(dto -> dto.toBuilder().editionRank(entry.getValue()).build())
                          .orElse(null))
              .map(ReferenceTransformer::transformToDomain)
              .toList());
    }

    if (editionDTO.getLiteratureCitations() != null) {
      references.addAll(
          editionDTO.getLiteratureCitations().entrySet().stream()
              .map(
                  entry ->
                      dependentLiteratureCitationRepository
                          .findById(entry.getKey())
                          .map(dto -> dto.toBuilder().editionRank(entry.getValue()).build())
                          .orElse(null))
              .map(DependentLiteratureTransformer::transformToDomain)
              .toList());
    }
    return references.stream()
        .sorted(Comparator.comparingInt(Reference::rank))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  private void deleteDocUnitLinksForDeletedReferences(LegalPeriodicalEdition updatedEdition) {
    var oldEdition = repository.findById(updatedEdition.id());
    if (oldEdition.isEmpty()) {
      return;
    }
    // Ensure it's removed from DocumentationUnit's references
    for (Map.Entry<UUID, Integer> reference : oldEdition.get().getReferences().entrySet()) {
      // skip all existing and null references
      var referenceDTO = referenceRepository.findById(reference.getKey());
      if (referenceDTO.isEmpty()
          || updatedEdition.references().stream()
              .anyMatch(newReference -> newReference.id().equals(reference.getKey()))) {
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
                    .filter(ref -> ref.getId().equals(reference.getKey()))
                    .isPresent()) {
                  docUnit.getSource().removeFirst();
                }
                documentationUnitRepository.save(docUnit);
              });
    }
  }

  private ReferenceDTO createReferenceDTO(
      Reference reference, DocumentationUnitDTO docUnit, AtomicInteger editionRank) {
    var newReference = ReferenceTransformer.transformToDTO(reference);
    newReference.setDocumentationUnit(docUnit);

    // keep rank for existing references and set to max rank +1 for new references
    newReference.setRank(
        docUnit.getReferences().stream()
            .filter(referenceDTO -> referenceDTO.getId().equals(reference.id()))
            .findFirst()
            .map(ReferenceDTO::getRank)
            .orElseGet(
                () ->
                    docUnit.getReferences().stream()
                            .map(ReferenceDTO::getRank)
                            .max(Comparator.naturalOrder())
                            .orElse(0)
                        + 1));

    return referenceRepository.save(newReference).toBuilder()
        .editionRank(editionRank.getAndIncrement())
        .build();
  }

  private DependentLiteratureCitationDTO createLiteratureCitationDTO(
      Reference reference, DocumentationUnitDTO docUnit, AtomicInteger editionRank) {
    var newReference = DependentLiteratureTransformer.transformToDTO(reference);
    newReference.setDocumentationUnit(docUnit);

    // keep rank for existing references and set to max rank +1 for new references
    newReference.setRank(
        docUnit.getReferences().stream()
            .filter(referenceDTO -> referenceDTO.getId().equals(reference.id()))
            .findFirst()
            .map(ReferenceDTO::getRank)
            .orElseGet(
                () ->
                    docUnit.getDependentLiteratureCitations().stream()
                            .map(DependentLiteratureCitationDTO::getRank)
                            .max(Comparator.naturalOrder())
                            .orElse(0)
                        + 1));
    return dependentLiteratureCitationRepository.save(newReference).toBuilder()
        .editionRank(editionRank.getAndIncrement())
        .build();
  }

  @Override
  public void delete(LegalPeriodicalEdition legalPeriodicalEdition) {
    repository.deleteById(legalPeriodicalEdition.id());
  }
}
