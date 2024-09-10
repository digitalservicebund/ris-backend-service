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
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.HandoverEntityType;
import de.bund.digitalservice.ris.caselaw.domain.HandoverException;
import de.bund.digitalservice.ris.caselaw.domain.HandoverMail;
import de.bund.digitalservice.ris.caselaw.domain.HandoverRepository;
import de.bund.digitalservice.ris.caselaw.domain.HttpMailSender;
import de.bund.digitalservice.ris.caselaw.domain.MailAttachment;
import de.bund.digitalservice.ris.caselaw.domain.XmlExporter;
import de.bund.digitalservice.ris.caselaw.domain.XmlTransformationResult;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
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
@Import({HandoverMailService.class})
@TestPropertySource(
    properties = {
      "mail.exporter.jurisUsername=test-user",
      "mail.exporter.senderAddress=export@neuris"
    })
@ActiveProfiles(profiles = {"uat"})
class HandoverMailServiceTest {
  private static final String RECEIVER_ADDRESS = "test-to@mail.com";
  private static final String ISSUER_ADDRESS = "neuris-user@example.com";
  private static final String SENDER_ADDRESS = "export@neuris";
  private static final String JURIS_USERNAME = "test-user";
  private static final Instant CREATED_DATE = Instant.parse("2020-05-05T10:21:35.00Z");
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
  private static final HandoverMail EXPECTED_BEFORE_SAVE =
      HandoverMail.builder()
          .entityId(TEST_UUID)
          .entityType(HandoverEntityType.DOCUMENTATION_UNIT)
          .receiverAddress(RECEIVER_ADDRESS)
          .mailSubject(MAIL_SUBJECT)
          .attachments(
              List.of(MailAttachment.builder().fileName("test.xml").fileContent("xml").build()))
          .success(true)
          .statusMessages(List.of("succeed"))
          .handoverDate(CREATED_DATE)
          .issuerAddress(ISSUER_ADDRESS)
          .build();

  private static final HandoverMail SAVED_XML_MAIL =
      HandoverMail.builder()
          .entityId(TEST_UUID)
          .entityType(HandoverEntityType.DOCUMENTATION_UNIT)
          .receiverAddress(RECEIVER_ADDRESS)
          .mailSubject(MAIL_SUBJECT)
          .attachments(
              List.of(MailAttachment.builder().fileName("test.xml").fileContent("xml").build()))
          .success(true)
          .statusMessages(List.of("succeed"))
          .handoverDate(CREATED_DATE)
          .issuerAddress(ISSUER_ADDRESS)
          .build();

  private static final HandoverMail EXPECTED_RESPONSE = SAVED_XML_MAIL;
  private static final XmlTransformationResult FORMATTED_XML =
      new XmlTransformationResult("xml", true, List.of("succeed"), "test.xml", CREATED_DATE);

  private DocumentationUnit documentationUnit;

  @Autowired private HandoverMailService service;

  @MockBean private XmlExporter xmlExporter;

  @MockBean private HandoverRepository repository;

  @MockBean private DatabaseDocumentationUnitRepository documentationUnitRepository;

  @MockBean private HttpMailSender mailSender;

  @BeforeEach
  void setUp() throws ParserConfigurationException, TransformerException {
    documentationUnit =
        DocumentationUnit.builder().uuid(TEST_UUID).documentNumber("test-document-number").build();
    when(xmlExporter.transformToXml(any(DocumentationUnit.class))).thenReturn(FORMATTED_XML);

    when(repository.save(EXPECTED_BEFORE_SAVE)).thenReturn(SAVED_XML_MAIL);
  }

