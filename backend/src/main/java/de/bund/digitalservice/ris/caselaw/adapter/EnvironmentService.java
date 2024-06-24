package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.CurrentEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class EnvironmentService {

  CurrentEnvironment currentEnvironment;

  public EnvironmentService(CurrentEnvironment currentEnvironment) {
    this.currentEnvironment = currentEnvironment;
  }

  @Bean
  public Mono<String> getEnvironment() {
    return Mono.just(currentEnvironment.name());
  }
}
