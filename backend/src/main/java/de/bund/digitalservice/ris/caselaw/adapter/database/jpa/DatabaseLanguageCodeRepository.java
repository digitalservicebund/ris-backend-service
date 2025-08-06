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
  @Query(
      value =
          """
          SELECT languageCode FROM LanguageCodeDTO languageCode
            WHERE UPPER(languageCode.value) LIKE UPPER(CONCAT('%', :searchString, '%'))
            ORDER BY UPPER(languageCode.value)
          """)
  List<LanguageCodeDTO> findLanguageCodeDTOByValueContainsIgnoreCase(
      @Param("searchString") String searchString, Limit limit);

  @Query(
      value =
          """
          SELECT languageCode FROM LanguageCodeDTO languageCode
            ORDER BY UPPER(languageCode.value)
            LIMIT :limit
          """)
  List<LanguageCodeDTO> findAllOrderByValueIgnoreCaseLimit(@Param("limit") int limit);
}
