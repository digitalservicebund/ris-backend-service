package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.HandoverMailTransformer;
import de.bund.digitalservice.ris.caselaw.domain.HandoverEntityType;
import de.bund.digitalservice.ris.caselaw.domain.HandoverMail;
import de.bund.digitalservice.ris.caselaw.domain.HandoverRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Postgres Repository for performed jDV handover operations. */
@Repository
public class PostgresHandoverRepositoryImpl implements HandoverRepository {

  private final DatabaseXmlHandoverMailRepository repository;
  private final DatabaseDocumentationUnitRepository documentationUnitRepository;

  private final DatabaseLegalPeriodicalEditionRepository editionRepository;

  public PostgresHandoverRepositoryImpl(
      DatabaseXmlHandoverMailRepository repository,
      DatabaseDocumentationUnitRepository documentationUnitRepository,
      DatabaseLegalPeriodicalEditionRepository legalPeriodicalEditionRepository) {

    this.repository = repository;
    this.documentationUnitRepository = documentationUnitRepository;
    this.editionRepository = legalPeriodicalEditionRepository;
  }

  /**
   * Saves a handover event to the database.
   *
   * @param handoverMail the handover event to save
   * @return the saved handover event
   */
  @Override
  @Transactional
  public HandoverMail save(HandoverMail handoverMail) {
    DocumentationUnitDTO documentationUnitDTO =
        documentationUnitRepository.findById(handoverMail.entityId()).orElseThrow();

    HandoverMailDTO handoverMailDTO =
        HandoverMailTransformer.transformToDTO(handoverMail, documentationUnitDTO.getId());
    handoverMailDTO = repository.save(handoverMailDTO);

    return HandoverMailTransformer.transformToDomain(
        handoverMailDTO, handoverMail.entityId(), HandoverEntityType.DOCUMENTATION_UNIT);
  }

  /**
   * Retrieves all handover events for an entity (documentation unit or edition).
   *
   * @param entityId the entity UUID
   * @param entityType the entity type (documentation unit or edition)
   * @return the handover events
   */
  @Override
  @Transactional
  public List<HandoverMail> getHandoversByEntity(UUID entityId, HandoverEntityType entityType) {

    switch (entityType) {
      case DOCUMENTATION_UNIT -> documentationUnitRepository.findById(entityId).orElseThrow();
      case EDITION -> editionRepository.findById(entityId).orElseThrow();
      default -> throw new IllegalArgumentException("Unsupported entity type: " + entityType);
    }

    List<HandoverMailDTO> handoverMailDTOS =
        repository.findAllByEntityIdOrderBySentDateDesc(entityId);

    return handoverMailDTOS.stream()
        .map(
            handoverMailDTO ->
                HandoverMailTransformer.transformToDomain(handoverMailDTO, entityId, entityType))
        .toList();
  }

  /**
   * Retrieves the last handover event for a documentation unit.
   *
   * @param documentationUnitId the documentation unit UUID
   * @return the last handover event
   */
  @Override
  @Transactional
  public HandoverMail getLastXmlHandoverMail(UUID documentationUnitId) {
    DocumentationUnitDTO documentationUnitDTO =
        documentationUnitRepository.findById(documentationUnitId).orElseThrow();

    HandoverMailDTO handoverMailDTO =
        repository.findTopByEntityIdOrderBySentDateDesc(documentationUnitDTO.getId());

    return HandoverMailTransformer.transformToDomain(
        handoverMailDTO, documentationUnitId, HandoverEntityType.DOCUMENTATION_UNIT);
  }
}
