package de.bund.digitalservice.ris.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.bund.digitalservice.ris.domain.DocUnit;
import de.bund.digitalservice.ris.domain.DocumentUnitPublishException;
import de.bund.digitalservice.ris.domain.XmlMail;
import de.bund.digitalservice.ris.domain.XmlMailRepository;
import de.bund.digitalservice.ris.domain.export.JurisXmlExporter;
import javax.mail.internet.MimeMessage;
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
  private XmlMailPublishService service;

  @MockBean private JurisXmlExporter xmlExporter;

  @MockBean private XmlMailRepository repository;

  @MockBean private JavaMailSender mailSender;

  @BeforeEach
  void setUp() {
    service = new XmlMailPublishService(xmlExporter, repository, mailSender, "fromMailAddress");
  }

  @Test
  void testPublish() throws JsonProcessingException {
    var documentUnit = new DocUnit();
    documentUnit.setId(123L);
    documentUnit.setDocumentnumber("test-document-number");
    var mailSubject = "id=BGH name=jDVNAME da=R df=X dt=N mod=A vg=test-document-number";
    var xml = "test-xml";
    var expectedBeforeSave = new XmlMail(null, 123L, mailSubject, xml);
    var expectedAfterSave = new XmlMail(1L, 123L, mailSubject, xml);
    when(xmlExporter.generateXml(documentUnit)).thenReturn("test-xml");
    when(repository.save(any(XmlMail.class))).thenReturn(Mono.just(expectedAfterSave));
    when(mailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));
    service.setToMailAddressList("test-to@mail.com");

    StepVerifier.create(service.publish(documentUnit))
        .consumeNextWith(response -> assertThat(response).isEqualTo(expectedAfterSave))
        .verifyComplete();

    verify(repository).save(expectedBeforeSave);
  }

  @Test
  void testPublish_withExceptionFromXmlExporter() throws JsonProcessingException {
    var documentUnit = new DocUnit();
    when(xmlExporter.generateXml(documentUnit)).thenThrow(JsonProcessingException.class);

    StepVerifier.create(service.publish(documentUnit))
        .expectErrorMatches(
            ex ->
                ex instanceof DocumentUnitPublishException
                    && ex.getMessage().equals("Couldn't generate xml."))
        .verify();
  }

  @Test
  void testPublish_withoutDocumentNumber() throws JsonProcessingException {
    var documentUnit = new DocUnit();
    when(xmlExporter.generateXml(documentUnit)).thenReturn("text-xml");

    StepVerifier.create(service.publish(documentUnit))
        .expectErrorMatches(
            ex ->
                ex instanceof DocumentUnitPublishException
                    && ex.getMessage().equals("No document number has set in the document unit."))
        .verify();
  }

  @Test
  void testPublish_withExceptionBySaving() throws JsonProcessingException {
    var documentUnit = new DocUnit();
    documentUnit.setDocumentnumber("test-document-number");
    when(xmlExporter.generateXml(documentUnit)).thenReturn("test-xml");
    when(repository.save(any(XmlMail.class))).thenThrow(IllegalArgumentException.class);

    StepVerifier.create(service.publish(documentUnit))
        .expectErrorMatches(ex -> ex instanceof IllegalArgumentException)
        .verify();
  }

  @Test
  void testPublish_withoutToMailSet() throws JsonProcessingException {
    var documentUnit = new DocUnit();
    documentUnit.setDocumentnumber("test-document-number");
    when(xmlExporter.generateXml(documentUnit)).thenReturn("test-xml");
    when(repository.save(any(XmlMail.class)))
        .thenReturn(Mono.just(new XmlMail(1L, 123L, "test-subject", "test-xml")));

    StepVerifier.create(service.publish(documentUnit))
        .expectErrorMatches(
            ex ->
                ex instanceof DocumentUnitPublishException
                    && ex.getMessage().equals("No receiver mail address is set"))
        .verify();
  }

  @Test
  void testPublish_withWrongFormattedFromMailSet() throws JsonProcessingException {
    var documentUnit = new DocUnit();
    documentUnit.setDocumentnumber("test-document-number");
    when(xmlExporter.generateXml(documentUnit)).thenReturn("test-xml");
    when(repository.save(any(XmlMail.class)))
        .thenReturn(Mono.just(new XmlMail(1L, 123L, "test-subject", "test-xml")));
    when(mailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));
    service = new XmlMailPublishService(xmlExporter, repository, mailSender, "<");
    service.setToMailAddressList("to-mail@test.com");

    StepVerifier.create(service.publish(documentUnit))
        .expectErrorMatches(
            ex ->
                ex instanceof DocumentUnitPublishException
                    && ex.getMessage().equals("Sender mail address is not correct"))
        .verify();
  }

  @Test
  void testPublish_withWrongFormattedToMailSet() throws JsonProcessingException {
    var documentUnit = new DocUnit();
    documentUnit.setDocumentnumber("test-document-number");
    when(xmlExporter.generateXml(documentUnit)).thenReturn("test-xml");
    when(repository.save(any(XmlMail.class)))
        .thenReturn(Mono.just(new XmlMail(1L, 123L, "test-subject", "test-xml")));
    when(mailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));
    service.setToMailAddressList("<");

    StepVerifier.create(service.publish(documentUnit))
        .expectErrorMatches(
            ex ->
                ex instanceof DocumentUnitPublishException
                    && ex.getMessage().equals("Receiver mail address is not correct"))
        .verify();
  }
}
