package de.bund.digitalservice.ris.caselaw.adapter;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.domain.Attachment;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.domain.HttpMailSender;
import de.bund.digitalservice.ris.caselaw.domain.PublicationReport;
import de.bund.digitalservice.ris.caselaw.domain.PublicationReportRepository;
import de.bund.digitalservice.ris.domain.export.juris.response.ImportMessageHandler;
import de.bund.digitalservice.ris.domain.export.juris.response.MessageAttachment;
import de.bund.digitalservice.ris.domain.export.juris.response.ProcessMessageHandler;
import de.bund.digitalservice.ris.domain.export.juris.response.StatusImporterException;
import jakarta.mail.BodyPart;
import jakarta.mail.Flags.Flag;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Store;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@Import({JurisXmlExporterResponseProcessor.class})
class JurisXmlExporterResponseProcessorTest {
  private final String DOCUMENT_NUMBER = "KORE123456789";
  @MockBean private DocumentUnitStatusService statusService;
  @MockBean private HttpMailSender mailSender;
  @MockBean private ImapStoreFactory storeFactory;
  @MockBean private PublicationReportRepository reportRepository;
  @Mock private Store store;
  @Mock private Folder inbox;
  @Mock private Folder processed;
  @Mock private Folder unprocessable;
  @Mock private Message message;
  @Mock private ImportMessageHandler importHandler;
  @Mock private ProcessMessageHandler messageHandler;
  private JurisXmlExporterResponseProcessor responseProcessor;

  @BeforeEach
  void setup() throws MessagingException, IOException {
    when(storeFactory.createStoreSession()).thenReturn(store);
    when(store.getFolder("INBOX")).thenReturn(inbox);
    when(store.getFolder("processed")).thenReturn(processed);
    when(store.getFolder("unprocessable")).thenReturn(unprocessable);
    when(inbox.getMessages()).thenReturn(new Message[] {message});
    when(messageHandler.canHandle(message)).thenReturn(true);
    when(messageHandler.messageIsActionable()).thenReturn(true);
    when(messageHandler.getDocumentNumber(message)).thenReturn(DOCUMENT_NUMBER);
    when(importHandler.canHandle(message)).thenReturn(true);
    when(importHandler.messageIsActionable()).thenReturn(true);
    when(importHandler.getDocumentNumber(message)).thenReturn(DOCUMENT_NUMBER);

    responseProcessor =
        new JurisXmlExporterResponseProcessor(
            Collections.singletonList(messageHandler),
            mailSender,
            statusService,
            storeFactory,
            reportRepository);
  }

  @Test
  void testMessageGetsForwarded() throws MessagingException {
    when(statusService.getIssuerAddressOfLatestStatus(DOCUMENT_NUMBER))
        .thenReturn(Mono.just("test@digitalservice.bund.de"));

    responseProcessor.readEmails();

    verify(storeFactory, times(1)).createStoreSession();
    verify(statusService, times(1)).getIssuerAddressOfLatestStatus(DOCUMENT_NUMBER);
    verify(mailSender, times(1))
        .sendMail(any(), any(), any(), any(), any(), eq("report-" + DOCUMENT_NUMBER));
    verify(inbox, times(1)).copyMessages(new Message[] {message}, processed);
    verify(message, times(1)).setFlag(Flag.DELETED, true);
  }

  @Test
  void testMessageEncoding() throws MessagingException, IOException {
    responseProcessor =
        new JurisXmlExporterResponseProcessor(
            Collections.singletonList(importHandler),
            mailSender,
            statusService,
            storeFactory,
            reportRepository);
    when(statusService.getIssuerAddressOfLatestStatus(DOCUMENT_NUMBER))
        .thenReturn(Mono.just("test@digitalservice.bund.de"));

    Multipart multipart = new MimeMultipart();
    BodyPart bodyPart = new MimeBodyPart();
    multipart.addBodyPart(bodyPart);
    MimeBodyPart attachmentPart = new MimeBodyPart();
    attachmentPart.attachFile("src/test/resources/EXAMPLE-LOGFILE.log");
    multipart.addBodyPart(attachmentPart);
    BufferedReader reader =
        new BufferedReader(new InputStreamReader(attachmentPart.getInputStream()));

    when(importHandler.getAttachments(message))
        .thenReturn(
            Collections.singletonList(
                new MessageAttachment(
                    "test.txt", reader.lines().collect(Collectors.joining("\n")))));
    when(message.getContent()).thenReturn(multipart);

    responseProcessor.readEmails();

    verify(mailSender, times(1))
        .sendMail(
            any(),
            any(),
            any(),
            any(),
            argThat(list -> list.get(0).equals(new Attachment("test.txt", "ÄÜÖäüöß"))),
            eq("report-" + DOCUMENT_NUMBER));
  }

  @Test
  void testMessageGetsNotMovedIfNotForwarded() throws MessagingException {
    when(message.getSubject()).thenThrow(new MessagingException());

    assertThrows(
        StatusImporterException.class,
        () -> {
          responseProcessor.readEmails();
        });

    verifyNoInteractions(mailSender);
    verify(inbox, never()).getFolder("processed");
    verify(message, never()).setFlag(Flag.DELETED, true);
  }

  @Test
  void testMessageGetsNotProcessedIfNotActionable() throws MessagingException {
    when(messageHandler.messageIsActionable()).thenReturn(false);

    responseProcessor.readEmails();

    verifyNoInteractions(mailSender);
    verify(inbox, times(1)).copyMessages(new Message[] {message}, unprocessable);
    verify(message, times(1)).setFlag(Flag.DELETED, true);
  }

  @Test
  void testAttachmentsGetsPersisted() throws MessagingException, IOException {
    Date now = new Date();
    when(statusService.getIssuerAddressOfLatestStatus(DOCUMENT_NUMBER))
        .thenReturn(Mono.just("test@digitalservice.bund.de"));
    when(messageHandler.getAttachments(message))
        .thenReturn(
            List.of(
                new MessageAttachment(String.format("%s.html", DOCUMENT_NUMBER), "report"),
                new MessageAttachment(
                    String.format("%s-spellcheck.html", DOCUMENT_NUMBER), "spellcheck")));
    when(messageHandler.getDocumentNumber(message)).thenReturn(DOCUMENT_NUMBER);
    when((message.getReceivedDate())).thenReturn(now);

    responseProcessor.readEmails();

    verify(inbox, times(1)).copyMessages(new Message[] {message}, processed);
    verify(reportRepository)
        .saveAll(
            List.of(
                PublicationReport.builder()
                    .content("report")
                    .documentNumber(DOCUMENT_NUMBER)
                    .receivedDate(now.toInstant())
                    .build(),
                PublicationReport.builder()
                    .content("spellcheck")
                    .documentNumber(DOCUMENT_NUMBER)
                    .receivedDate(now.toInstant())
                    .build()));
  }
}
