package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentNumberRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentNumberDTO;
import de.bund.digitalservice.ris.caselaw.domain.DateUtil;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberFormatter;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberFormatterException;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberPatternException;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitExistsException;
import de.bund.digitalservice.ris.caselaw.domain.StringsUtil;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.stereotype.Service;

@Service
public class DatabaseDocumentNumberService implements DocumentNumberService {
  private final DatabaseDocumentNumberRepository repository;
  private final DocumentNumberPatternConfig documentNumberPatternConfig;
  private final DocumentUnitRepository documentUnitRepository;

  public DatabaseDocumentNumberService(
      DatabaseDocumentNumberRepository repository,
      DocumentNumberPatternConfig documentNumberPatternConfig,
      DocumentUnitRepository documentUnitRepository) {
    this.repository = repository;
    this.documentNumberPatternConfig = documentNumberPatternConfig;
    this.documentUnitRepository = documentUnitRepository;
  }

  @Override
  public String generateNextAvailableDocumentNumber(DocumentationOffice documentationOffice)
      throws DocumentNumberPatternException, DocumentNumberFormatterException {
    try {
      return execute(documentationOffice.abbreviation());
    } catch (DocumentationUnitExistsException e) {
      return generateNextAvailableDocumentNumber(documentationOffice);
    }
  }

  public String execute(@NotEmpty String abbreviation)
      throws DocumentNumberPatternException,
          DocumentationUnitExistsException,
          DocumentNumberFormatterException {
    if (StringsUtil.returnTrueIfNullOrBlank(abbreviation)) {
      throw new IllegalArgumentException("Documentation Office abbreviation can not be empty");
    }
    String pattern =
        documentNumberPatternConfig.documentNumberPatterns.getOrDefault(abbreviation, null);

    if (pattern == null) {
      throw new DocumentNumberPatternException(
          "Could not find pattern for abbreviation " + abbreviation);
    }

    DocumentNumberDTO documentNumberDTO =
        repository
            .findById(abbreviation)
            .orElse(
                DocumentNumberDTO.builder()
                    .documentationOfficeAbbreviation(abbreviation)
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
    DocumentUnit documentUnit = documentUnitRepository.findByDocumentNumber(documentNumber).block();
    if (documentUnit != null) {
      throw new DocumentationUnitExistsException(
          "Document Number already exists: " + documentNumber);
    }
  }
}
