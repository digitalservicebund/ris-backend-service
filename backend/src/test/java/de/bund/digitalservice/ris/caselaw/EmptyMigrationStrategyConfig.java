package de.bund.digitalservice.ris.caselaw;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class EmptyMigrationStrategyConfig {

  @Bean
  @Primary
  public FlywayMigrationStrategy flywayMigrationStrategy() {
    return flyway -> {
      // do nothing, we only use the script with all migrations until the schema ownership is moved
      // to caselaw
    };
  }
}
