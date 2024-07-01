package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.domain.Migration;
import de.bund.digitalservice.ris.caselaw.domain.MigrationRepository;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresMigrationRepositoryImpl implements MigrationRepository {

  private final DatabaseMigrationRepository dbRepository;

  public PostgresMigrationRepositoryImpl(DatabaseMigrationRepository dbRepository) {
    this.dbRepository = dbRepository;
  }

  @Override
  public Migration getLatestMigration(UUID documentUnitUuid) {

    return dbRepository
        .findByDocumentationUnitId(documentUnitUuid)
        .map(
            originalXmlDTO ->
                Migration.builder()
                    .xml(originalXmlDTO.getContent())
                    .migratedDate(originalXmlDTO.getUpdatedAt())
                    .build())
        .orElse(null);
  }
}
