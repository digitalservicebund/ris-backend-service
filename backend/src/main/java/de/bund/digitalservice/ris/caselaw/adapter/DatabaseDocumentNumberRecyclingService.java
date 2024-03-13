package de.bund.digitalservice.ris.caselaw.adapter;

import static de.bund.digitalservice.ris.caselaw.domain.DateUtil.getYear;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDeletedDocumentationIdsRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseStatusRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeletedDocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberPatternException;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberRecyclingService;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.StringsUtil;
import java.time.Year;
import java.util.List;
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

  private final DatabaseStatusRepository statusRepository;

  public DatabaseDocumentNumberRecyclingService(
      DatabaseDeletedDocumentationIdsRepository repository,
      DocumentNumberPatternConfig documentNumberPatternConfig,
      DatabaseStatusRepository statusRepository) {
    this.repository = repository;
    this.documentNumberPatternConfig = documentNumberPatternConfig;
    this.statusRepository = statusRepository;
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

      if (StringsUtil.returnTrueIfNullOrBlank(documentationUnitNumber)) {
        throw new DocumentNumberPatternException("Document number is empty");
      }
      if (!documentNumberPatternConfig
          .getDocumentNumberPatterns()
          .containsKey(documentationOfficeAbbreviation))
        throw new DocumentNumberPatternException(
            documentationOfficeAbbreviation + " is not included in pattern");

      if (!documentNumberPatternConfig.hasValidPrefix(documentationUnitNumber))
        throw new DocumentNumberPatternException("prefix is not included in pattern");

      var status = statusRepository.findAllByDocumentationUnitDTO_Id(documentationUnitId);

      var unpublishedStatus =
          getUnpublishedStatus(status)
              .orElseThrow(
                  () -> new DocumentNumberPatternException("Status are empty or published"));

      var deleted =
          DeletedDocumentationUnitDTO.builder()
              .documentNumber(documentationUnitNumber)
              .year(getYear(unpublishedStatus.getCreatedAt()))
              .abbreviation(documentationOfficeAbbreviation)
              .build();

      return Optional.of(repository.save(deleted).getDocumentNumber());

    } catch (Exception e) {
      log.info("Won´t reuse the document number", e);
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

    if (optionalDeletedDocumentationUnitDTO.isEmpty()) return Optional.empty();
    var deletedDocumentationUnitDTO = optionalDeletedDocumentationUnitDTO.get();

    try {

      var deletedDocumentNumber = deletedDocumentationUnitDTO.getDocumentNumber();

      if (StringsUtil.returnTrueIfNullOrBlank(deletedDocumentNumber)) {
        throw new DocumentNumberPatternException("Can't reuse empty document number");
      }

      if (!documentNumberPatternConfig.hasValidPrefix(deletedDocumentNumber)) {
        throw new DocumentNumberPatternException("Prefix is not included in pattern");
      }

      return Optional.of(deletedDocumentNumber);

    } catch (Exception e) {
      repository.deleteById(deletedDocumentationUnitDTO.getDocumentNumber());
      return Optional.empty();
    }
  }

  @Override
  public void delete(String documentNumber) {
    repository.deleteById(documentNumber);
  }

  private Optional<StatusDTO> getUnpublishedStatus(List<StatusDTO> status) {
    if (status != null
        && status.size() == 1
        && (status.get(0).getPublicationStatus().equals(PublicationStatus.UNPUBLISHED))) {
      return Optional.of(status.get(0));
    }
    return Optional.empty();
  }
}
