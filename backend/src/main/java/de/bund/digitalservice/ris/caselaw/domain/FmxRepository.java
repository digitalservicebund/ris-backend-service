package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface FmxRepository {

  String getFmxAsString(UUID documentationUnitUuid);

  void attachFmxToDocumentationUnit(UUID documentationUnitUuid, String content);
}
