package de.bund.digitalservice.ris.caselaw.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
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

  @Bean(initMethod = "migrate")
  public Flyway flyway() {
    final String url = "jdbc:postgresql://" + host + ":" + port + "/" + database;
    return Flyway.configure()
        .dataSource(url, user, password)
        .baselineOnMigrate(true)
        .baselineVersion("0.0")
        .load();
  }
}
