package de.bund.digitalservice.ris.caselaw.config;

import de.bund.digitalservice.ris.caselaw.domain.CurrentEnv;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvConfig {

  @Value("${neuris.environment:environment}")
  private String environment;

  @Bean
  public CurrentEnv currentEnv() {
    return new CurrentEnv(environment);
  }
}
