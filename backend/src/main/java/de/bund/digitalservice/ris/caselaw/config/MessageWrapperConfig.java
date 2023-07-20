package de.bund.digitalservice.ris.caselaw.config;

import de.bund.digitalservice.ris.domain.export.juris.response.ImportMessageWrapper;
import de.bund.digitalservice.ris.domain.export.juris.response.ProcessMessageWrapper;
import de.bund.digitalservice.ris.domain.export.juris.response.StagingProcessMessageWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageWrapperConfig {

  @Bean
  public Class<ImportMessageWrapper> importMessageHandler() {
    return ImportMessageWrapper.class;
  }

  @Bean
  public Class<ProcessMessageWrapper> processMessageHandler() {
    return ProcessMessageWrapper.class;
  }

  @Bean
  public Class<StagingProcessMessageWrapper> stagingProcessMessageHandler() {
    return StagingProcessMessageWrapper.class;
  }
}
