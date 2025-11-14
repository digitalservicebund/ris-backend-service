package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseCollectiveAgreementIndustryRepository
    extends JpaRepository<CollectiveAgreementIndustryDTO, UUID> {
  @Query(
      value =
          """
          SELECT collectiveAgreementIndustry FROM CollectiveAgreementIndustryDTO collectiveAgreementIndustry
            WHERE UPPER(collectiveAgreementIndustry.value) LIKE UPPER(CONCAT('%', :searchString, '%'))
            ORDER BY UPPER(collectiveAgreementIndustry.value)
          """)
  List<CollectiveAgreementIndustryDTO> findCollectiveAgreementIndustryDTOByValueContainsIgnoreCase(
      @Param("searchString") String searchString, Limit limit);

  @Query(
      value =
          """
          SELECT collectiveAgreementIndustry FROM CollectiveAgreementIndustryDTO collectiveAgreementIndustry
            ORDER BY UPPER(collectiveAgreementIndustry.value)
            LIMIT :limit
          """)
  List<CollectiveAgreementIndustryDTO> findAllOrderByValueIgnoreCaseLimit(
      @Param("limit") int limit);
}
