package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentNumberRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentNumberDTO;
import de.bund.digitalservice.ris.caselaw.domain.DateUtil;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberFormatter;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberFormatterException;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberPatternException;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberRecyclingService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitException;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitExistsException;
import de.bund.digitalservice.ris.caselaw.domain.StringsUtil;
import jakarta.validation.constraints.NotEmpty;
import java.time.Year;
import java.util.Optional;
import org.springframework.stereotype.Service;

/** Service to generate the next available doc unit number based on documentation office */
@Service
public class DatabaseDocumentNumberGeneratorService implements DocumentNumberService {
  private final DatabaseDocumentNumberRepository repository;
  private final DocumentNumberPatternConfig documentNumberPatternConfig;
  private final DatabaseDocumentationUnitRepository databaseDocumentationUnitRepository;

  private final DocumentNumberRecyclingService documentNumberRecyclingService;

  public DatabaseDocumentNumberGeneratorService(
      DatabaseDocumentNumberRepository repository,
      DocumentNumberPatternConfig documentNumberPatternConfig,
      DatabaseDocumentationUnitRepository databaseDocumentationUnitRepository,
      DocumentNumberRecyclingService documentNumberRecyclingService) {
    this.repository = repository;
    this.documentNumberPatternConfig = documentNumberPatternConfig;
    this.databaseDocumentationUnitRepository = databaseDocumentationUnitRepository;
    this.documentNumberRecyclingService = documentNumberRecyclingService;
  }

  /**
   * A recursive function to attempt generating a document office identifier by incrementing the
   * sequence up to a maximum number of tries. If the pattern is incorrect or there are no available
   * document numbers left, the function will terminate with an error.
   *
   * @param documentationOfficeAbbreviation The abbreviation of the documentation office.
   * @param attempts The maximum number of attempts to generate the document number.
   * @return The generated next available document number to use.
   * @throws DocumentNumberPatternException If the pattern for generating document numbers is
   *     invalid.
   * @throws DocumentNumberFormatterException If there is an issue with formatting the document
   *     number.
   */
  @Override
  public String generateDocumentNumber(String documentationOfficeAbbreviation, int attempts)
      throws DocumentNumberPatternException, DocumentNumberFormatterException {
    try {
      return generateDocumentNumber(documentationOfficeAbbreviation);
    } catch (DocumentationUnitExistsException e) {
      if (attempts <= 0) {
        throw new DocumentationUnitException("Could not generate Document number", e);
      }
      return generateDocumentNumber(documentationOfficeAbbreviation, attempts - 1);
    }
  }

  /**
   * Executes the generation of a document number for the provided documentation office.
   *
   * @param documentationOfficeAbbreviation The abbreviation of the documentation office. Must not
   *     be empty.
   * @return The generated next available document number to use.
   * @throws DocumentNumberPatternException If the pattern for generating document numbers is
   *     invalid.
   * @throws DocumentationUnitExistsException If a documentation unit already exists.
   * @throws DocumentNumberFormatterException If there is an issue with formatting the document
   *     number.
   */
  @Override
  public String generateDocumentNumber(@NotEmpty String documentationOfficeAbbreviation)
      throws DocumentNumberPatternException,
          DocumentationUnitExistsException,
          DocumentNumberFormatterException {
    if (StringsUtil.returnTrueIfNullOrBlank(documentationOfficeAbbreviation)) {
      throw new DocumentNumberPatternException(
          "Documentation Office abbreviation can not be empty");
    }

    String pattern =
        documentNumberPatternConfig
            .getDocumentNumberPatterns()
            .getOrDefault(documentationOfficeAbbreviation, null);

    if (pattern == null) {
      throw new DocumentNumberPatternException(
          "Could not find pattern for abbreviation " + documentationOfficeAbbreviation);
    }

    String recycledId = recycle(documentationOfficeAbbreviation).orElse(null);

    if (recycledId != null) return recycledId;

    DocumentNumberDTO documentNumberDTO =
        repository
            .findById(documentationOfficeAbbreviation)
            .orElse(
                DocumentNumberDTO.builder()
                    .documentationOfficeAbbreviation(documentationOfficeAbbreviation)
                    .lastNumber(0)
                    .build());

    String documentNumber =
        DocumentNumberFormatter.builder()
            .sequenceNumber(documentNumberDTO.increaseLastNumber())
            .year(DateUtil.getYear())
            .pattern(pattern)
            .build()
            .generate();

    repository.save(documentNumberDTO);

    assertNotExists(documentNumber);

    return documentNumber;
  }

  /**
   * Validate document number not exists in the database
   *
   * @param documentNumber
   * @throws DocumentationUnitExistsException
   */
  public void assertNotExists(String documentNumber) throws DocumentationUnitExistsException {
    if (databaseDocumentationUnitRepository.findByDocumentNumber(documentNumber).isPresent()) {
      throw new DocumentationUnitExistsException(
          "Document number already exists: " + documentNumber);
    }
  }

  public Optional<String> recycle(String documentationOfficeAbbreviation) {
    var optionalDeletedDocumentationUnitID =
        documentNumberRecyclingService.findDeletedDocumentNumber(
            documentationOfficeAbbreviation, Year.now());

    if (optionalDeletedDocumentationUnitID.isPresent()) {
      var recycledDocumentNumber = optionalDeletedDocumentationUnitID.get();
      documentNumberRecyclingService.delete(recycledDocumentNumber);
      return Optional.of(recycledDocumentNumber);
    }
    return Optional.empty();
  }
}
