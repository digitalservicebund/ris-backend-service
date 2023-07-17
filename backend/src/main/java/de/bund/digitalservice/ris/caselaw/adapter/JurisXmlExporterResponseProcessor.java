package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.Attachment;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatus;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.domain.HttpMailSender;
import de.bund.digitalservice.ris.caselaw.domain.MailStoreFactory;
import de.bund.digitalservice.ris.caselaw.domain.PublicationReport;
import de.bund.digitalservice.ris.caselaw.domain.PublicationReportRepository;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
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
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class JurisXmlExporterResponseProcessor {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(JurisXmlExporterResponseProcessor.class);
  private final List<MessageHandler> messageHandlers;
  private final HttpMailSender mailSender;
  private final DocumentUnitStatusService statusService;
  private final PublicationReportRepository reportRepository;
  private final MailStoreFactory storeFactory;

  public JurisXmlExporterResponseProcessor(
      List<MessageHandler> messageHandlers,
      HttpMailSender mailSender,
      DocumentUnitStatusService statusService,
      MailStoreFactory storeFactory,
      PublicationReportRepository reportRepository) {
    this.messageHandlers = messageHandlers;
    this.mailSender = mailSender;
    this.statusService = statusService;
    this.storeFactory = storeFactory;
    this.reportRepository = reportRepository;
  }

  @Scheduled(fixedDelay = 60000, initialDelay = 60000)
  public void readEmails() {
    try (Store store = storeFactory.createStore()) {
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
        Optional<MessageHandler> handler = getResponsibleHandler(message);

        if (handler.isEmpty() || !handler.get().messageIsActionable()) {
          unprocessableMessages.add(message);
          continue;
        }

        processMessage(message, (ActionableMessageHandler) handler.get(), processedMessages);
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

  private void processMessage(
      Message message, ActionableMessageHandler handler, List<Message> processedMessages) {
    try {
      String documentNumber = handler.getDocumentNumber(message);
      List<Attachment> attachments = collectAttachments(message, handler);
      Mono.when(
              forwardMessage(documentNumber, message.getSubject(), attachments),
              setPublicationStatus(message, handler, documentNumber),
              saveAttachments(documentNumber, message.getReceivedDate(), attachments))
          .doOnSuccess(result -> processedMessages.add(message))
          .doOnError(e -> LOGGER.error("Error processing message: ", e))
          .onErrorResume(e -> Mono.empty())
          .block();
    } catch (MessagingException | IOException e) {
      throw new StatusImporterException("Error processing message: " + e);
    }
  }

  private Mono<Void> saveAttachments(
      String documentNumber, Date receivedDate, List<Attachment> attachments) {

    PolicyFactory policy =
        new HtmlPolicyBuilder()
            .allowElements(
                "a", "img", "br", "h2", "table", "tbody", "tr", "td", "hr", "p", "strong", "i",
                "font")
            .allowUrlProtocols("https")
            .allowAttributes("src", "align")
            .onElements("img")
            .allowAttributes("width", "align", "hspace", "cellSpacing", "border")
            .onElements("td", "hr", "table")
            .allowAttributes("color")
            .onElements("font")
            .toFactory();

    return reportRepository
        .saveAll(
            attachments.stream()
                .filter(attachment -> attachment.fileName().endsWith(".html"))
                .map(
                    attachment ->
                        PublicationReport.builder()
                            .documentNumber(documentNumber)
                            .receivedDate(receivedDate.toInstant())
                            .content(policy.sanitize(attachment.fileContent()))
                            .build())
                .toList())
        .then();
  }

  private List<Attachment> collectAttachments(Message message, ActionableMessageHandler handler)
      throws MessagingException, IOException {
    return handler.getAttachments(message).stream()
        .map(
            attachment ->
                Attachment.builder()
                    .fileName(attachment.fileName())
                    .fileContent(attachment.fileContent())
                    .build())
        .toList();
  }

  private Mono<Void> setPublicationStatus(
      Message message, ActionableMessageHandler handler, String documentNumber) {
    try {

      return statusService.update(
          documentNumber,
          DocumentUnitStatus.builder()
              .status(getPublicationStatus(handler.isPublished(message)))
              .withError(handler.hasErrors(message))
              .build());
    } catch (MessagingException | IOException e) {
      throw new StatusImporterException("Could not update status" + e);
    }
  }

  private Mono<Void> forwardMessage(
      String documentNumber, String subject, List<Attachment> attachments) {
    return statusService
        .getLatestIssuerAddress(documentNumber)
        .flatMap(
            issuerAddress ->
                Mono.fromRunnable(
                    () ->
                        mailSender.sendMail(
                            storeFactory.getUsername(),
                            issuerAddress,
                            "FWD: " + subject,
                            "Anbei weitergeleitet von der jDV:",
                            attachments,
                            "report-" + documentNumber)));
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

  private Optional<MessageHandler> getResponsibleHandler(Message message) {
    return this.messageHandlers.stream().filter(handler -> handler.canHandle(message)).findFirst();
  }

  private PublicationStatus getPublicationStatus(Optional<Boolean> isPublished) {
    return isPublished
        .map(published -> published ? PublicationStatus.PUBLISHED : PublicationStatus.UNPUBLISHED)
        .orElse(PublicationStatus.PUBLISHING);
  }
}
