package de.bund.digitalservice.ris.config;

import java.io.InputStream;
import javax.mail.internet.MimeMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

@Configuration
public class MailConfig {
  @Bean
  public JavaMailSender javaMailSender() {
    return new JavaMailSender() {
      @Override
      public MimeMessage createMimeMessage() {
        return null;
      }

      @Override
      public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
        return null;
      }

      @Override
      public void send(MimeMessage mimeMessage) throws MailException {}

      @Override
      public void send(MimeMessage... mimeMessages) throws MailException {}

      @Override
      public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException {}

      @Override
      public void send(MimeMessagePreparator... mimeMessagePreparators) throws MailException {}

      @Override
      public void send(SimpleMailMessage simpleMessage) throws MailException {}

      @Override
      public void send(SimpleMailMessage... simpleMessages) throws MailException {}
    };
  }
}
