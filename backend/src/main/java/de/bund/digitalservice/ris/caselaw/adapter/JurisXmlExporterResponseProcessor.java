package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.domain.HttpMailSender;
import de.bund.digitalservice.ris.domain.export.juris.response.ActionableMessageHandler;
import de.bund.digitalservice.ris.domain.export.juris.response.ImportMessageHandler;
import de.bund.digitalservice.ris.domain.export.juris.response.MessageHandler;
import de.bund.digitalservice.ris.domain.export.juris.response.ProcessMessageHandler;
import de.bund.digitalservice.ris.domain.export.juris.response.StagingProcessMessageHandler;
import de.bund.digitalservice.ris.domain.export.juris.response.StatusImporterException;
import jakarta.mail.Flags.Flag;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Profile("production")
public class JurisXmlExporterResponseProcessor {

  private final List<MessageHandler> messageHandlers;
  private final HttpMailSender mailSender;
  private final DocumentUnitStatusService statusService;

  @Value("${mail.exporter.response.mailbox.protocol:}")
  public String mailboxProtocol;

  @Value("${mail.exporter.response.mailbox.host:}")
  public String mailboxHost;

  @Value("${mail.exporter.response.mailbox.port:}")
  public String mailboxPort;

  @Value("${mail.exporter.response.mailbox.username:}")
  public String mailboxUsername;

  @Value("${mail.exporter.response.mailbox.password:}")
  public String mailboxPassword;

  private static final Logger LOGGER =
      LoggerFactory.getLogger(JurisXmlExporterResponseProcessor.class);

  public JurisXmlExporterResponseProcessor(
      HttpMailSender mailSender, DocumentUnitStatusService statusService) {
    this.mailSender = mailSender;
    this.statusService = statusService;
    this.messageHandlers =
        List.of(
            new StagingProcessMessageHandler(),
            new ImportMessageHandler(),
            new ProcessMessageHandler());
  }

  @Scheduled(fixedRate = 60000)
  public void readEmails() {
    try (Store store = createStoreSession()) {
      processInbox(store);
    } catch (MessagingException e) {
      throw new StatusImporterException("Error creating or closing the store session: " + e);
    }
  }

  private Store createStoreSession() throws MessagingException {
    Properties props = new Properties();
    props.put("mail.store.protocol", mailboxProtocol);
    props.put("mail.imaps.host", mailboxHost);
    props.put("mail.imaps.port", mailboxPort);
    Session session = Session.getInstance(props);

    Store store = session.getStore();
    store.connect(mailboxUsername, mailboxPassword);
    return store;
  }

  private void processInbox(Store store) {
    List<Message> processedMessages = new ArrayList<>();
    List<Message> unprocessableMessages = new ArrayList<>();

    try {
      Folder inbox = store.getFolder("INBOX");
      inbox.open(Folder.READ_WRITE);

      for (Message message : inbox.getMessages()) {
        for (MessageHandler handler : this.messageHandlers) {
          if (!handler.canHandle(message)) continue;

          if (!handler.messageIsActionable()) {
            unprocessableMessages.add(message);
            continue;
          }

          forwardMessage(message, (ActionableMessageHandler) handler)
              .doOnSuccess(result -> processedMessages.add(message))
              .onErrorResume(e -> Mono.empty())
              .block();
          break;
        }
      }

      moveMessages(processedMessages, inbox, store.getFolder("processed"));
      moveMessages(unprocessableMessages, inbox, store.getFolder("unprocessable"));
      inbox.expunge();
    } catch (MessagingException e) {
      throw new StatusImporterException("Error processing inbox: " + e);
    }
  }

  private Mono<Void> forwardMessage(Message message, ActionableMessageHandler handler) {
    try {
      String documentNumber = handler.getDocumentNumber(message);
      String subject = message.getSubject();
      String fileName = handler.getFileName(message);
      String fileContent = handler.getFileContent(message);

      return statusService
          .getIssuerAddressOfLatestStatus(documentNumber)
          .flatMap(
              issuerAddress ->
                  Mono.fromRunnable(
                      () ->
                          mailSender.sendMail(
                              mailboxUsername,
                              issuerAddress,
                              "FWD: " + subject,
                              "Anbei weitergeleitet von der jDV:",
                              fileName,
                              fileContent,
                              "report-" + documentNumber)));
    } catch (MessagingException | IOException e) {
      throw new StatusImporterException("Error forwarding message: " + e);
    }
  }

  private void moveMessages(List<Message> messages, Folder from, Folder to) {
    try {
      if (!from.isOpen()) from.open(Folder.READ_WRITE);
      from.copyMessages(messages.toArray(new Message[0]), to);
    } catch (MessagingException e) {
      throw new StatusImporterException("Error moving message: " + e);
    }

    deleteMessages(messages);
  }

  private void deleteMessages(List<Message> messages) {
    messages.forEach(
        message -> {
          try {
            message.setFlag(Flag.DELETED, true);
          } catch (MessagingException e) {
            throw new StatusImporterException("Error deleting Message: " + e);
          }
        });
  }
}
