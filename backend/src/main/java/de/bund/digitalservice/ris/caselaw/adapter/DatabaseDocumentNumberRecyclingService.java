package de.bund.digitalservice.ris.caselaw.adapter;

import static de.bund.digitalservice.ris.caselaw.domain.DateUtil.getYear;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDeletedDocumentationIdsRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeletedDocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberRecyclingService;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentNumberPatternException;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import java.time.Year;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Service to keep track off deleted unpublished documentation units that can be recycled */
@Service
@Slf4j
public class DatabaseDocumentNumberRecyclingService implements DocumentNumberRecyclingService {

  private final DatabaseDeletedDocumentationIdsRepository repository;
  private final DocumentNumberPatternConfig documentNumberPatternConfig;

  private final DatabaseDocumentationUnitRepository documentationUnitRepository;

  public DatabaseDocumentNumberRecyclingService(
      DatabaseDeletedDocumentationIdsRepository repository,
      DocumentNumberPatternConfig documentNumberPatternConfig,
      DatabaseDocumentationUnitRepository documentationUnitRepository) {
    this.repository = repository;
    this.documentNumberPatternConfig = documentNumberPatternConfig;
    this.documentationUnitRepository = documentationUnitRepository;
  }

  /**
   * A method to track deleted unpublished document for reuse
   *
   * @param documentationUnitId
   * @param documentationUnitNumber
   * @param documentationOfficeAbbreviation
   * @return
   */
  @Override
  public Optional<String> addForRecycling(
      UUID documentationUnitId,
      String documentationUnitNumber,
      String documentationOfficeAbbreviation) {

    try {

      if (!documentNumberPatternConfig.hasValidPattern(
          documentationOfficeAbbreviation, documentationUnitNumber)) {
        throw new DocumentNumberPatternException("Pattern is invalid");
      }

      var docUnit =
          documentationUnitRepository
              .findById(documentationUnitId)
              .orElseThrow(() -> new DocumentationUnitNotExistsException(documentationUnitId));

      PublicationStatus status = docUnit.getStatus().getPublicationStatus();
      if (docUnit.getStatusHistory().size() != 1
          || !(status.equals(PublicationStatus.UNPUBLISHED)
              || status.equals(PublicationStatus.EXTERNAL_HANDOVER_PENDING))) {
        throw new DocumentNumberPatternException(
            "Status is changed or neither unpublished nor pending");
      }

      var deleted =
          DeletedDocumentationUnitDTO.builder()
              .documentNumber(documentationUnitNumber)
              .year(getYear(docUnit.getStatus().getCreatedAt()))
              .abbreviation(documentationOfficeAbbreviation)
              .build();

      return Optional.of(repository.save(deleted).getDocumentNumber());

    } catch (Exception e) {
      log.warn("Won´t reuse the document number", e);
      return Optional.empty();
    }
  }

  /**
   * Returns a reusable document number
   *
   * @param documentationOfficeAbbreviation
   * @param year
   * @return
   */
  public Optional<String> findDeletedDocumentNumber(
      String documentationOfficeAbbreviation, Year year) {

    var optionalDeletedDocumentationUnitDTO =
        repository.findFirstByAbbreviationAndYear(documentationOfficeAbbreviation, year);

    try {
      if (optionalDeletedDocumentationUnitDTO.isEmpty()) {
        return Optional.empty();
      }

      var deletedDocumentationUnitDTO = optionalDeletedDocumentationUnitDTO.get();

      if (!documentNumberPatternConfig.hasValidPattern(
          documentationOfficeAbbreviation, deletedDocumentationUnitDTO.getDocumentNumber())) {
        throw new DocumentNumberPatternException("Pattern is invalid");
      }
      return Optional.of(deletedDocumentationUnitDTO.getDocumentNumber());

    } catch (Exception e) {
      optionalDeletedDocumentationUnitDTO.ifPresent(
          deletedDocumentationUnitDTO ->
              repository.deleteById(deletedDocumentationUnitDTO.getDocumentNumber()));
      return Optional.empty();
    }
  }

  @Override
  public void delete(String documentNumber) {
    repository.deleteById(documentNumber);
  }
}
