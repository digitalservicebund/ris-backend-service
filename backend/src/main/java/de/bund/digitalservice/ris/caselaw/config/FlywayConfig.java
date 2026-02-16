package de.bund.digitalservice.ris.caselaw.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationInitializer;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class FlywayConfig {

  @Value("${database.user:test}")
  private String user;

  @Value("${database.password:test}")
  private String password;

  @Value("${database.host:localhost}")
  private String host;

  @Value("${database.port:5432}")
  private Integer port;

  @Value("${database.database:neuris}")
  private String database;

  @Value("${database.seed:false}")
  private boolean seed;

  @Value("${database.schema:caselaw}")
  private String schema;

  @Bean
  public Flyway flyway() {
    final String url = "jdbc:postgresql://" + host + ":" + port + "/" + database;
    String locationsPath = seed ? "classpath:db-scripts" : "classpath:db-scripts/migration";
    return Flyway.configure()
        .dataSource(url, user, password)
        .baselineOnMigrate(true)
        .baselineVersion("0.0")
        .schemas(schema)
        .locations(locationsPath)
        .load();
  }

  @Bean
  @Primary
  public FlywayMigrationStrategy flywayMigrationStrategy() {
    return new MultiSchemaFlywayMigrationStrategy();
  }

  @Bean
  @ConditionalOnMissingBean
  public FlywayMigrationInitializer flywayInitializer(
      Flyway flyway, ObjectProvider<FlywayMigrationStrategy> migrationStrategy) {
    return new FlywayMigrationInitializer(flyway, migrationStrategy.getIfAvailable());
  }
}