  @Test
  void testSend() throws ParserConfigurationException, TransformerException {
    var response = service.handOver(documentationUnit, RECEIVER_ADDRESS, ISSUER_ADDRESS);

    assertThat(response).usingRecursiveComparison().isEqualTo(EXPECTED_RESPONSE);

    verify(xmlExporter)
        .transformToXml(
            documentationUnit.toBuilder()
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
                    .fileName(SAVED_XML_MAIL.attachments().get(0).fileName())
                    .fileContent(SAVED_XML_MAIL.attachments().get(0).fileContent())
                    .build()),
            SAVED_XML_MAIL.entityId().toString());
  }

  @Test
  void testSend_withValidationError() throws ParserConfigurationException, TransformerException {
    var xmlWithValidationError =
        new XmlTransformationResult(
            "xml", false, List.of("status-message"), "test.xml", CREATED_DATE);
    var expected =
        HandoverMail.builder()
            .entityId(TEST_UUID)
            .statusMessages(List.of("status-message"))
            .success(false)
            .build();

    when(xmlExporter.transformToXml(any(DocumentationUnit.class)))
        .thenReturn(xmlWithValidationError);

    var response = service.handOver(documentationUnit, RECEIVER_ADDRESS, ISSUER_ADDRESS);
    assertThat(response).usingRecursiveComparison().isEqualTo(expected);

    verify(repository, times(0)).save(any(HandoverMail.class));
    verify(mailSender, times(0))
        .sendMail(anyString(), anyString(), anyString(), anyString(), anyList(), anyString());
  }

  @Test
  void testSend_withExceptionFromXmlExporter()
      throws ParserConfigurationException, TransformerException {
    when(xmlExporter.transformToXml(any(DocumentationUnit.class)))
        .thenThrow(ParserConfigurationException.class);

    HandoverException ex =
        Assertions.assertThrows(
            HandoverException.class,
            () -> service.handOver(documentationUnit, RECEIVER_ADDRESS, ISSUER_ADDRESS));
    Assertions.assertEquals("Couldn't generate xml.", ex.getMessage());

    verify(repository, times(0)).save(any(HandoverMail.class));
    verify(mailSender, times(0))
        .sendMail(anyString(), anyString(), anyString(), anyString(), anyList(), anyString());
  }

  @Test
  void testSend_withoutDocumentNumber() {
    documentationUnit = documentationUnit.toBuilder().documentNumber(null).build();

    // Call the method and check for the exception
    Throwable throwable =
        Assert.assertThrows(
            HandoverException.class,
            () -> service.handOver(documentationUnit, RECEIVER_ADDRESS, ISSUER_ADDRESS));

    assertThat(throwable.getMessage())
        .isEqualTo("No document number has set in the document unit.");

    // Verify that repository.save and mailSender.sendMail were not called
    verify(repository, times(0)).save(any(HandoverMail.class));
    verify(mailSender, times(0))
        .sendMail(anyString(), anyString(), anyString(), anyString(), any(List.class), anyString());
  }

  @Test
  void testSend_withExceptionBySaving() {
    when(repository.save(EXPECTED_BEFORE_SAVE)).thenThrow(IllegalArgumentException.class);

    Assert.assertThrows(
        IllegalArgumentException.class,
        () -> service.handOver(documentationUnit, RECEIVER_ADDRESS, ISSUER_ADDRESS));

    verify(repository).save(any(HandoverMail.class));
    verify(mailSender)
        .sendMail(anyString(), anyString(), anyString(), anyString(), anyList(), anyString());
  }

  @Test
  void testSend_withoutToReceiverAddressSet() {
    Throwable throwable =
        Assert.assertThrows(
            HandoverException.class, () -> service.handOver(documentationUnit, null, null));

    assertThat(throwable.getMessage()).isEqualTo("No receiver mail address is set");

    verify(repository, times(0)).save(any(HandoverMail.class));
    verify(mailSender, times(0))
        .sendMail(anyString(), anyString(), anyString(), anyString(), anyList(), anyString());
  }

  @Test
  void testSend_withExceptionBySendingEmail() {
    doThrow(HandoverException.class)
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
        HandoverException.class,
        () -> service.handOver(documentationUnit, RECEIVER_ADDRESS, ISSUER_ADDRESS));

    verify(repository, times(0)).save(any(HandoverMail.class));
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
  void testGetLastHandoverXmlMail() {
    List<HandoverMail> list = List.of(SAVED_XML_MAIL);
    when(repository.getHandoversByEntity(TEST_UUID, HandoverEntityType.DOCUMENTATION_UNIT))
        .thenReturn(list);

    var response = service.getHandoverResult(TEST_UUID, HandoverEntityType.DOCUMENTATION_UNIT);
    assertThat(response.get(0)).usingRecursiveComparison().isEqualTo(EXPECTED_RESPONSE);

    verify(repository).getHandoversByEntity(TEST_UUID, HandoverEntityType.DOCUMENTATION_UNIT);
  }

  @Test
  void testGetLastEditionHandoverXmlMail() {
    List<HandoverMail> list = List.of(SAVED_XML_MAIL);
    when(repository.getHandoversByEntity(TEST_UUID, HandoverEntityType.EDITION)).thenReturn(list);

    var response = service.getHandoverResult(TEST_UUID, HandoverEntityType.EDITION);
    assertThat(response.get(0)).usingRecursiveComparison().isEqualTo(EXPECTED_RESPONSE);

    verify(repository).getHandoversByEntity(TEST_UUID, HandoverEntityType.EDITION);
  }
}
