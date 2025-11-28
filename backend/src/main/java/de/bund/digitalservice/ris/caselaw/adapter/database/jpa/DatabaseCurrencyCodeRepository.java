package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseCurrencyCodeRepository extends JpaRepository<CurrencyCodeDTO, UUID> {
  @Query(
      value =
          """
          SELECT currencyCode FROM CurrencyCodeDTO currencyCode
            WHERE UPPER(currencyCode.value) LIKE UPPER(CONCAT('%', :searchString, '%'))
            ORDER BY UPPER(currencyCode.value)
          """)
  List<CurrencyCodeDTO> findCurrencyCodeDTOByValueContainsIgnoreCase(
      @Param("searchString") String searchString, Limit limit);

  @Query(
      value =
          """
          SELECT currencyCode FROM CurrencyCodeDTO currencyCode
            ORDER BY UPPER(currencyCode.value)
            LIMIT :limit
          """)
  List<CurrencyCodeDTO> findAllOrderByValueIgnoreCaseLimit(@Param("limit") int limit);
}
