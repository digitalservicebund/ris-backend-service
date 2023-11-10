package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.Attachment;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatus;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.domain.HttpMailSender;
import de.bund.digitalservice.ris.caselaw.domain.MailStoreFactory;
import de.bund.digitalservice.ris.caselaw.domain.PublicationReport;
import de.bund.digitalservice.ris.caselaw.domain.PublicationReportRepository;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.domain.export.juris.response.ImportMessageWrapper;
import de.bund.digitalservice.ris.domain.export.juris.response.MessageWrapper;
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
import java.util.Optional;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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

      List<MessageWrapper> processedMessages =
          Arrays.stream(inbox.getMessages())
              .map(wrapperFactory::getResponsibleWrapper)
              .flatMap(Optional::stream)
              .sorted(
                  Comparator.comparing(wrapper -> wrapper instanceof ImportMessageWrapper ? 0 : 1))
              .map(this::forwardMessage)
              .flatMap(Optional::stream)
              .map(this::setPublicationStatus)
              .map(this::saveAttachments)
              .toList();
      moveMessages(processedMessages, inbox, store.getFolder("processed"));
    } catch (MessagingException e) {
      throw new StatusImporterException("Error processing inbox: " + e);
    }
  }

  private MessageWrapper saveAttachments(MessageWrapper messageWrapper) {
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
      reportRepository.saveAll(
          attachments.stream()
              .map(
                  attachment ->
                      PublicationReport.builder()
                          .documentNumber(documentNumber)
                          .receivedDate(receivedDate)
                          .content(
                              policy.sanitize(
                                  attachment.fileName().endsWith(".html")
                                      ? attachment.fileContent()
                                      : stringToHTML(attachment.fileContent())))
                          .build())
              .toList());
      return messageWrapper;
    } catch (MessagingException | IOException e) {
      throw new StatusImporterException("Error saving attachments");
    }
  }

  public static String stringToHTML(String input) {
    return "<html>"
        + String.join(
            "",
            Arrays.stream(input.split("\n")).map(s -> "<p>" + s + "</p>").toArray(String[]::new))
        + "</html>";
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

  private MessageWrapper setPublicationStatus(MessageWrapper messageWrapper) {
    try {
      return statusService
          .update(
              messageWrapper.getDocumentNumber(),
              DocumentUnitStatus.builder()
                  .publicationStatus(getPublicationStatus(messageWrapper.isPublished()))
                  .withError(messageWrapper.hasErrors())
                  .build())
          .thenReturn(messageWrapper)
          .block();
    } catch (MessagingException | IOException | NullPointerException e) {
      throw new StatusImporterException("Could not update publicationStatus" + e);
    }
  }

  private Optional<MessageWrapper> forwardMessage(MessageWrapper messageWrapper) {
    try {
      String documentNumber = messageWrapper.getDocumentNumber();
      String subject = messageWrapper.getSubject();
      List<Attachment> attachments = collectAttachments(messageWrapper);

      return Optional.ofNullable(statusService.getLatestIssuerAddress(documentNumber).block())
          .map(
              issuerAddress -> {
                mailSender.sendMail(
                    storeFactory.getUsername(),
                    issuerAddress,
                    "FWD: " + subject,
                    "Anbei weitergeleitet von der jDV:",
                    attachments,
                    "report-" + documentNumber);
                return messageWrapper;
              });

    } catch (MessagingException | IOException e) {
      throw new StatusImporterException("Could not forward Message");
    } catch (NullPointerException ex) {
      LOGGER.error("NPE with messageWrapper: {}", messageWrapper, ex);
      throw new StatusImporterException("Could not forward Message");
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
