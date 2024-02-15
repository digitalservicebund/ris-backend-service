package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentNumberRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentNumberDTO;
import de.bund.digitalservice.ris.caselaw.domain.DateUtil;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberFormat;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
public class DatabaseDocumentNumberService implements DocumentNumberService {
  private final DatabaseDocumentNumberRepository repository;
  private final Map<String, String> documentNumberPatterns;

  public DatabaseDocumentNumberService(
      DatabaseDocumentNumberRepository repository,
      DocumentNumberPatternConfig documentNumberPatternConfig) {
    this.repository = repository;
    this.documentNumberPatterns = documentNumberPatternConfig.getDocumentNumberPatterns();
    assert (documentNumberPatterns != null);
    assert (repository != null);
  }

  @Override
  @Transactional(transactionManager = "jpaTransactionManager")
  public Mono<String> generateNextDocumentNumber(DocumentationOffice documentationOffice) {
    String abbreviation = documentationOffice.abbreviation();

    if (abbreviation == null || abbreviation.isBlank()) {
      throw new IllegalArgumentException("Documentation Office abbreviation can not be empty");
    }

    DocumentNumberDTO documentNumberDTO =
        repository
            .findById(abbreviation)
            .orElse(
                DocumentNumberDTO.builder()
                    .documentationOfficeAbbreviation(abbreviation)
                    .lastNumber(0)
                    .build());

    var documentNumberFormat =
        DocumentNumberFormat.builder()
            .docNumber(documentNumberDTO.increaseLastNumber())
            .year(DateUtil.getCurrentYear())
            .pattern(documentNumberPatterns.get(abbreviation))
            .build();

    repository.save(documentNumberDTO);

    return Mono.just(documentNumberFormat.toString());
  } /**/
}
