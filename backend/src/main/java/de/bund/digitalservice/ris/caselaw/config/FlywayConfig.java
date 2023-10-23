package de.bund.digitalservice.ris.caselaw.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

  @Bean
  public Flyway flyway() {
    final String url = "jdbc:postgresql://" + host + ":" + port + "/" + database;
    return Flyway.configure()
        .dataSource(url, user, password)
        .baselineOnMigrate(true)
        .baselineVersion("0.0")
        .load();
  }

  @Bean
  public FlywayMigrationStrategy defaultMigrationStrategy() {
    return flyway -> {
      flyway.repair();
      flyway.migrate();
    };
  }

  @Bean
  @ConditionalOnMissingBean
  public FlywayMigrationInitializer flywayInitializer(
      Flyway flyway, ObjectProvider<FlywayMigrationStrategy> migrationStrategy) {
    return new FlywayMigrationInitializer(flyway, migrationStrategy.getIfAvailable());
  }
}
