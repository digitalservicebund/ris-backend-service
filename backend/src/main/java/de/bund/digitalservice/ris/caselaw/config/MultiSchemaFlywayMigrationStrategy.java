package de.bund.digitalservice.ris.caselaw.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.stereotype.Component;

@Component
public class MultiSchemaFlywayMigrationStrategy implements FlywayMigrationStrategy {

  @Override
  public void migrate(Flyway flyway) {
    var dataSource = flyway.getConfiguration().getDataSource();
    var locations = flyway.getConfiguration().getLocations();
    Flyway publicModule =
        Flyway.configure().schemas("public").locations(locations).dataSource(dataSource).load();

    Flyway incrementalMigrationModule =
        Flyway.configure()
            .schemas("incremental_migration")
            .locations("db")
            .dataSource(dataSource)
            .load();

    publicModule.repair();
    publicModule.migrate();
    incrementalMigrationModule.repair();
    incrementalMigrationModule.migrate();
  }
}
