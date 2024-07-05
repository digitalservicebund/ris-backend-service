package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.HttpMailSender;
import de.bund.digitalservice.ris.caselaw.domain.MailAttachment;
import de.bund.digitalservice.ris.caselaw.domain.MailStoreFactory;
import de.bund.digitalservice.ris.caselaw.domain.PublicationReport;
import de.bund.digitalservice.ris.caselaw.domain.PublicationReportRepository;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.XmlPublicationRepository;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
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

  private final DocumentUnitRepository documentUnitRepository;
  private final XmlPublicationRepository xmlPublicationRepository;

  public JurisXmlExporterResponseProcessor(
      HttpMailSender mailSender,
      DocumentUnitStatusService statusService,
      MailStoreFactory storeFactory,
      PublicationReportRepository reportRepository,
      JurisMessageWrapperFactory wrapperFactory,
      DocumentUnitRepository documentUnitRepository,
      XmlPublicationRepository xmlPublicationRepository) {
    this.mailSender = mailSender;
    this.statusService = statusService;
    this.storeFactory = storeFactory;
    this.reportRepository = reportRepository;
    this.wrapperFactory = wrapperFactory;
    this.documentUnitRepository = documentUnitRepository;
    this.xmlPublicationRepository = xmlPublicationRepository;
  }

  @Scheduled(fixedDelay = 60000, initialDelay = 60000)
  public void readEmails() {
    try (Store store = storeFactory.createStore()) {
      processInbox(store);
    } catch (MessagingException e) {
      LOGGER.error("Error creating or closing the store session", e);
    }
  }

  private void processInbox(Store store) {
    try {
      Folder inbox = store.getFolder("INBOX");
      inbox.open(Folder.READ_WRITE);

      List<MessageWrapper> processedMessages = processMessages(inbox);
      moveMessages(processedMessages, inbox, store.getFolder("processed"));
    } catch (MessagingException e) {
      throw new StatusImporterException("Error processing inbox", e);
    }
  }

  @NotNull
  private List<MessageWrapper> processMessages(Folder inbox) throws MessagingException {
    List<MessageWrapper> successfulProcessedMessages = new ArrayList<>();

    Arrays.stream(inbox.getMessages())
        .map(wrapperFactory::getResponsibleWrapper)
        .flatMap(Optional::stream)
        .sorted(Comparator.comparing(wrapper -> wrapper instanceof ImportMessageWrapper ? 0 : 1))
        .forEach(
            messageWrapper -> {
              String subject = null;

              try {
                subject = messageWrapper.getSubject();

                forwardMessage(messageWrapper);
                setPublicationStatus(messageWrapper);
                saveAttachments(messageWrapper);

                successfulProcessedMessages.add(messageWrapper);
              } catch (MessagingException ex) {
                LOGGER.error("Message has no subject", ex);
              } catch (StatusImporterException ex) {
                LOGGER.error("Message {} couldn't processed", subject, ex);
              } catch (Exception ex) {
                LOGGER.error("Unexpected exception by process messages", ex);
              }
            });

    return successfulProcessedMessages;
  }

  private void saveAttachments(MessageWrapper messageWrapper) {
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
      List<MailAttachment> mailAttachments = collectAttachments(messageWrapper);
      String documentNumber = messageWrapper.getDocumentNumber();
      Instant receivedDate = messageWrapper.getReceivedDate();
      reportRepository.saveAll(
          mailAttachments.stream()
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
    } catch (MessagingException | IOException e) {
      throw new StatusImporterException("Error saving attachments", e);
    }
  }

  public static String stringToHTML(String input) {
    return "<html>"
        + String.join(
            "",
            Arrays.stream(input.split("\n")).map(s -> "<p>" + s + "</p>").toArray(String[]::new))
        + "</html>";
  }

  private List<MailAttachment> collectAttachments(MessageWrapper messageWrapper)
      throws MessagingException, IOException {
    return messageWrapper.getAttachments().stream()
        .map(
            attachment ->
                MailAttachment.builder()
                    .fileName(attachment.fileName())
                    .fileContent(attachment.fileContent())
                    .build())
        .toList();
  }

  private void setPublicationStatus(MessageWrapper messageWrapper) {
    try {
      var lastStatus = statusService.getLatestStatus(messageWrapper.getDocumentNumber());
      if (lastStatus == null) {
        return;
      }
      statusService.update(
          messageWrapper.getDocumentNumber(),
          Status.builder()
              .publicationStatus(lastStatus)
              .withError(messageWrapper.hasErrors() || !messageWrapper.isPublished().orElse(true))
              .build());
    } catch (Exception e) {
      throw new StatusImporterException("Could not update publicationStatus", e);
    }
  }

  private void forwardMessage(MessageWrapper messageWrapper) {
    try {
      String documentNumber = messageWrapper.getDocumentNumber();
      String subject = messageWrapper.getSubject();
      List<MailAttachment> mailAttachments = collectAttachments(messageWrapper);

      var xmlPublication =
          xmlPublicationRepository.getLastXmlPublication(
              documentUnitRepository
                  .findByDocumentNumber(documentNumber)
                  .orElseThrow(() -> new DocumentationUnitNotExistsException(documentNumber))
                  .uuid());
      if (xmlPublication != null && xmlPublication.getIssuerAddress() != null) {
        mailSender.sendMail(
            storeFactory.getUsername(),
            xmlPublication.getIssuerAddress(),
            "FWD: " + subject,
            "Anbei weitergeleitet von der jDV:",
            mailAttachments,
            "report-" + documentNumber);
      } else {
        throw new StatusImporterException(
            "Couldn't find issuer address for document number: " + documentNumber);
      }
    } catch (Exception e) {
      throw new StatusImporterException("Could not forward Message", e);
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
      throw new StatusImporterException("Error moving message", e);
    }
  }

  private void deleteMessages(List<Message> messages) {
    messages.forEach(
        message -> {
          try {
            message.setFlag(Flag.DELETED, true);
          } catch (MessagingException e) {
            throw new StatusImporterException("Error deleting Message", e);
          }
        });
  }
}
