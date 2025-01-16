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
  private final DatabaseReferenceRepository referenceRepository;
  private final DatabaseDocumentationUnitRepository documentationUnitRepository;

  public PostgresLegalPeriodicalEditionRepositoryImpl(
      DatabaseLegalPeriodicalEditionRepository repository,
      DatabaseReferenceRepository referenceRepository,
      DatabaseDocumentationUnitRepository documentationUnitRepository) {
    this.repository = repository;
    this.referenceRepository = referenceRepository;
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
    removeDeletedReferences(legalPeriodicalEdition);
    return LegalPeriodicalEditionTransformer.transformToDomain(repository.save(editionDTO));
  }

  /**
   * Keep doc unit's rank for existing references and set to max rank +1 for new references
   *
   * @param reference the reference to calculate the rank for
   * @param docUnit the documentation unit that the reference will be inserted into
   * @return the appropriate rank for the reference inside the documentation unit
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

  /**
   * Delete references removed in legal periodical evaluation from their DocumentationUnit to
   * prevent reference orphans and ensure a source relationship is removed if exists. The references
   * to delete are identified by comparing the last saved edition to the updated one.
   *
   * @param updatedEdition the new version of the legal periodical edition
   */
  private void removeDeletedReferences(LegalPeriodicalEdition updatedEdition) {
    var oldEdition = repository.findById(updatedEdition.id());
    if (oldEdition.isEmpty()) {
      return;
    }

    // Ensure references deleted in edition are removed from DocumentationUnit's references
    for (ReferenceDTO reference : oldEdition.get().getReferences()) {
      // identify deleted references (not null and not in updated edition)
      var referenceDTO = referenceRepository.findById(reference.getId());
      if (referenceDTO.isEmpty()
          || updatedEdition.references().stream()
              .anyMatch(newReference -> newReference.id().equals(reference.getId()))) {
        continue;
      }

      // delete all deleted references and possible source reference
      if (referenceDTO.get() instanceof CaselawReferenceDTO caselawReferenceDTO) {
        documentationUnitRepository
            .findById(referenceDTO.get().getDocumentationUnit().getId())
            .ifPresent(
                docUnit -> {
                  docUnit.getCaselawReferences().remove(caselawReferenceDTO);
                  if (docUnit.getSource().stream()
                      .findFirst()
                      .map(SourceDTO::getReference)
                      .filter(ref -> ref.getId().equals(reference.getId()))
                      .isPresent()) {
                    docUnit.getSource().removeFirst();
                  }
                  documentationUnitRepository.save(docUnit);
                });
      } else if (referenceDTO.get() instanceof LiteratureReferenceDTO literatureReferenceDTO) {
        documentationUnitRepository
            .findById(referenceDTO.get().getDocumentationUnit().getId())
            .ifPresent(
                docUnit -> {
                  docUnit.getLiteratureReferences().remove(literatureReferenceDTO);
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
  }

  @Override
  public void delete(LegalPeriodicalEdition legalPeriodicalEdition) {
    repository.deleteById(legalPeriodicalEdition.id());
  }
}
