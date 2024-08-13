package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.HandoverMailTransformer;
import de.bund.digitalservice.ris.caselaw.domain.HandoverMail;
import de.bund.digitalservice.ris.caselaw.domain.HandoverRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/** Postgres Repository for performed jDV handover operations. */
@Repository
public class PostgresHandoverRepositoryImpl implements HandoverRepository {

  private final DatabaseXmlHandoverMailRepository repository;
  private final DatabaseDocumentationUnitRepository documentationUnitRepository;

  public PostgresHandoverRepositoryImpl(
      DatabaseXmlHandoverMailRepository repository,
      DatabaseDocumentationUnitRepository documentationUnitRepository) {

    this.repository = repository;
    this.documentationUnitRepository = documentationUnitRepository;
  }

  /**
   * Saves a handover event to the database.
   *
   * @param handoverMail the handover event to save
   * @return the saved handover event
   */
  @Override
  public HandoverMail save(HandoverMail handoverMail) {
    DocumentationUnitDTO documentationUnitDTO =
        documentationUnitRepository.findById(handoverMail.documentationUnitId()).orElseThrow();

    HandoverMailDTO handoverMailDTO =
        HandoverMailTransformer.transformToDTO(handoverMail, documentationUnitDTO.getId());
    handoverMailDTO = repository.save(handoverMailDTO);

    return HandoverMailTransformer.transformToDomain(
        handoverMailDTO, handoverMail.documentationUnitId());
  }

  /**
   * Retrieves all handover events for a documentation unit.
   *
   * @param documentationUnitId the documentation unit UUID
   * @return the handover events
   */
  @Override
  public List<HandoverMail> getHandoversByDocumentationUnitId(UUID documentationUnitId) {
    DocumentationUnitDTO documentationUnitDTO =
        documentationUnitRepository.findById(documentationUnitId).orElseThrow();

    List<HandoverMailDTO> handoverMailDTOS =
        repository.findAllByDocumentationUnitIdOrderBySentDateDesc(documentationUnitDTO.getId());

    return handoverMailDTOS.stream()
        .map(
            handoverMailDTO ->
                HandoverMailTransformer.transformToDomain(handoverMailDTO, documentationUnitId))
        .toList();
  }

  /**
   * Retrieves the last handover event for a documentation unit.
   *
   * @param documentationUnitId the documentation unit UUID
   * @return the last handover event
   */
  @Override
  public HandoverMail getLastXmlHandoverMail(UUID documentationUnitId) {
    DocumentationUnitDTO documentationUnitDTO =
        documentationUnitRepository.findById(documentationUnitId).orElseThrow();

    HandoverMailDTO handoverMailDTO =
        repository.findTopByDocumentationUnitIdOrderBySentDateDesc(documentationUnitDTO.getId());

    return HandoverMailTransformer.transformToDomain(handoverMailDTO, documentationUnitId);
  }
}
