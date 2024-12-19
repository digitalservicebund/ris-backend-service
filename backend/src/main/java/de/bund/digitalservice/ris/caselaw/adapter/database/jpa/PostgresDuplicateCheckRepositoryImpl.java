package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationUnitTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateCheckRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Repository
public class PostgresDuplicateCheckRepositoryImpl implements DuplicateCheckRepository {

  private final DatabaseDuplicateCheckRepository repository;

  public PostgresDuplicateCheckRepositoryImpl(DatabaseDuplicateCheckRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional
  public List<DocumentationUnit> findDuplicates(
      List<String> allFileNumbers,
      List<LocalDate> allDates,
      List<UUID> allCourtIds,
      List<String> allDeviatingCourts,
      List<String> allEclis,
      UUID documentTypeId) {
    long startTime = System.currentTimeMillis();

    List<DocumentationUnitDTO> result =
        repository.findDuplicates(
            allFileNumbers, allDates, allCourtIds, allDeviatingCourts, allEclis, documentTypeId);

    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;
    log.info("Query executed in: " + duration + " ms");

    return result.stream()
        .filter(Objects::nonNull)
        .distinct()
        .map(DocumentationUnitTransformer::transformToDomain)
        .toList();
  }
}
