package de.bund.digitalservice.ris.caselaw.adapter;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.HttpMailSender;
import de.bund.digitalservice.ris.caselaw.domain.PublicationReport;
import de.bund.digitalservice.ris.caselaw.domain.PublicationReportRepository;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.domain.export.juris.response.ImportMessageWrapper;
import de.bund.digitalservice.ris.domain.export.juris.response.MessageAttachment;
import de.bund.digitalservice.ris.domain.export.juris.response.ProcessMessageWrapper;
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
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@Import({JurisXmlExporterResponseProcessor.class, JurisMessageWrapperFactory.class})
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
  @Mock private Message importMessage;
  @Mock private ImportMessageWrapper importMessageWrapper;
  @Mock private Message processMessage;
  @Mock private ProcessMessageWrapper processMessageWrapper;
  @Mock private JurisMessageWrapperFactory wrapperFactory;
  private JurisXmlExporterResponseProcessor responseProcessor;

  @BeforeEach
  void setup() throws MessagingException, DocumentationUnitNotExistsException {
    when(storeFactory.createStore()).thenReturn(store);
    when(store.getFolder("INBOX")).thenReturn(inbox);
    when(store.getFolder("processed")).thenReturn(processed);
    when(store.getFolder("unprocessable")).thenReturn(unprocessable);

    when(importMessageWrapper.getMessage()).thenReturn(importMessage);
    when(importMessageWrapper.getDocumentNumber()).thenReturn(DOCUMENT_NUMBER);
    when(wrapperFactory.getResponsibleWrapper(importMessage))
        .thenReturn(Optional.of(importMessageWrapper));

    when(processMessageWrapper.getMessage()).thenReturn(processMessage);
    when(processMessageWrapper.getDocumentNumber()).thenReturn(DOCUMENT_NUMBER);
    when(wrapperFactory.getResponsibleWrapper(processMessage))
        .thenReturn(Optional.of(processMessageWrapper));

    when(reportRepository.saveAll(any())).thenReturn(Collections.emptyList());
    when(statusService.update(anyString(), any(Status.class))).thenReturn(Mono.empty());
    when(statusService.getLatestIssuerAddress(DOCUMENT_NUMBER))
        .thenReturn(Mono.just("test@digitalservice.bund.de"));

    responseProcessor =
        new JurisXmlExporterResponseProcessor(
            mailSender, statusService, storeFactory, reportRepository, wrapperFactory);
  }

  @Test
  void testMessageGetsForwarded() throws MessagingException, DocumentationUnitNotExistsException {
    when(inbox.getMessages()).thenReturn(new Message[] {importMessage});

    responseProcessor.readEmails();

    verify(storeFactory, times(1)).createStore();
    verify(statusService, times(1)).getLatestIssuerAddress(DOCUMENT_NUMBER);
    verify(mailSender, times(1))
        .sendMail(any(), any(), any(), any(), any(), eq("report-" + DOCUMENT_NUMBER));
    verify(inbox, times(1)).copyMessages(new Message[] {importMessage}, processed);
    verify(importMessage, times(1)).setFlag(Flag.DELETED, true);
  }

  @Test
  void testMessageGetsNotMovedIfNotForwarded() throws MessagingException {
    when(inbox.getMessages()).thenReturn(new Message[] {importMessage});
    when(importMessageWrapper.getSubject()).thenThrow(new MessagingException());

    assertThrows(
        StatusImporterException.class,
        () -> {
          responseProcessor.readEmails();
        });

    verifyNoInteractions(mailSender);
    verify(inbox, never()).copyMessages(any(), any());
    verify(importMessage, never()).setFlag(Flag.DELETED, true);
  }

  @Test
  void testMessageGetsNotMovedIfDocumentNumberNotFound()
      throws MessagingException, DocumentationUnitNotExistsException {
    when(inbox.getMessages()).thenReturn(new Message[] {importMessage});
    when(statusService.getLatestIssuerAddress(DOCUMENT_NUMBER)).thenReturn(Mono.empty());

    responseProcessor.readEmails();

    verifyNoInteractions(mailSender);
    verify(inbox, never()).copyMessages(new Message[] {importMessage}, processed);
    verify(importMessage, never()).setFlag(Flag.DELETED, true);
  }

  @Test
  void testMessageEncoding() throws MessagingException, IOException {
    when(inbox.getMessages()).thenReturn(new Message[] {importMessage});
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

    when(importMessageWrapper.getAttachments())
        .thenReturn(
            Collections.singletonList(
                new MessageAttachment(
                    "test.txt", reader.lines().collect(Collectors.joining("\n")))));
    when(importMessage.getContent()).thenReturn(multipart);

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
  void testAttachmentsGetsPersisted() throws MessagingException, IOException {
    when(inbox.getMessages()).thenReturn(new Message[] {processMessage});

    Date now = new Date();
    when(statusService.getLatestIssuerAddress(DOCUMENT_NUMBER))
        .thenReturn(Mono.just("test@digitalservice.bund.de"));
    when(processMessageWrapper.getAttachments())
        .thenReturn(
            List.of(
                new MessageAttachment(String.format("%s.html", DOCUMENT_NUMBER), "report"),
                new MessageAttachment(
                    String.format("%s-spellcheck.html", DOCUMENT_NUMBER), "spellcheck")));
    when(processMessageWrapper.getDocumentNumber()).thenReturn(DOCUMENT_NUMBER);
    when((processMessageWrapper.getReceivedDate())).thenReturn(now.toInstant());

    responseProcessor.readEmails();

    verify(inbox, times(1)).copyMessages(new Message[] {processMessage}, processed);
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
    when(inbox.getMessages()).thenReturn(new Message[] {processMessage});
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
    when(processMessageWrapper.getAttachments())
        .thenReturn(
            List.of(
                new MessageAttachment(
                    String.format("%s.html", DOCUMENT_NUMBER),
                    providedHtml.replaceAll("\\s+", " ").replaceAll(">\\s+<", "><")),
                new MessageAttachment(
                    String.format("%s-spellcheck.html", DOCUMENT_NUMBER),
                    "<p><script>alert('sanitize me')</script></p>")));
    when(processMessageWrapper.getDocumentNumber()).thenReturn(DOCUMENT_NUMBER);
    when((processMessageWrapper.getReceivedDate())).thenReturn(now.toInstant());

    responseProcessor.readEmails();

    verify(inbox, times(1)).copyMessages(new Message[] {processMessage}, processed);
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
  void testProcessMessageSetsPublishingStatus()
      throws MessagingException, DocumentationUnitNotExistsException {
    when(inbox.getMessages()).thenReturn(new Message[] {importMessage});

    when(processMessageWrapper.isPublished()).thenReturn(Optional.empty());
    when(processMessageWrapper.hasErrors()).thenReturn(false);

    responseProcessor.readEmails();

    verify(statusService, times(1))
        .update(
            anyString(),
            eq(
                Status.builder()
                    .publicationStatus(PublicationStatus.PUBLISHING)
                    .withError(false)
                    .build()));
  }

  @Test
  void testProcessMessageSetsUnpublishedStatus()
      throws MessagingException, DocumentationUnitNotExistsException {
    when(inbox.getMessages()).thenReturn(new Message[] {processMessage});

    when(processMessageWrapper.isPublished()).thenReturn(Optional.of(false));
    when(processMessageWrapper.hasErrors()).thenReturn(true);

    responseProcessor.readEmails();

    verify(statusService, times(1))
        .update(
            anyString(),
            eq(
                Status.builder()
                    .publicationStatus(PublicationStatus.UNPUBLISHED)
                    .withError(true)
                    .build()));
  }

  @Test
  void testImportMessageSetsPublishedStatus()
      throws MessagingException, DocumentationUnitNotExistsException {
    when(inbox.getMessages()).thenReturn(new Message[] {processMessage});

    when(processMessageWrapper.isPublished()).thenReturn(Optional.of(true));
    when(processMessageWrapper.hasErrors()).thenReturn(false);

    responseProcessor.readEmails();

    verify(statusService, times(1))
        .update(
            anyString(),
            eq(
                Status.builder()
                    .publicationStatus(PublicationStatus.PUBLISHED)
                    .withError(false)
                    .build()));
  }

  @Test
  void testImportMessageSetsPublishedWithErrorsStatus()
      throws MessagingException, DocumentationUnitNotExistsException {
    when(inbox.getMessages()).thenReturn(new Message[] {processMessage});

    when(processMessageWrapper.isPublished()).thenReturn(Optional.of(true));
    when(processMessageWrapper.hasErrors()).thenReturn(true);

    responseProcessor.readEmails();

    verify(statusService, times(1))
        .update(
            anyString(),
            eq(
                Status.builder()
                    .publicationStatus(PublicationStatus.PUBLISHED)
                    .withError(true)
                    .build()));
  }

  @Test
  void testImportMessageSetsUnpublishedStatus()
      throws MessagingException, DocumentationUnitNotExistsException {
    when(inbox.getMessages()).thenReturn(new Message[] {importMessage});

    when(importMessageWrapper.isPublished()).thenReturn(Optional.of(false));
    when(importMessageWrapper.hasErrors()).thenReturn(true);

    responseProcessor.readEmails();

    verify(statusService, times(1))
        .update(
            anyString(),
            eq(
                Status.builder()
                    .publicationStatus(PublicationStatus.UNPUBLISHED)
                    .withError(true)
                    .build()));
  }

  @Test
  void testImportMessagesGetProcessedFirst()
      throws MessagingException, DocumentationUnitNotExistsException {
    when(inbox.getMessages()).thenReturn(new Message[] {processMessage, importMessage});

    when(processMessageWrapper.hasErrors()).thenReturn(false);
    when(processMessageWrapper.isPublished()).thenReturn(Optional.of(true));

    when(importMessageWrapper.hasErrors()).thenReturn(false);
    when(importMessageWrapper.isPublished()).thenReturn(Optional.empty());

    responseProcessor.readEmails();

    InOrder inOrder = Mockito.inOrder(statusService);

    inOrder
        .verify(statusService, times(1))
        .update(
            anyString(),
            eq(
                Status.builder()
                    .publicationStatus(PublicationStatus.PUBLISHING)
                    .withError(false)
                    .build()));

    inOrder
        .verify(statusService, times(1))
        .update(
            anyString(),
            eq(
                Status.builder()
                    .publicationStatus(PublicationStatus.PUBLISHED)
                    .withError(false)
                    .build()));

    inOrder.verifyNoMoreInteractions();
  }

  @Test
  void testLoggingForUnknownDocumentNumber()
      throws MessagingException, DocumentationUnitNotExistsException {
    when(inbox.getMessages()).thenReturn(new Message[] {processMessage});
    when(statusService.getLatestIssuerAddress(DOCUMENT_NUMBER)).thenReturn(Mono.empty());

    assertDoesNotThrow(responseProcessor::readEmails);
  }

  @Test
  void testRethrowsIfCannotGetFolder() throws MessagingException {
    when(store.getFolder("INBOX")).thenThrow(new MessagingException());

    StatusImporterException exception =
        assertThrows(StatusImporterException.class, () -> responseProcessor.readEmails());
    Assertions.assertTrue(exception.getMessage().contains("Error processing inbox: "));
  }

  @Test
  void testRethrowsIfCannotSaveAttachment() throws MessagingException {
    when(inbox.getMessages()).thenReturn(new Message[] {processMessage});
    when(processMessageWrapper.getReceivedDate()).thenThrow(new MessagingException());

    StatusImporterException exception =
        assertThrows(StatusImporterException.class, () -> responseProcessor.readEmails());
    Assertions.assertEquals("Error saving attachments", exception.getMessage());
  }

  @Test
  void testRethrowsIfCannotSetStatus() throws MessagingException {
    when(inbox.getMessages()).thenReturn(new Message[] {processMessage});
    when(processMessageWrapper.hasErrors()).thenThrow(new IOException());

    StatusImporterException exception =
        assertThrows(StatusImporterException.class, () -> responseProcessor.readEmails());
    Assertions.assertTrue(exception.getMessage().contains("Could not update publicationStatus"));
  }
}
