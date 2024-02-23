package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentNumberRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentNumberDTO;
import de.bund.digitalservice.ris.caselaw.domain.DateUtil;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberFormatter;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberFormatterException;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberPatternException;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitException;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitExistsException;
import de.bund.digitalservice.ris.caselaw.domain.StringsUtil;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.stereotype.Service;

/** Service to generate the next available doc unit number based on documentation office */
@Service
public class DatabaseDocumentNumberGeneratorService implements DocumentNumberService {
  private final DatabaseDocumentNumberRepository repository;
  private final DocumentNumberPatternConfig documentNumberPatternConfig;
  private final DatabaseDocumentationUnitRepository databaseDocumentationUnitRepository;

  public DatabaseDocumentNumberGeneratorService(
      DatabaseDocumentNumberRepository repository,
      DocumentNumberPatternConfig documentNumberPatternConfig,
      DatabaseDocumentationUnitRepository databaseDocumentationUnitRepository) {
    this.repository = repository;
    this.documentNumberPatternConfig = documentNumberPatternConfig;
    this.databaseDocumentationUnitRepository = databaseDocumentationUnitRepository;
  }

  @Override
  public String execute(String documentationOfficeAbbreviation, int maxTries)
      throws DocumentNumberPatternException, DocumentNumberFormatterException {
    try {
      return execute(documentationOfficeAbbreviation);
    } catch (DocumentationUnitExistsException e) {
      if (maxTries <= 0) {
        throw new DocumentationUnitException("Could not generate Document number", e);
      }
      return execute(documentationOfficeAbbreviation, maxTries - 1);
    }
  }

  @Override
  public String execute(@NotEmpty String docUnitAbbreviation)
      throws DocumentNumberPatternException,
          DocumentationUnitExistsException,
          DocumentNumberFormatterException {
    if (StringsUtil.returnTrueIfNullOrBlank(docUnitAbbreviation)) {
      throw new DocumentNumberPatternException(
          "Documentation Office abbreviation can not be empty");
    }
    String pattern =
        documentNumberPatternConfig
            .getDocumentNumberPatterns()
            .getOrDefault(docUnitAbbreviation, null);

    if (pattern == null) {
      throw new DocumentNumberPatternException(
          "Could not find pattern for abbreviation " + docUnitAbbreviation);
    }

    DocumentNumberDTO documentNumberDTO =
        repository
            .findById(docUnitAbbreviation)
            .orElse(
                DocumentNumberDTO.builder()
                    .documentationOfficeAbbreviation(docUnitAbbreviation)
                    .lastNumber(0)
                    .build());

    String documentNumber =
        DocumentNumberFormatter.builder()
            .docNumber(documentNumberDTO.increaseLastNumber())
            .year(DateUtil.getYear())
            .pattern(pattern)
            .build()
            .generate();

    repository.save(documentNumberDTO);

    assertNotExists(documentNumber);

    return documentNumber;
  }

  public void assertNotExists(String documentNumber) throws DocumentationUnitExistsException {
    if (databaseDocumentationUnitRepository.findByDocumentNumber(documentNumber).isPresent()) {
      throw new DocumentationUnitExistsException(
          "Document number already exists: " + documentNumber);
    }
  }
}
