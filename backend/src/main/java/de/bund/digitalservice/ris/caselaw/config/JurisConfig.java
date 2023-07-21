package de.bund.digitalservice.ris.caselaw.config;

import de.bund.digitalservice.ris.caselaw.adapter.ImapStoreFactory;
import de.bund.digitalservice.ris.caselaw.adapter.JurisStub;
import de.bund.digitalservice.ris.caselaw.adapter.SendInBlueHttpMailSender;
import de.bund.digitalservice.ris.caselaw.domain.HttpMailSender;
import de.bund.digitalservice.ris.caselaw.domain.MailStoreFactory;
import de.bund.digitalservice.ris.domain.export.juris.response.ImportMessageWrapper;
import de.bund.digitalservice.ris.domain.export.juris.response.ProcessMessageWrapper;
import de.bund.digitalservice.ris.domain.export.juris.response.StagingProcessMessageWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
public class JurisConfig {
  @Value("${mail.exporter.response.mailbox.username:user@neuris.dev}")
  private String mailboxUsername;

  @Value("${mail.exporter.response.mailbox.password:password}")
  private String mailboxPassword;

  @Value("${mail.exporter.apiKey:apiKey}")
  private String apiKey;

  @Bean
  @Profile({"production"})
  public HttpMailSender httpMailSender() {
    return new SendInBlueHttpMailSender(apiKey);
  }

  @Bean
  @Profile({"production"})
  public MailStoreFactory mailStoreFactory() {
    return new ImapStoreFactory();
  }

  @Bean
  @Primary
  @Profile({"!production"})
  public JurisStub jurisStub() {
    return new JurisStub(mailboxUsername, mailboxPassword);
  }

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
