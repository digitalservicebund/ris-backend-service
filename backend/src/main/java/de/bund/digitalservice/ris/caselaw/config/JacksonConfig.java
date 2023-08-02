package de.bund.digitalservice.ris.caselaw.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.module.kotlin.KotlinFeature;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

public class JacksonConfig {
  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    KotlinModule kotlinModule =
        new KotlinModule.Builder().configure(KotlinFeature.StrictNullChecks, true).build();
    return JsonMapper.builder().addModule(kotlinModule).build();
  }
}
