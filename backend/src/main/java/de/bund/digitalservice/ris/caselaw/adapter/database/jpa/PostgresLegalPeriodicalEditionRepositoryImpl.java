package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.LegalPeriodicalEditionTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.PassiveCitationUliTransformer;
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
  private final DatabasePassiveCitationUliRepository passiveCitationRepository; // Neu
  private final DatabaseDocumentationUnitRepository documentationUnitRepository;

  public PostgresLegalPeriodicalEditionRepositoryImpl(
      DatabaseLegalPeriodicalEditionRepository repository,
      DatabaseReferenceRepository referenceRepository,
      DatabasePassiveCitationUliRepository passiveCitationRepository,
      DatabaseDocumentationUnitRepository documentationUnitRepository) {
    this.repository = repository;
    this.referenceRepository = referenceRepository;
    this.passiveCitationRepository = passiveCitationRepository;
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
    var editionDTO = LegalPeriodicalEditionTransformer.transformToDTO(legalPeriodicalEdition);
    AtomicInteger editionRank = new AtomicInteger(0);

    for (Reference reference :
        Optional.ofNullable(legalPeriodicalEdition.references()).orElse(new ArrayList<>())) {
      var documentationUnitOptional =
          documentationUnitRepository.findByDocumentNumber(
              reference.documentationUnit().getDocumentNumber());

      if (documentationUnitOptional.isEmpty()) continue;
      var docUnitDTO = documentationUnitOptional.get();

      if (reference.referenceType().equals(ReferenceType.CASELAW)) {
        ReferenceDTO dto = ReferenceTransformer.transformToDTO(reference);
        dto.setDocumentationUnitRank(calculateCaselawRank(reference, docUnitDTO));
        dto.setDocumentationUnit(docUnitDTO);
        dto.setEditionRank(editionRank.getAndIncrement());
        dto.setEdition(editionDTO);
        referenceRepository.save(dto);
      } else if (reference.referenceType().equals(ReferenceType.LITERATURE)) {
        if (docUnitDTO instanceof DecisionDTO decisionDTO) {
          PassiveCitationUliDTO dto = PassiveCitationUliTransformer.transformToDTO(reference);
          dto.setRank(calculateLiteratureRank(reference, decisionDTO));
          dto.setTarget(decisionDTO);
          dto.setEditionRank(editionRank.getAndIncrement());
          dto.setEdition(editionDTO);
          passiveCitationRepository.save(dto);
        }
      }
    }

    removeDeletedReferences(legalPeriodicalEdition);
    return LegalPeriodicalEditionTransformer.transformToDomain(repository.save(editionDTO));
  }

  /**
   * Keep rank for existing references and set to max rank +1 for new references
   *
   * @param reference the reference to calculate the rank for
   * @param docUnit the documentation unit that the reference will be inserted into
   * @return the appropriate rank for the reference inside the documentation unit
   */
  private Integer calculateCaselawRank(Reference reference, DocumentationUnitDTO docUnit) {
    return docUnit.getCaselawReferences().stream()
        .filter(dto -> dto.getId().equals(reference.id()))
        .findFirst()
        .map(ReferenceDTO::getDocumentationUnitRank)
        .orElseGet(
            () ->
                docUnit.getCaselawReferences().stream()
                        .map(ReferenceDTO::getDocumentationUnitRank)
                        .max(Comparator.naturalOrder())
                        .orElse(0)
                    + 1);
  }

  /**
   * Keep decision's rank for existing literature references and set to max rank +1 for new ones
   *
   * @param reference the reference to calculate the rank for
   * @param decision the decision that the reference will be inserted into
   * @return the appropriate rank for the reference inside the documentation unit
   */
  private Integer calculateLiteratureRank(Reference reference, DecisionDTO decision) {
    return decision.getPassiveUliCitations().stream()
        .filter(dto -> dto.getId().equals(reference.id()))
        .findFirst()
        .map(PassiveCitationUliDTO::getRank)
        .orElseGet(
            () ->
                decision.getPassiveUliCitations().stream()
                        .map(PassiveCitationUliDTO::getRank)
                        .max(Comparator.naturalOrder())
                        .orElse(0)
                    + 1);
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
      // identify deleted references (not in updated edition)
      if (updatedEdition.references().stream()
          .anyMatch(newReference -> newReference.id().equals(reference.getId()))) {
        continue;
      }

      // 1. Rechtsprechungsfundstellen
      Optional<ReferenceDTO> caselawRefOpt = referenceRepository.findById(reference.getId());
      if (caselawRefOpt.isPresent()) {
        ReferenceDTO ref = caselawRefOpt.get();
        documentationUnitRepository
            .findById(ref.getDocumentationUnit().getId())
            .ifPresent(
                docUnit -> {
                  docUnit.getCaselawReferences().remove(ref);
                  removeReferenceFromSource(docUnit, ref.getId());
                  documentationUnitRepository.save(docUnit);
                });
        referenceRepository.delete(ref);

      } else {
        // 2. Literaturfundstellen
        passiveCitationRepository
            .findById(reference.getId())
            .ifPresent(
                ref -> {
                  documentationUnitRepository
                      .findById(ref.getTarget().getId())
                      .ifPresent(
                          docUnit -> {
                            if (docUnit instanceof DecisionDTO decision) {
                              decision.getPassiveUliCitations().remove(ref);
                              removeReferenceFromSource(decision, ref.getId());
                              documentationUnitRepository.save(decision);
                            }
                          });
                  passiveCitationRepository.delete(ref);
                });
      }
    }
  }

  private void removeReferenceFromSource(DocumentationUnitDTO docUnit, UUID refId) {
    if (docUnit.getSource() != null) {
      docUnit
          .getSource()
          .removeIf(s -> s.getReference() != null && refId.equals(s.getReference().getId()));
    }
  }

  @Override
  public void delete(LegalPeriodicalEdition legalPeriodicalEdition) {
    repository.deleteById(legalPeriodicalEdition.id());
  }
}
