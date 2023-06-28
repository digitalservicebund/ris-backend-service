package de.bund.digitalservice.ris.caselaw.config;

import de.bund.digitalservice.ris.caselaw.adapter.ImapStoreFactory;
import de.bund.digitalservice.ris.caselaw.adapter.JurisStub;
import de.bund.digitalservice.ris.caselaw.adapter.SendInBlueHttpMailSender;
import de.bund.digitalservice.ris.caselaw.domain.HttpMailSender;
import de.bund.digitalservice.ris.caselaw.domain.MailStoreFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
public class MailConfig {
  @Value("${mail.exporter.response.mailbox.username:username}")
  private String mailboxUsername;

  @Value("${mail.exporter.response.mailbox.password:password}")
  private String mailboxPassword;

  @Value("${mail.exporter.apiKey:apiKey}")
  private String apiKey;

  @Bean
  @Profile({"production", "staging"})
  public HttpMailSender httpMailSender() {
    return new SendInBlueHttpMailSender(apiKey);
  }

  @Bean
  @Profile({"production", "staging"})
  public MailStoreFactory mailStoreFactory() {
    return new ImapStoreFactory();
  }

  @Bean
  @Primary
  @Profile({"!production & !staging"})
  public JurisStub jurisMock() {
    return new JurisStub(mailboxUsername, mailboxPassword);
  }
}
