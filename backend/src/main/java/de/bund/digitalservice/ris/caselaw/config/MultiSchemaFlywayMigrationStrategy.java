package de.bund.digitalservice.ris.caselaw.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.stereotype.Component;

@Component
public class MultiSchemaFlywayMigrationStrategy implements FlywayMigrationStrategy {

  @Override
  public void migrate(Flyway flyway) {
    var dataSource = flyway.getConfiguration().getDataSource();
    var locations = flyway.getConfiguration().getLocations();

    Flyway incrementalMigrationModule =
        Flyway.configure().schemas("caselaw").locations("db").dataSource(dataSource).load();

    Flyway publicModule =
        Flyway.configure().schemas("public").locations(locations).dataSource(dataSource).load();

    incrementalMigrationModule.repair();
    incrementalMigrationModule.migrate();
    publicModule.repair();
    publicModule.migrate();
  }
}
