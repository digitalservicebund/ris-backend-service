package de.bund.digitalservice.ris.caselaw.domain.lookuptable.court;

import org.springframework.data.repository.NoRepositoryBean;
import reactor.core.publisher.Flux;

@NoRepositoryBean
public interface CourtRepository {
  Flux<Court> findBySearchStr(String searchString);

  Flux<Court> findAllByOrderByCourttypeAscCourtlocationAsc();
}
