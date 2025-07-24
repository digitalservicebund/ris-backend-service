package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseLanguageCodeRepository extends JpaRepository<LanguageCodeDTO, UUID> {
  List<LanguageCodeDTO> findLanguageCodeDTOByValueContainsIgnoreCase(
      String searchString, Limit limit);

  @Query(
      value =
          "SELECT * FROM incremental_migration.language_codes l ORDER BY UPPER(l.value) LIMIT :limit",
      nativeQuery = true)
  List<LanguageCodeDTO> findAllOrderByValueIgnoreCaseLimit(@Param("limit") int limit);
}
