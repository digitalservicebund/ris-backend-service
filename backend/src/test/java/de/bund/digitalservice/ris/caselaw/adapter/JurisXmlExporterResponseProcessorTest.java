package de.bund.digitalservice.ris.caselaw.adapter;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.domain.Attachment;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatus;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.domain.HttpMailSender;
import de.bund.digitalservice.ris.caselaw.domain.PublicationReport;
import de.bund.digitalservice.ris.caselaw.domain.PublicationReportRepository;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
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
  @Mock private ImportMessageHandler importMessageHandler;
  @Mock private ProcessMessageHandler processMessageHandler;
  private JurisXmlExporterResponseProcessor responseProcessor;

  @BeforeEach
  void setup() throws MessagingException, IOException {
    when(storeFactory.createStore()).thenReturn(store);
    when(store.getFolder("INBOX")).thenReturn(inbox);
    when(store.getFolder("processed")).thenReturn(processed);
    when(store.getFolder("unprocessable")).thenReturn(unprocessable);
    when(inbox.getMessages()).thenReturn(new Message[] {message});
    when(processMessageHandler.canHandle(message)).thenReturn(true);
    when(processMessageHandler.messageIsActionable()).thenReturn(true);
    when(processMessageHandler.getDocumentNumber(message)).thenReturn(DOCUMENT_NUMBER);
    when(importMessageHandler.canHandle(message)).thenReturn(true);
    when(importMessageHandler.messageIsActionable()).thenReturn(true);
    when(importMessageHandler.getDocumentNumber(message)).thenReturn(DOCUMENT_NUMBER);

    when(reportRepository.saveAll(any())).thenReturn(Flux.empty());
    when(statusService.update(anyString(), any(DocumentUnitStatus.class))).thenReturn(Mono.empty());
    when(statusService.getLatestIssuerAddress(DOCUMENT_NUMBER))
        .thenReturn(Mono.just("test@digitalservice.bund.de"));

    responseProcessor =
        new JurisXmlExporterResponseProcessor(
            Arrays.asList(processMessageHandler, importMessageHandler),
            mailSender,
            statusService,
            storeFactory,
            reportRepository);
  }

  @Test
  void testMessageGetsForwarded() throws MessagingException {
    responseProcessor.readEmails();

    verify(storeFactory, times(1)).createStore();
    verify(statusService, times(1)).getLatestIssuerAddress(DOCUMENT_NUMBER);
    verify(mailSender, times(1))
        .sendMail(any(), any(), any(), any(), any(), eq("report-" + DOCUMENT_NUMBER));
    verify(inbox, times(1)).copyMessages(new Message[] {message}, processed);
    verify(message, times(1)).setFlag(Flag.DELETED, true);
  }

  @Test
  void testMessageEncoding() throws MessagingException, IOException {
    when(processMessageHandler.canHandle(message)).thenReturn(false);
    when(statusService.getLatestIssuerAddress(DOCUMENT_NUMBER))
        .thenReturn(Mono.just("test@digitalservice.bund.de"));

    Multipart multipart = new MimeMultipart();
    BodyPart bodyPart = new MimeBodyPart();
    multipart.addBodyPart(bodyPart);
    MimeBodyPart attachmentPart = new MimeBodyPart();
    attachmentPart.attachFile("src/test/resources/EXAMPLE-LOGFILE.log");
    multipart.addBodyPart(attachmentPart);
    BufferedReader reader =
        new BufferedReader(new InputStreamReader(attachmentPart.getInputStream()));

    when(importMessageHandler.getAttachments(message))
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
    when(processMessageHandler.messageIsActionable()).thenReturn(false);
    when(importMessageHandler.canHandle(message)).thenReturn(false);

    responseProcessor.readEmails();

    verifyNoInteractions(mailSender);
    verify(inbox, times(1)).copyMessages(new Message[] {message}, unprocessable);
    verify(message, times(1)).setFlag(Flag.DELETED, true);
  }

  @Test
  void testAttachmentsGetsPersisted() throws MessagingException, IOException {
    Date now = new Date();
    when(statusService.getLatestIssuerAddress(DOCUMENT_NUMBER))
        .thenReturn(Mono.just("test@digitalservice.bund.de"));
    when(processMessageHandler.getAttachments(message))
        .thenReturn(
            List.of(
                new MessageAttachment(String.format("%s.html", DOCUMENT_NUMBER), "report"),
                new MessageAttachment(
                    String.format("%s-spellcheck.html", DOCUMENT_NUMBER), "spellcheck")));
    when(processMessageHandler.getDocumentNumber(message)).thenReturn(DOCUMENT_NUMBER);
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

  @Test
  void testAttachmentsGetSanitized() throws MessagingException, IOException {
    Date now = new Date();
    when(statusService.getLatestIssuerAddress(DOCUMENT_NUMBER))
        .thenReturn(Mono.just("test@digitalservice.bund.de"));
    String providedHtml =
        """
<html>
<head>
    <META http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Title</title>
</head>
<body>
    <p><img src="https://placehold.it/120x120&text=image1" align="right"><br></p>
    <h2>Header</h2>
    <table border="0" width="55%">
        <tbody>
            <tr>
                <td width="50%"><strong>ABC</strong></td>
                <td width="50%" align="right"><strong>DEF</strong></td>
            </tr>
        </tbody>
    </table>
    <hr width="100%">
    <p>Paragraph 1</p>
    <p><strong>Paragraph 2<font color="#ff0000" size="+1">
                <i>Italic</i>
            </font><br>
            <table hspace="50" border="0" width="50%" cellSpacing="8">
                <tbody></tbody>
            </table>Text
        </strong><br></p>
    <hr width="100%">
</body>
</html>""";
    String expectedHtml =
        """
<p><img src="https://placehold.it/120x120&amp;text&#61;image1" align="right" /><br /></p>
<h2>Header</h2>
<table border="0" width="55%">
   <tbody>
       <tr>
           <td width="50%"><strong>ABC</strong></td>
           <td width="50%" align="right"><strong>DEF</strong></td>
       </tr>
   </tbody>
</table>
<hr width="100%" />
<p>Paragraph 1</p>
<p><strong>Paragraph 2<font color="#ff0000">
           <i>Italic</i>
       </font><br />
       <table hspace="50" border="0" width="50%" cellspacing="8">
           <tbody></tbody>
       </table>Text
   </strong><br /></p>
<hr width="100%" />""";
    when(processMessageHandler.getAttachments(message))
        .thenReturn(
            List.of(
                new MessageAttachment(
                    String.format("%s.html", DOCUMENT_NUMBER),
                    providedHtml.replaceAll("\\s+", " ").replaceAll(">\\s+<", "><")),
                new MessageAttachment(
                    String.format("%s-spellcheck.html", DOCUMENT_NUMBER),
                    "<p><script>alert('sanitize me')</script></p>")));
    when(processMessageHandler.getDocumentNumber(message)).thenReturn(DOCUMENT_NUMBER);
    when((message.getReceivedDate())).thenReturn(now);

    responseProcessor.readEmails();

    verify(inbox, times(1)).copyMessages(new Message[] {message}, processed);
    verify(reportRepository)
        .saveAll(
            List.of(
                PublicationReport.builder()
                    .content(expectedHtml.replaceAll("\\s+", " ").replaceAll(">\\s+<", "><"))
                    .documentNumber(DOCUMENT_NUMBER)
                    .receivedDate(now.toInstant())
                    .build(),
                PublicationReport.builder()
                    .content("<p></p>")
                    .documentNumber(DOCUMENT_NUMBER)
                    .receivedDate(now.toInstant())
                    .build()));
  }

  @Test
  void testProcessMessageSetsPublishingStatus() {
    when(processMessageHandler.canHandle(message)).thenReturn(true);
    when(processMessageHandler.messageIsActionable()).thenReturn(true);
    when(processMessageHandler.isPublished(message)).thenReturn(Optional.empty());
    when(processMessageHandler.hasErrors(message)).thenReturn(false);

    responseProcessor.readEmails();

    verify(statusService, times(1))
        .update(
            anyString(),
            eq(
                DocumentUnitStatus.builder()
                    .status(PublicationStatus.PUBLISHING)
                    .withError(false)
                    .build()));
  }

  @Test
  void testProcessMessageSetsUnpublishedStatus() {
    when(processMessageHandler.canHandle(message)).thenReturn(true);
    when(processMessageHandler.messageIsActionable()).thenReturn(true);
    when(processMessageHandler.isPublished(message)).thenReturn(Optional.of(false));
    when(processMessageHandler.hasErrors(message)).thenReturn(true);

    when(importMessageHandler.canHandle(message)).thenReturn(false);

    responseProcessor.readEmails();

    verify(statusService, times(1))
        .update(
            anyString(),
            eq(
                DocumentUnitStatus.builder()
                    .status(PublicationStatus.UNPUBLISHED)
                    .withError(true)
                    .build()));
  }

  @Test
  void testImportMessageSetsPublishedStatus() {
    when(processMessageHandler.canHandle(message)).thenReturn(false);

    when(importMessageHandler.canHandle(message)).thenReturn(true);
    when(importMessageHandler.messageIsActionable()).thenReturn(true);
    when(importMessageHandler.isPublished(message)).thenReturn(Optional.of(true));
    when(importMessageHandler.hasErrors(message)).thenReturn(false);

    responseProcessor.readEmails();

    verify(statusService, times(1))
        .update(
            anyString(),
            eq(
                DocumentUnitStatus.builder()
                    .status(PublicationStatus.PUBLISHED)
                    .withError(false)
                    .build()));
  }

  @Test
  void testImportMessageSetsPublishedWithErrorsStatus() {
    when(processMessageHandler.canHandle(message)).thenReturn(false);

    when(importMessageHandler.canHandle(message)).thenReturn(true);
    when(importMessageHandler.messageIsActionable()).thenReturn(true);
    when(importMessageHandler.isPublished(message)).thenReturn(Optional.of(true));
    when(importMessageHandler.hasErrors(message)).thenReturn(true);

    responseProcessor.readEmails();

    verify(statusService, times(1))
        .update(
            anyString(),
            eq(
                DocumentUnitStatus.builder()
                    .status(PublicationStatus.PUBLISHED)
                    .withError(true)
                    .build()));
  }

  @Test
  void testImportMessageSetsUnpublishedStatus() {
    when(processMessageHandler.canHandle(message)).thenReturn(false);

    when(importMessageHandler.canHandle(message)).thenReturn(true);
    when(importMessageHandler.messageIsActionable()).thenReturn(true);
    when(importMessageHandler.isPublished(message)).thenReturn(Optional.of(false));
    when(importMessageHandler.hasErrors(message)).thenReturn(true);

    responseProcessor.readEmails();

    verify(statusService, times(1))
        .update(
            anyString(),
            eq(
                DocumentUnitStatus.builder()
                    .status(PublicationStatus.UNPUBLISHED)
                    .withError(true)
                    .build()));
  }
}
