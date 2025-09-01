package de.bund.digitalservice.ris.caselaw.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RequestLoggingFilterConfig {

  @Bean
  public CaselawRequestLoggingFilter logFilter() {
    CaselawRequestLoggingFilter filter = new CaselawRequestLoggingFilter();
    filter.setIncludeQueryString(true);
    filter.setIncludeClientInfo(false);
    filter.setIncludePayload(false);
    filter.setMaxPayloadLength(10000);
    filter.setIncludeHeaders(false);
    filter.setBeforeMessagePrefix("Request [");
    return filter;
  }
}
