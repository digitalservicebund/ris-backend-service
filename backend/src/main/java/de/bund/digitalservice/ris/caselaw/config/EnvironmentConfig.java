package de.bund.digitalservice.ris.caselaw.config;

import de.bund.digitalservice.ris.caselaw.domain.CurrentEnvironment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvironmentConfig {

  @Value("${neuris.environment:environment}")
  private String environment;

  @Bean
  public CurrentEnvironment currentEnvironment() {
    return new CurrentEnvironment(environment);
  }
}
