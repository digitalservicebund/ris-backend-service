package de.bund.digitalservice.ris.caselaw.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface SearchService {
  Page<SearchResult> getSearchResults(
      String page,
      DocumentationOffice documentationOffice,
      Optional<String> fileNumber,
      Optional<String> celex,
      Optional<String> court,
      Optional<LocalDate> startDate,
      Optional<LocalDate> endDate);

  void updateResultStatus(List<String> celexNumbers);

  void requestNewestDecisions();

  void cleanUpTestdata();
}
