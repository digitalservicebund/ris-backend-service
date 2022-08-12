package de.bund.digitalservice.ris.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.domain.DocUnit;
import de.bund.digitalservice.ris.domain.DocumentUnitPublishException;
import de.bund.digitalservice.ris.domain.XmlMail;
import de.bund.digitalservice.ris.domain.XmlMailRepository;
import de.bund.digitalservice.ris.domain.XmlMailResponse;
import de.bund.digitalservice.ris.domain.export.juris.JurisFormattedXML;
import de.bund.digitalservice.ris.domain.export.juris.JurisXmlExporter;
import de.bund.digitalservice.ris.domain.export.juris.Status;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import javax.mail.internet.MimeMessage;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@Import(XmlMailPublishService.class)
class XmlMailPublishServiceTest {
  private static final Instant PUBLISH_DATE = Instant.parse("2020-05-05T10:21:35.00Z");
  private static final String MAIL_SUBJECT =
      "id=BGH name=jDVNAME da=R df=X dt=N mod=A vg=test-document-number";
  private static final UUID TEST_UUID = UUID.fromString("88888888-4444-4444-4444-121212121212");
  private static final XmlMail EXPECTED_BEFORE_SAVE =
      new XmlMail(
          null,
          123L,
          MAIL_SUBJECT,
          "xml",
          "status-code",
          "status-messages",
          "test.xml",
          PUBLISH_DATE);
  private static final XmlMail SAVED_XML_MAIL =
      new XmlMail(
          1L,
          123L,
          MAIL_SUBJECT,
          "xml",
          "status-code",
          "status-messages",
          "test.xml",
          PUBLISH_DATE);
  private static final XmlMailResponse EXPECTED_RESPONSE =
      new XmlMailResponse(TEST_UUID, SAVED_XML_MAIL);
  private static final JurisFormattedXML FORMATTED_XML =
      new JurisFormattedXML(
          "xml", new Status("status-code", List.of("status-messages")), "test.xml", PUBLISH_DATE);

  private DocUnit documentUnit;

  private XmlMailPublishService service;

  @MockBean private JurisXmlExporter xmlExporter;

  @MockBean private XmlMailRepository repository;

  @MockBean private JavaMailSender mailSender;

  @BeforeEach
  void setUp() throws ParserConfigurationException, TransformerException {
    service = new XmlMailPublishService(xmlExporter, repository, mailSender, "fromMailAddress");

    documentUnit = new DocUnit();
    documentUnit.setId(123L);
    documentUnit.setUuid(TEST_UUID);
    documentUnit.setDocumentnumber("test-document-number");
    when(xmlExporter.generateXml(documentUnit)).thenReturn(FORMATTED_XML);

    when(repository.save(EXPECTED_BEFORE_SAVE)).thenReturn(Mono.just(SAVED_XML_MAIL));
    when(mailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));
  }

  @Test
  void testPublish() {
    service.setToMailAddressList("test-to@mail.com");

    StepVerifier.create(service.publish(documentUnit))
        .consumeNextWith(
            response ->
                assertThat(response).usingRecursiveComparison().isEqualTo(EXPECTED_RESPONSE))
        .verifyComplete();

    verify(repository).save(EXPECTED_BEFORE_SAVE);
  }

  @Test
  void testPublish_withExceptionFromXmlExporter()
      throws ParserConfigurationException, TransformerException {
    when(xmlExporter.generateXml(documentUnit)).thenThrow(ParserConfigurationException.class);

    StepVerifier.create(service.publish(documentUnit))
        .expectErrorMatches(
            ex ->
                ex instanceof DocumentUnitPublishException
                    && ex.getMessage().equals("Couldn't generate xml."))
        .verify();
  }

  @Test
  void testPublish_withoutDocumentNumber() {
    documentUnit.setDocumentnumber(null);

    StepVerifier.create(service.publish(documentUnit))
        .expectErrorMatches(
            ex ->
                ex instanceof DocumentUnitPublishException
                    && ex.getMessage().equals("No document number has set in the document unit."))
        .verify();
  }

  @Test
  void testPublish_withExceptionBySaving() {
    when(repository.save(EXPECTED_BEFORE_SAVE)).thenThrow(IllegalArgumentException.class);

    StepVerifier.create(service.publish(documentUnit))
        .expectErrorMatches(ex -> ex instanceof IllegalArgumentException)
        .verify();
  }

  //  @Test
  void testPublish_withoutToMailSet() {

    StepVerifier.create(service.publish(documentUnit))
        .expectErrorMatches(
            ex ->
                ex instanceof DocumentUnitPublishException
                    && ex.getMessage().equals("No receiver mail address is set"))
        .verify();
  }

  //  @Test
  void testPublish_withWrongFormattedFromMailSet() {
    service = new XmlMailPublishService(xmlExporter, repository, mailSender, "<");
    service.setToMailAddressList("to-mail@test.com");

    StepVerifier.create(service.publish(documentUnit))
        .expectErrorMatches(
            ex ->
                ex instanceof DocumentUnitPublishException
                    && ex.getMessage().equals("Sender mail address is not correct"))
        .verify();
  }

  //  @Test
  void testPublish_withWrongFormattedToMailSet() {
    service.setToMailAddressList("<");

    StepVerifier.create(service.publish(documentUnit))
        .expectErrorMatches(
            ex ->
                ex instanceof DocumentUnitPublishException
                    && ex.getMessage().equals("Receiver mail address is not correct"))
        .verify();
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
