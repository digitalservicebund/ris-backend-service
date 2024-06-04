package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.CurrentEnv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class EnvService {

  CurrentEnv currentEnv;

  public EnvService(CurrentEnv env) {
    this.currentEnv = env;
  }

  @Bean
  public Mono<String> getEnv() {
    return Mono.just(currentEnv.name());
  }
}
