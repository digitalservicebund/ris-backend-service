package de.bund.digitalservice.ris.caselaw.adapter;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ImapStoreFactory {
  @Value("${mail.exporter.response.mailbox.protocol:}")
  private String mailboxProtocol;

  @Value("${mail.exporter.response.mailbox.host:}")
  private String mailboxHost;

  @Value("${mail.exporter.response.mailbox.port:}")
  private String mailboxPort;

  @Value("${mail.exporter.response.mailbox.username:}")
  public String mailboxUsername;

  @Value("${mail.exporter.response.mailbox.password:}")
  private String mailboxPassword;

  public Store createStoreSession() throws MessagingException {
    Properties props = new Properties();
    props.put("mail.store.protocol", mailboxProtocol);
    props.put("mail.imaps.host", mailboxHost);
    props.put("mail.imaps.port", mailboxPort);
    Session session = Session.getInstance(props);

    Store store = session.getStore();
    store.connect(mailboxUsername, mailboxPassword);
    return store;
  }
}
