package de.bund.digitalservice.ris.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PostgresConfig {
  @Value("${database.user:test}")
  private String user;

  @Value("${database.password:test}")
  private String password;
}
