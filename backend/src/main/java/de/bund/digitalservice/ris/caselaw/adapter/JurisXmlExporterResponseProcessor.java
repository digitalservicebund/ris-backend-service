package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.domain.HttpMailSender;
import de.bund.digitalservice.ris.domain.export.juris.response.ActionableMessageHandler;
import de.bund.digitalservice.ris.domain.export.juris.response.MessageHandler;
import de.bund.digitalservice.ris.domain.export.juris.response.StatusImporterException;
import jakarta.mail.Flags.Flag;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Store;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  private final ImapStoreFactory storeFactory;

  private static final Logger LOGGER =
      LoggerFactory.getLogger(JurisXmlExporterResponseProcessor.class);

  public JurisXmlExporterResponseProcessor(
      List<MessageHandler> messageHandlers,
      HttpMailSender mailSender,
      DocumentUnitStatusService statusService,
      ImapStoreFactory storeFactory) {
    this.messageHandlers = messageHandlers;
    this.mailSender = mailSender;
    this.statusService = statusService;
    this.storeFactory = storeFactory;
  }

  @Scheduled(fixedDelay = 60000, initialDelay = 60000)
  public void readEmails() {
    try (Store store = storeFactory.createStoreSession()) {
      processInbox(store);
    } catch (MessagingException e) {
      throw new StatusImporterException("Error creating or closing the store session: " + e);
    }
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

      if (!processedMessages.isEmpty())
        moveMessages(processedMessages, inbox, store.getFolder("processed"));
      if (!unprocessableMessages.isEmpty())
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
                              storeFactory.mailboxUsername,
                              issuerAddress,
                              "FWD: " + subject,
                              "Anbei weitergeleitet von der jDV:",
                              fileName,
                              fileContent,
                              "report-" + documentNumber)));
    } catch (MessagingException | IOException e) {
      LOGGER.error("Error forwarding message: " + e);
      return Mono.error(e);
    }
  }

  private void moveMessages(List<Message> messages, Folder from, Folder to) {
    try {
      if (!from.isOpen()) from.open(Folder.READ_WRITE);
      from.copyMessages(messages.toArray(new Message[0]), to);
      deleteMessages(messages);
    } catch (MessagingException e) {
      throw new StatusImporterException("Error moving message: " + e);
    }
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
