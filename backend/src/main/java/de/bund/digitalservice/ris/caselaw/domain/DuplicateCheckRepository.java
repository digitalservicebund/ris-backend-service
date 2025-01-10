package de.bund.digitalservice.ris.caselaw.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface DuplicateCheckRepository {

  List<DocumentationUnitIdDuplicateCheck> findDuplicates(
      List<String> allFileNumbers,
      List<LocalDate> allDates,
      List<UUID> allCourtIds,
      List<String> allDeviatingCourts,
      List<String> allEclis,
      List<UUID> allDocTypeIds);
}
