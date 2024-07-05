package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.HttpMailSender;
import de.bund.digitalservice.ris.caselaw.domain.MailAttachment;
import de.bund.digitalservice.ris.caselaw.domain.XmlExporter;
import de.bund.digitalservice.ris.caselaw.domain.XmlPublication;
import de.bund.digitalservice.ris.caselaw.domain.XmlPublicationRepository;
import de.bund.digitalservice.ris.caselaw.domain.XmlResultObject;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentUnitPublishException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({XmlEMailPublishService.class})
@TestPropertySource(
    properties = {
      "mail.exporter.jurisUsername=test-user",
      "mail.exporter.senderAddress=export@neuris"
    })
@ActiveProfiles(profiles = {"uat"})
class XmlEMailPublishServiceTest {
  private static final String RECEIVER_ADDRESS = "test-to@mail.com";
  private static final String SENDER_ADDRESS = "export@neuris";
  private static final String JURIS_USERNAME = "test-user";
  private static final Instant PUBLISH_DATE = Instant.parse("2020-05-05T10:21:35.00Z");
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final String DELIVER_DATE =
      LocalDate.now(Clock.system(ZoneId.of("Europe/Berlin"))).format(DATE_FORMATTER);
  private static final String MAIL_SUBJECT =
      "id=juris name="
          + JURIS_USERNAME
          + " da=R df=X dt=N mod=T ld="
          + DELIVER_DATE
          + " vg=test-document-number";

  private static final UUID TEST_UUID = UUID.fromString("88888888-4444-4444-4444-121212121212");
  private static final XmlPublication EXPECTED_BEFORE_SAVE =
      XmlPublication.builder()
          .documentUnitUuid(TEST_UUID)
          .receiverAddress(RECEIVER_ADDRESS)
          .mailSubject(MAIL_SUBJECT)
          .xml("xml")
          .statusCode("200")
          .statusMessages(List.of("succeed"))
          .fileName("test.xml")
          .publishDate(PUBLISH_DATE)
          .build();

  private static final XmlPublication SAVED_XML_MAIL =
      XmlPublication.builder()
          .documentUnitUuid(TEST_UUID)
          .receiverAddress(RECEIVER_ADDRESS)
          .mailSubject(MAIL_SUBJECT)
          .xml("xml")
          .statusCode("200")
          .statusMessages(List.of("succeed"))
          .fileName("test.xml")
          .publishDate(PUBLISH_DATE)
          .build();

  private static final XmlPublication EXPECTED_RESPONSE = SAVED_XML_MAIL;
  private static final XmlResultObject FORMATTED_XML =
      new XmlResultObject("xml", "200", List.of("succeed"), "test.xml", PUBLISH_DATE);

  private DocumentUnit documentUnit;

  @Autowired private XmlEMailPublishService service;

  @MockBean private XmlExporter xmlExporter;

  @MockBean private XmlPublicationRepository repository;

  @MockBean private DatabaseDocumentationUnitRepository documentationUnitRepository;

  @MockBean private HttpMailSender mailSender;

  @BeforeEach
  void setUp() throws ParserConfigurationException, TransformerException {
    documentUnit =
        DocumentUnit.builder().uuid(TEST_UUID).documentNumber("test-document-number").build();
    when(xmlExporter.generateXml(any(DocumentUnit.class))).thenReturn(FORMATTED_XML);

    when(repository.save(EXPECTED_BEFORE_SAVE)).thenReturn(SAVED_XML_MAIL);
  }

  @Test
  void testPublish() throws ParserConfigurationException, TransformerException {
    var response = service.publish(documentUnit, RECEIVER_ADDRESS);

    assertThat(response).usingRecursiveComparison().isEqualTo(EXPECTED_RESPONSE);

    verify(xmlExporter)
        .generateXml(
            documentUnit.toBuilder()
                .coreData(
                    CoreData.builder()
                        .court(
                            Court.builder()
                                .label("VGH Mannheim")
                                .location("Mannheim")
                                .type("VGH")
                                .build())
                        .fileNumbers(List.of("TEST"))
                        .build())
                .build());
    verify(repository).save(EXPECTED_BEFORE_SAVE);
    verify(mailSender)
        .sendMail(
            SENDER_ADDRESS,
            RECEIVER_ADDRESS,
            SAVED_XML_MAIL.mailSubject(),
            "neuris",
            Collections.singletonList(
                MailAttachment.builder()
                    .fileName(SAVED_XML_MAIL.fileName())
                    .fileContent(SAVED_XML_MAIL.xml())
                    .build()),
            SAVED_XML_MAIL.documentUnitUuid().toString());
  }

