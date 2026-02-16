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
    var schemas = flyway.getConfiguration().getSchemas();

    var schemaLocationForImportedCaselawMigrationSchema = "db";

    /*
     * When importing a dependent implementation
     * implementation("de.bund.digitalservice:neuris-caselaw-migration-schema:0.0.???")
     * ??? - marks the latest schema version.
     * Then we need to reference the location of the schema in that repo,
     * and there it is in the "db" location.
     */
    Flyway incrementalMigrationModule =
        Flyway.configure()
            .schemas(schemas)
            .locations(schemaLocationForImportedCaselawMigrationSchema)
            .dataSource(dataSource)
            .load();

    Flyway publicModule =
        Flyway.configure().schemas(schemas).locations(locations).dataSource(dataSource).load();

    incrementalMigrationModule.repair();
    incrementalMigrationModule.migrate();
    publicModule.repair();
    publicModule.migrate();
  }
}
