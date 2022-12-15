package de.bund.digitalservice.ris.caselaw.domain.lookuptable.state;

import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StateRepository extends ReactiveSortingRepository<StateDTO, Long> {}
