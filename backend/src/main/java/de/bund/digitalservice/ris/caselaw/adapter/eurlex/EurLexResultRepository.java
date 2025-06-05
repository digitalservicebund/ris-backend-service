package de.bund.digitalservice.ris.caselaw.adapter.eurlex;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.EurLexResultDTO;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface EurLexResultRepository {
  Optional<EurLexResultDTO> findTopByOrderByCreatedAtDesc();

  Page<EurLexResultDTO> findAllNewWithUriBySearchParameters(
      Pageable pageable,
      Optional<String> fileNumber,
      Optional<String> celex,
      Optional<String> court,
      Optional<LocalDate> startDate,
      Optional<LocalDate> endDate);

  List<EurLexResultDTO> findAllByCelexNumbers(List<String> celexNumbers);

  List<EurLexResultDTO> deleteAllByCelexNumbers(List<String> celexNumbers);

  Optional<EurLexResultDTO> findByCelexNumber(String celexNumbers);

  void saveAll(List<EurLexResultDTO> transformedList);
}
