package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface EurLexResultRepository {
  Optional<EurLexResultDTO> findTopByOrderByCreatedAtDesc();

  Page<EurLexResultDTO> findAllBySearchParameters(
      Pageable pageable,
      Optional<String> fileNumber,
      Optional<String> celex,
      Optional<String> court,
      Optional<LocalDate> startDate,
      Optional<LocalDate> endDate);

  void saveAll(List<EurLexResultDTO> transformedList);
}
