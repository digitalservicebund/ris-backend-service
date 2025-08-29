package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.CurrentEnvironment;
import de.bund.digitalservice.ris.caselaw.domain.EnvironmentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EnvironmentService {

  CurrentEnvironment currentEnvironment;

  public EnvironmentService(CurrentEnvironment currentEnvironment) {
    this.currentEnvironment = currentEnvironment;
  }

  @Bean
  public EnvironmentResponse getEnvironment() {
    return EnvironmentResponse.builder()
        .environment(currentEnvironment.name())
        .portalUrl(currentEnvironment.portalUrl())
        .build();
  }

  @Bean
  public String getAccountManagementUrl() {
    return currentEnvironment.accountManagementUrl();
  }
}
