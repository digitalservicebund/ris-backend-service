package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentNumberCounterRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentNumberCounterDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentNumberService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import java.util.Calendar;
import java.util.Map;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class DatabaseDocumentNumberService implements DocumentNumberService {
  private final DatabaseDocumentNumberCounterRepository repository;
  private static final String CASELAW_ABBREVIATION = "RE";

  // TODO delete - will be replaced by configuration in application.yaml
  private static final Map<String, String> DOCUMENT_NUMBER_ABBREVIATIONS =
      Map.ofEntries(
          Map.entry("BGH", "KO"),
          Map.entry("BVerfG", "KV"),
          Map.entry("BAG", "EF"),
          Map.entry("BFH", "ST"),
          Map.entry("BPatG", "MP"),
          Map.entry("BSG", "KS"),
          Map.entry("BVerwG", "LE"),
          Map.entry("OVG_NRW", "MW"),
          Map.entry("BZSt", "FM"),
          Map.entry("DS", "XX"),
          Map.entry("CC-RIS", "XX"));

  public DatabaseDocumentNumberService(DatabaseDocumentNumberCounterRepository repository) {
    this.repository = repository;
  }

  @Override
  public Mono<String> generateNextDocumentNumber(DocumentationOffice documentationOffice) {
    int currentYear = Calendar.getInstance().get(Calendar.YEAR);

    DocumentNumberCounterDTO numberCounterDTO =
        repository
            .findById(1) // findById("bgh")
            .orElse(
                DocumentNumberCounterDTO.builder().currentYear(currentYear).nextNumber(1).build());

    if (numberCounterDTO.getCurrentYear() != currentYear) {
      numberCounterDTO.setCurrentYear(currentYear);
      numberCounterDTO.setNextNumber(1);
    }

    // TODO either string replacement or string format

    // BGH Example
    // parse docoffice specific format --> for BGH KORE7****YYYY
    // replace **** with next available number
    // --> "%s%s%04d%04d"
    // %s -> KO
    // %s -> RE7
    // %04d -> 0001
    // %04d -> 2024

    // BVerfG Example
    // parse docoffice specific format --> for BVerfG KVRE*****YY41
    // replace ***** with next available number
    // --> "%s%s%05d%02d%s"
    // %s -> KV
    // %s -> RE
    // %05d -> 00001
    // %02d -> 24
    // %s -> 41

    // TODO use docnumber counter of doc office

    String result =
        String.format(
            "%s%s%04d%05d",
            DOCUMENT_NUMBER_ABBREVIATIONS.get(documentationOffice.abbreviation()), // XX
            CASELAW_ABBREVIATION, // RE
            currentYear, // 2024
            numberCounterDTO.getNextNumber()); // 00001

    numberCounterDTO.setNextNumber(numberCounterDTO.getNextNumber() + 1);
    repository.save(numberCounterDTO);

    return Mono.just(result);
  }
}
