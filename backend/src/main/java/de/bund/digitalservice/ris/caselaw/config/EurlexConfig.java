package de.bund.digitalservice.ris.caselaw.config;

import de.bund.digitalservice.ris.caselaw.adapter.eurlex.EurlexRetrievalService;
import de.bund.digitalservice.ris.caselaw.adapter.eurlex.HttpEurlexRetrievalService;
import de.bund.digitalservice.ris.caselaw.adapter.eurlex.MockEurlexRetrievalService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class EurlexConfig {

  @Bean
  @Profile({"production", "uat"})
  public EurlexRetrievalService httpEurlexRetrievalService() {
    return new HttpEurlexRetrievalService();
  }

  @Bean
  @Profile({"!production & !uat"})
  public EurlexRetrievalService mockEurlexRetrievalService() {
    return new MockEurlexRetrievalService();
  }
}
