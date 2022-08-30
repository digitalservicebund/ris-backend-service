package de.bund.digitalservice.ris.config;

import java.io.InputStream;
import java.util.Properties;
import javax.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessagePreparator;

@Configuration
public class MailConfig {
  @Value("${mail.exporter.host:host}")
  private String smtpHost;

  @Value("${mail.exporter.port:587}")
  private Integer port;

  @Value("${mail.exporter.user:test}")
  private String user;

  @Value("${mail.exporter.password:test}")
  private String password;

  @Bean
  @Profile({"production", "staging"})
  public JavaMailSender javaMailSender() {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost(smtpHost);
    mailSender.setPort(port);

    mailSender.setUsername(user);
    mailSender.setPassword(password);

    Properties props = mailSender.getJavaMailProperties();
    props.put("mail.transport.protocol", "smtp");
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.debug", "true");

    return mailSender;
  }

  @Bean
  @Profile({"!production & !staging"})
  public JavaMailSender javaMailSenderMock() {
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
