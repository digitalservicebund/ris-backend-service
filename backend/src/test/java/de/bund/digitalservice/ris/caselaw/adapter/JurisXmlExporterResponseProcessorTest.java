package de.bund.digitalservice.ris.caselaw.adapter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.domain.HttpMailSender;
import de.bund.digitalservice.ris.domain.export.juris.response.ActionableMessageHandler;
import jakarta.mail.Flags.Flag;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Store;
import java.util.Arrays;
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
public class JurisXmlExporterResponseProcessorTest {
  @MockBean private DocumentUnitStatusService statusService;
  @MockBean private HttpMailSender mailSender;
  @MockBean private ImapStoreFactory storeFactory;

  @Mock private Store store;
  @Mock private Folder inbox;
  @Mock private Folder processed;
  @Mock private Folder unprocessable;
  @Mock private Message message;
  @Mock private ActionableMessageHandler messageHandler;

  private JurisXmlExporterResponseProcessor responseProcessor;
  private final String DOCUMENT_NUMBER = "KORE123456789";

  @BeforeEach
  void setup() throws MessagingException {
    when(storeFactory.createStoreSession()).thenReturn(store);
    when(store.getFolder("INBOX")).thenReturn(inbox);
    when(store.getFolder("processed")).thenReturn(processed);
    when(store.getFolder("unprocessable")).thenReturn(unprocessable);
    when(inbox.getMessages()).thenReturn(new Message[] {message});
    when(messageHandler.canHandle(message)).thenReturn(true);
    when(messageHandler.messageIsActionable()).thenReturn(true);
    when(messageHandler.getDocumentNumber(message)).thenReturn(DOCUMENT_NUMBER);

    responseProcessor =
        new JurisXmlExporterResponseProcessor(
            Arrays.asList(messageHandler), mailSender, statusService, storeFactory);
  }

  @Test
  void testMessageGetsForwarded() throws MessagingException {
    when(statusService.getIssuerAddressOfLatestStatus(DOCUMENT_NUMBER))
        .thenReturn(Mono.just("test@digitalservice.bund.de"));

    responseProcessor.readEmails();

    verify(storeFactory, times(1)).createStoreSession();
    verify(statusService, times(1)).getIssuerAddressOfLatestStatus(DOCUMENT_NUMBER);
    verify(mailSender, times(1))
        .sendMail(any(), any(), any(), any(), any(), any(), eq("report-" + DOCUMENT_NUMBER));
    verify(inbox, times(1)).copyMessages(new Message[] {message}, processed);
    verify(message, times(1)).setFlag(Flag.DELETED, true);
  }

  @Test
  void testMessageGetsNotMovedIfNotForwarded() throws MessagingException {
    when(message.getSubject()).thenThrow(new MessagingException());

    responseProcessor.readEmails();

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
}
