package de.bund.digitalservice.ris.caselaw.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class RequestLoggingFilterConfig {

  @Bean
  public CommonsRequestLoggingFilter logFilter() {
    CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
    filter.setIncludeQueryString(true);
    filter.setIncludeClientInfo(true);
    filter.setIncludePayload(false);
    filter.setMaxPayloadLength(10000);
    filter.setIncludeHeaders(false);
    return filter;
  }
}
