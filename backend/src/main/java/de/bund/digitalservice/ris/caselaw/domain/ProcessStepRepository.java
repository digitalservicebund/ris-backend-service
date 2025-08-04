package de.bund.digitalservice.ris.caselaw.domain;

import java.util.Optional;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ProcessStepRepository {

  Optional<ProcessStep> findByName(String name);
}
