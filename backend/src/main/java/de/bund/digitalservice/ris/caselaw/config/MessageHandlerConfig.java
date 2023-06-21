package de.bund.digitalservice.ris.caselaw.config;

import de.bund.digitalservice.ris.domain.export.juris.response.ImportMessageHandler;
import de.bund.digitalservice.ris.domain.export.juris.response.ProcessMessageHandler;
import de.bund.digitalservice.ris.domain.export.juris.response.StagingProcessMessageHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageHandlerConfig {

  @Bean
  public ImportMessageHandler importMessageHandler() {
    return new ImportMessageHandler();
  }

  @Bean
  public ProcessMessageHandler processMessageHandler() {
    return new ProcessMessageHandler();
  }

  @Bean
  public StagingProcessMessageHandler stagingProcessMessageHandler() {
    return new StagingProcessMessageHandler();
  }
}
