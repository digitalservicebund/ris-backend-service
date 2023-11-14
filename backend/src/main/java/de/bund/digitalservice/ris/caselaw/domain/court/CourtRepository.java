package de.bund.digitalservice.ris.caselaw.domain.court;

import java.util.List;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface CourtRepository {
  List<Court> findBySearchStr(String searchString);

  List<Court> findAllByOrderByTypeAscLocationAsc();
}
