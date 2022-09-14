package de.bund.digitalservice.ris.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.domain.DocUnitDTO;
import de.bund.digitalservice.ris.domain.DocumentUnitPublishException;
import de.bund.digitalservice.ris.domain.HttpMailSender;
import de.bund.digitalservice.ris.domain.XmlExporter;
import de.bund.digitalservice.ris.domain.XmlMail;
import de.bund.digitalservice.ris.domain.XmlMailRepository;
import de.bund.digitalservice.ris.domain.XmlMailResponse;
import de.bund.digitalservice.ris.domain.XmlResultObject;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@Import({XmlEMailPublishService.class})
@TestPropertySource(properties = "mail.exporter.senderAddress=export@neuris")
class XmlEMailPublishServiceTest {
  private static final Instant PUBLISH_DATE = Instant.parse("2020-05-05T10:21:35.00Z");
  private static final String MAIL_SUBJECT =
      "id=BGH name=jDVNAME da=R df=X dt=N mod=A vg=Testvorgang";
  private static final UUID TEST_UUID = UUID.fromString("88888888-4444-4444-4444-121212121212");
  private static final XmlMail EXPECTED_BEFORE_SAVE =
      new XmlMail(
          null,
          123L,
          "test-to@mail.com",
          MAIL_SUBJECT,
          "xml",
          "200",
          "succeed",
          "test.xml",
          PUBLISH_DATE);
  private static final XmlMail SAVED_XML_MAIL =
      new XmlMail(
          1L,
          123L,
          "test-to@mail.com",
          MAIL_SUBJECT,
          "xml",
          "200",
          "succeed",
          "test.xml",
          PUBLISH_DATE);
  private static final XmlMailResponse EXPECTED_RESPONSE =
      new XmlMailResponse(TEST_UUID, SAVED_XML_MAIL);
  private static final XmlResultObject FORMATTED_XML =
      new XmlResultObject("xml", "200", List.of("succeed"), "test.xml", PUBLISH_DATE);
  private static final String RECEIVER_ADDRESS = "test-to@mail.com";
  private static final String SENDER_ADDRESS = "export@neuris";

  private DocUnitDTO documentUnit;

  @Autowired private XmlEMailPublishService service;

  @MockBean private XmlExporter xmlExporter;

  @MockBean private XmlMailRepository repository;

  @MockBean private HttpMailSender mailSender;

  @BeforeEach
  void setUp() throws ParserConfigurationException, TransformerException {
    documentUnit = new DocUnitDTO();
    documentUnit.setId(123L);
    documentUnit.setUuid(TEST_UUID);
    documentUnit.setDocumentnumber("test-document-number");
    when(xmlExporter.generateXml(documentUnit)).thenReturn(FORMATTED_XML);

    when(repository.save(EXPECTED_BEFORE_SAVE)).thenReturn(Mono.just(SAVED_XML_MAIL));
  }

  @Test
  void testPublish() {
    StepVerifier.create(service.publish(documentUnit, RECEIVER_ADDRESS))
        .consumeNextWith(
            response ->
                assertThat(response).usingRecursiveComparison().isEqualTo(EXPECTED_RESPONSE))
        .verifyComplete();

    verify(repository).save(EXPECTED_BEFORE_SAVE);
    verify(mailSender)
        .sendMail(
            SENDER_ADDRESS,
            RECEIVER_ADDRESS,
            SAVED_XML_MAIL.mailSubject(),
            SAVED_XML_MAIL.xml(),
            SAVED_XML_MAIL.fileName());
  }

