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
  private final DatabaseDocumentationUnitRepository documentUnitRepository;

  public PostgresHandoverRepositoryImpl(
      DatabaseXmlHandoverMailRepository repository,
      DatabaseDocumentationUnitRepository documentUnitRepository) {

    this.repository = repository;
    this.documentUnitRepository = documentUnitRepository;
  }

  /**
   * Saves a handover event to the database.
   *
   * @param handoverMail the handover event to save
   * @return the saved handover event
   */
  @Override
  public HandoverMail save(HandoverMail handoverMail) {
    DocumentationUnitDTO documentUnitDTO =
        documentUnitRepository.findById(handoverMail.documentUnitUuid()).orElseThrow();

    HandoverMailDTO handoverMailDTO =
        HandoverMailTransformer.transformToDTO(handoverMail, documentUnitDTO.getId());
    handoverMailDTO = repository.save(handoverMailDTO);

    return HandoverMailTransformer.transformToDomain(
        handoverMailDTO, handoverMail.documentUnitUuid());
  }

  /**
   * Retrieves all handover events for a documentation unit.
   *
   * @param documentUnitUuid the documentation unit UUID
   * @return the handover events
   */
  @Override
  public List<HandoverMail> getHandoversByDocumentUnitUuid(UUID documentUnitUuid) {
    DocumentationUnitDTO documentUnitDTO =
        documentUnitRepository.findById(documentUnitUuid).orElseThrow();

    List<HandoverMailDTO> handoverMailDTOS =
        repository.findAllByDocumentUnitIdOrderBySentDateDesc(documentUnitDTO.getId());

    return handoverMailDTOS.stream()
        .map(
            handoverMailDTO ->
                HandoverMailTransformer.transformToDomain(handoverMailDTO, documentUnitUuid))
        .toList();
  }

  /**
   * Retrieves the last handover event for a documentation unit.
   *
   * @param documentUnitUuid the documentation unit UUID
   * @return the last handover event
   */
  @Override
  public HandoverMail getLastXmlHandoverMail(UUID documentUnitUuid) {
    DocumentationUnitDTO documentationUnitDTO =
        documentUnitRepository.findById(documentUnitUuid).orElseThrow();

    HandoverMailDTO handoverMailDTO =
        repository.findTopByDocumentUnitIdOrderBySentDateDesc(documentationUnitDTO.getId());

    return HandoverMailTransformer.transformToDomain(handoverMailDTO, documentUnitUuid);
  }
}
