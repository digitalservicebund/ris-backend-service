package de.bund.digitalservice.ris.caselaw.domain.court;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface CourtBranchLocationRepository {
  List<String> findAllByCourtId(UUID courtId);
}