  @Test
  void testPublish_withValidationError() throws ParserConfigurationException, TransformerException {
    var xmlWithValidationError =
        new XmlResultObject("xml", "400", List.of("status-message"), "test.xml", PUBLISH_DATE);
    var expected =
        XmlPublication.builder()
            .documentUnitUuid(TEST_UUID)
            .statusMessages(List.of("status-message"))
            .statusCode("400")
            .build();

    when(xmlExporter.generateXml(any(DocumentUnit.class))).thenReturn(xmlWithValidationError);

    var response = service.publish(documentUnit, RECEIVER_ADDRESS);
    assertThat(response).usingRecursiveComparison().isEqualTo(expected);

    verify(repository, times(0)).save(any(XmlPublication.class));
    verify(mailSender, times(0))
        .sendMail(anyString(), anyString(), anyString(), anyString(), anyList(), anyString());
  }

  @Test
  void testPublish_withExceptionFromXmlExporter()
      throws ParserConfigurationException, TransformerException {
    when(xmlExporter.generateXml(any(DocumentUnit.class)))
        .thenThrow(ParserConfigurationException.class);

    DocumentUnitPublishException ex =
        Assertions.assertThrows(
            DocumentUnitPublishException.class,
            () -> service.publish(documentUnit, RECEIVER_ADDRESS));
    Assertions.assertEquals("Couldn't generate xml.", ex.getMessage());

    verify(repository, times(0)).save(any(XmlPublication.class));
    verify(mailSender, times(0))
        .sendMail(anyString(), anyString(), anyString(), anyString(), anyList(), anyString());
  }

  @Test
  void testPublish_withoutDocumentNumber() {
    documentUnit = documentUnit.toBuilder().documentNumber(null).build();

    // Call the method and check for the exception
    Throwable throwable =
        Assert.assertThrows(
            DocumentUnitPublishException.class,
            () -> service.publish(documentUnit, RECEIVER_ADDRESS));

    assertThat(throwable.getMessage())
        .isEqualTo("No document number has set in the document unit.");

    // Verify that repository.save and mailSender.sendMail were not called
    verify(repository, times(0)).save(any(XmlPublication.class));
    verify(mailSender, times(0))
        .sendMail(anyString(), anyString(), anyString(), anyString(), any(List.class), anyString());
  }

  @Test
  void testPublish_withExceptionBySaving() {
    when(repository.save(EXPECTED_BEFORE_SAVE)).thenThrow(IllegalArgumentException.class);

    Assert.assertThrows(
        IllegalArgumentException.class, () -> service.publish(documentUnit, RECEIVER_ADDRESS));

    verify(repository).save(any(XmlPublication.class));
    verify(mailSender)
        .sendMail(anyString(), anyString(), anyString(), anyString(), anyList(), anyString());
  }

  @Test
  void testPublish_withoutToReceiverAddressSet() {
    Throwable throwable =
        Assert.assertThrows(
            DocumentUnitPublishException.class, () -> service.publish(documentUnit, null));

    assertThat(throwable.getMessage()).isEqualTo("No receiver mail address is set");

    verify(repository, times(0)).save(any(XmlPublication.class));
    verify(mailSender, times(0))
        .sendMail(anyString(), anyString(), anyString(), anyString(), anyList(), anyString());
  }

  @Test
  void testPublish_withExceptionBySendingEmail() {
    doThrow(DocumentUnitPublishException.class)
        .when(mailSender)
        .sendMail(
            SENDER_ADDRESS,
            RECEIVER_ADDRESS,
            MAIL_SUBJECT,
            "neuris",
            Collections.singletonList(
                MailAttachment.builder().fileName("test.xml").fileContent("xml").build()),
            TEST_UUID.toString());

    Assert.assertThrows(
        DocumentUnitPublishException.class, () -> service.publish(documentUnit, RECEIVER_ADDRESS));

    verify(repository, times(0)).save(any(XmlPublication.class));
    verify(mailSender)
        .sendMail(
            SENDER_ADDRESS,
            RECEIVER_ADDRESS,
            MAIL_SUBJECT,
            "neuris",
            Collections.singletonList(
                MailAttachment.builder().fileName("test.xml").fileContent("xml").build()),
            TEST_UUID.toString());
  }

  @Test
  void testGetLastPublishedXml() {
    List<XmlPublication> list = List.of(SAVED_XML_MAIL);
    when(repository.getPublicationsByDocumentUnitUuid(TEST_UUID)).thenReturn((List) list);

    var response = service.getPublications(TEST_UUID);
    assertThat(response.get(0)).usingRecursiveComparison().isEqualTo(EXPECTED_RESPONSE);

    verify(repository).getPublicationsByDocumentUnitUuid(TEST_UUID);
  }
}
