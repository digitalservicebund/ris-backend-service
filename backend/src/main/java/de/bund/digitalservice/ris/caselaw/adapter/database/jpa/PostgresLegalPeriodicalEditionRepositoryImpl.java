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
  private final DatabaseDependentLiteratureCitationRepository dependentLiteratureCitationRepository;

  public PostgresLegalPeriodicalEditionRepositoryImpl(
      DatabaseLegalPeriodicalEditionRepository repository,
      DatabaseDocumentationUnitRepository documentationUnitRepository,
      DatabaseDependentLiteratureCitationRepository dependentLiteratureCitationRepository) {
    this.repository = repository;
    this.documentationUnitRepository = documentationUnitRepository;
    this.dependentLiteratureCitationRepository = dependentLiteratureCitationRepository;
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
    for (Reference reference :
        Optional.ofNullable(legalPeriodicalEdition.references()).orElse(new ArrayList<>())) {

      var documentationUnitOptional =
          documentationUnitRepository.findByDocumentNumber(
              reference.documentationUnit().getDocumentNumber());
      if (documentationUnitOptional.isEmpty()) {
        continue;
      }
      var referenceDTO = ReferenceTransformer.transformToDTO(reference);
      var documentationUnitDTO = documentationUnitOptional.get();
      if (referenceDTO instanceof CaselawReferenceDTO caselawReferenceDTO) {
        documentationUnitDTO.addCaselawReference(caselawReferenceDTO);
      } else if (referenceDTO instanceof LiteratureReferenceDTO literatureReferenceDTO) {
        documentationUnitDTO.addLiteratureReference(literatureReferenceDTO);
      }
      documentationUnitRepository.save(documentationUnitDTO);
      referenceDTO.setEdition(editionDTO);
      references.add(referenceDTO);
    }

    editionDTO.setReferences(references);

    return LegalPeriodicalEditionTransformer.transformToDomain(repository.save(editionDTO));
  }

  @Override
  public void delete(LegalPeriodicalEdition legalPeriodicalEdition) {
    repository.deleteById(legalPeriodicalEdition.id());
  }
}
