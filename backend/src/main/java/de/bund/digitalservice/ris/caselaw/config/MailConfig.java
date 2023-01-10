package de.bund.digitalservice.ris.caselaw.config;

import de.bund.digitalservice.ris.caselaw.adapter.SendInBlueHttpMailSender;
import de.bund.digitalservice.ris.caselaw.domain.HttpMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class MailConfig {
  @Value("${mail.exporter.apiKey:apiKey}")
  private String apiKey;

  @Bean
  @Profile({"production", "staging"})
  public HttpMailSender httpMailSender() {
    return new SendInBlueHttpMailSender(apiKey);
  }

  @Bean
  @Profile({"!production & !staging"})
  public HttpMailSender httpMailSenderMock() {
    return (senderAddress, receiverAddress, subject, content, fileName, documentUnitUuid) -> {
      // mock for sending publish xml via email
    };
  }
}