  @Test
  void testPublish_withValidationError() throws ParserConfigurationException, TransformerException {
    var xmlWithValidationError =
        new XmlResultObject("xml", "400", List.of("status-message"), "test.xml", PUBLISH_DATE);
    var xmlMail = new XmlMail(null, 123L, null, null, null, "400", "status-message", null, null);
    var expected = new XmlMailResponse(TEST_UUID, xmlMail);
    when(xmlExporter.generateXml(documentUnit)).thenReturn(xmlWithValidationError);

    StepVerifier.create(service.publish(documentUnit, RECEIVER_ADDRESS))
        .consumeNextWith(
            response -> assertThat(response).usingRecursiveComparison().isEqualTo(expected))
        .verifyComplete();

    verify(repository, times(0)).save(any(XmlMail.class));
    verify(mailSender, times(0))
        .sendMail(anyString(), anyString(), anyString(), anyString(), anyString());
  }

  @Test
  void testPublish_withExceptionFromXmlExporter()
      throws ParserConfigurationException, TransformerException {
    when(xmlExporter.generateXml(documentUnit)).thenThrow(ParserConfigurationException.class);

    StepVerifier.create(service.publish(documentUnit, RECEIVER_ADDRESS))
        .expectErrorMatches(
            ex ->
                ex instanceof DocumentUnitPublishException
                    && ex.getMessage().equals("Couldn't generate xml."))
        .verify();

    verify(repository, times(0)).save(any(XmlMail.class));
    verify(mailSender, times(0))
        .sendMail(anyString(), anyString(), anyString(), anyString(), anyString());
  }

  @Test
  void testPublish_withoutDocumentNumber() {
    documentUnit.setDocumentnumber(null);

    StepVerifier.create(service.publish(documentUnit, RECEIVER_ADDRESS))
        .expectErrorMatches(
            ex ->
                ex instanceof DocumentUnitPublishException
                    && ex.getMessage().equals("No document number has set in the document unit."))
        .verify();

    verify(repository, times(0)).save(any(XmlMail.class));
    verify(mailSender, times(0))
        .sendMail(anyString(), anyString(), anyString(), anyString(), anyString());
  }

  @Test
  void testPublish_withExceptionBySaving() {
    when(repository.save(EXPECTED_BEFORE_SAVE)).thenThrow(IllegalArgumentException.class);

    StepVerifier.create(service.publish(documentUnit, RECEIVER_ADDRESS))
        .expectErrorMatches(ex -> ex instanceof IllegalArgumentException)
        .verify();

    verify(repository).save(any(XmlMail.class));
    verify(mailSender).sendMail(anyString(), anyString(), anyString(), anyString(), anyString());
  }

  @Test
  void testPublish_withoutToReceiverAddressSet() {

    StepVerifier.create(service.publish(documentUnit, null))
        .expectErrorMatches(
            ex ->
                ex instanceof DocumentUnitPublishException
                    && ex.getMessage().equals("No receiver mail address is set"))
        .verify();

    verify(repository, times(0)).save(any(XmlMail.class));
    verify(mailSender, times(0))
        .sendMail(anyString(), anyString(), anyString(), anyString(), anyString());
  }

  @Test
  void testPublish_withExceptionBySendingEmail() {
    doThrow(DocumentUnitPublishException.class)
        .when(mailSender)
        .sendMail(SENDER_ADDRESS, RECEIVER_ADDRESS, MAIL_SUBJECT, "xml", "test.xml");

    StepVerifier.create(service.publish(documentUnit, RECEIVER_ADDRESS))
        .expectErrorMatches(DocumentUnitPublishException.class::isInstance)
        .verify();

    verify(repository, times(0)).save(any(XmlMail.class));
    verify(mailSender).sendMail(SENDER_ADDRESS, RECEIVER_ADDRESS, MAIL_SUBJECT, "xml", "test.xml");
  }

  @Test
  void testGetLastPublishedXml() {
    when(repository.findTopByDocumentUnitIdOrderByPublishDateDesc(123L))
        .thenReturn(Mono.just(SAVED_XML_MAIL));

    StepVerifier.create(service.getLastPublishedXml(123L, TEST_UUID))
        .consumeNextWith(
            response ->
                assertThat(response).usingRecursiveComparison().isEqualTo(EXPECTED_RESPONSE))
        .verifyComplete();

    verify(repository).findTopByDocumentUnitIdOrderByPublishDateDesc(123L);
  }
}
