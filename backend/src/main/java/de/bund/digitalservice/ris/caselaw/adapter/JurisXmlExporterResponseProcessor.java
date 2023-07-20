package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.Attachment;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatus;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.domain.HttpMailSender;
import de.bund.digitalservice.ris.caselaw.domain.MailStoreFactory;
import de.bund.digitalservice.ris.caselaw.domain.PublicationReport;
import de.bund.digitalservice.ris.caselaw.domain.PublicationReportRepository;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.domain.export.juris.response.MessageWrapper;
import de.bund.digitalservice.ris.domain.export.juris.response.ProcessMessageWrapper;
import de.bund.digitalservice.ris.domain.export.juris.response.StatusImporterException;
import jakarta.mail.Flags.Flag;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Store;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
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
  private final HttpMailSender mailSender;
  private final DocumentUnitStatusService statusService;
  private final PublicationReportRepository reportRepository;
  private final MailStoreFactory storeFactory;
  private final JurisMessageWrapperFactory wrapperFactory;

  public JurisXmlExporterResponseProcessor(
      HttpMailSender mailSender,
      DocumentUnitStatusService statusService,
      MailStoreFactory storeFactory,
      PublicationReportRepository reportRepository,
      JurisMessageWrapperFactory wrapperFactory) {
    this.mailSender = mailSender;
    this.statusService = statusService;
    this.storeFactory = storeFactory;
    this.reportRepository = reportRepository;
    this.wrapperFactory = wrapperFactory;
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
    try {
      Folder inbox = store.getFolder("INBOX");
      inbox.open(Folder.READ_WRITE);

      Map<Boolean, List<MessageWrapper>> partitionedMessages =
          Arrays.stream(inbox.getMessages())
              .map(wrapperFactory::getResponsibleWrapper)
              .flatMap(Optional::stream)
              .collect(Collectors.partitioningBy(MessageWrapper::messageIsActionable));

      List<MessageWrapper> unprocessableMessages = partitionedMessages.get(false);
      moveMessages(unprocessableMessages, inbox, store.getFolder("unprocessable"));

      List<MessageWrapper> processedMessages =
          partitionedMessages.get(true).stream()
              .sorted(
                  Comparator.comparing(wrapper -> wrapper instanceof ProcessMessageWrapper ? 0 : 1))
              .map(this::processMessage)
              .toList();
      moveMessages(processedMessages, inbox, store.getFolder("processed"));
    } catch (MessagingException e) {
      throw new StatusImporterException("Error processing inbox: " + e);
    }
  }

  private MessageWrapper processMessage(MessageWrapper messageWrapper) {
    return Mono.just(messageWrapper)
        .flatMap(this::forwardMessage)
        .flatMap(this::setPublicationStatus)
        .flatMap(this::saveAttachments)
        .doOnSuccess(result -> LOGGER.info("Message processed for: {}", messageWrapper))
        .doOnError(e -> LOGGER.error("Error processing message: ", e))
        .block();
  }

  private Mono<MessageWrapper> saveAttachments(MessageWrapper messageWrapper) {
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

    try {
      List<Attachment> attachments = collectAttachments(messageWrapper);
      String documentNumber = messageWrapper.getDocumentNumber();
      Instant receivedDate = messageWrapper.getReceivedDate();
      return reportRepository
          .saveAll(
              attachments.stream()
                  .filter(attachment -> attachment.fileName().endsWith(".html"))
                  .map(
                      attachment ->
                          PublicationReport.builder()
                              .documentNumber(documentNumber)
                              .receivedDate(receivedDate)
                              .content(policy.sanitize(attachment.fileContent()))
                              .build())
                  .toList())
          .collectList()
          .thenReturn(messageWrapper);
    } catch (MessagingException | IOException e) {
      return Mono.error(new StatusImporterException("Error saving attachments"));
    }
  }

  private List<Attachment> collectAttachments(MessageWrapper messageWrapper)
      throws MessagingException, IOException {
    return messageWrapper.getAttachments().stream()
        .map(
            attachment ->
                Attachment.builder()
                    .fileName(attachment.fileName())
                    .fileContent(attachment.fileContent())
                    .build())
        .toList();
  }

  private Mono<MessageWrapper> setPublicationStatus(MessageWrapper messageWrapper) {
    try {
      return statusService
          .update(
              messageWrapper.getDocumentNumber(),
              DocumentUnitStatus.builder()
                  .status(getPublicationStatus(messageWrapper.isPublished()))
                  .withError(messageWrapper.hasErrors())
                  .build())
          .thenReturn(messageWrapper);
    } catch (MessagingException | IOException e) {
      return Mono.error(new StatusImporterException("Could not update status" + e));
    }
  }

  private Mono<MessageWrapper> forwardMessage(MessageWrapper messageWrapper) {
    try {
      String documentNumber = messageWrapper.getDocumentNumber();
      String subject = messageWrapper.getSubject();
      List<Attachment> attachments = collectAttachments(messageWrapper);

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
                              "report-" + documentNumber)))
          .thenReturn(messageWrapper);
    } catch (MessagingException | IOException e) {
      return Mono.error(new StatusImporterException("Could not forward Message"));
    }
  }

  private void moveMessages(List<MessageWrapper> messageWrappers, Folder from, Folder to) {
    if (messageWrappers.isEmpty()) return;

    try {
      if (!from.isOpen()) from.open(Folder.READ_WRITE);
      List<Message> messages = messageWrappers.stream().map(MessageWrapper::getMessage).toList();
      from.copyMessages(messages.toArray(new Message[0]), to);

      deleteMessages(messages);
      from.expunge();
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

  private PublicationStatus getPublicationStatus(Optional<Boolean> isPublished) {
    return isPublished
        .map(published -> published ? PublicationStatus.PUBLISHED : PublicationStatus.UNPUBLISHED)
        .orElse(PublicationStatus.PUBLISHING);
  }
}
