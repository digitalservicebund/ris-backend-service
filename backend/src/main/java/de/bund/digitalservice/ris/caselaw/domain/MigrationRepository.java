package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;
import org.springframework.data.repository.NoRepositoryBean;

/** Domain repository for migration runs */
@NoRepositoryBean
public interface MigrationRepository {

  Migration getLatestMigration(UUID documentUnitUuid);
}
