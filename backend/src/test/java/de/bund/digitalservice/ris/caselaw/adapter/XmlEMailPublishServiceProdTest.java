package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.Attachment;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.HttpMailSender;
import de.bund.digitalservice.ris.caselaw.domain.XmlExporter;
import de.bund.digitalservice.ris.caselaw.domain.XmlPublication;
import de.bund.digitalservice.ris.caselaw.domain.XmlPublicationRepository;
import de.bund.digitalservice.ris.caselaw.domain.XmlResultObject;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@Import({XmlEMailPublishService.class})
@TestPropertySource(properties = "mail.exporter.senderAddress=export@neuris")
@ActiveProfiles(profiles = {"production"})
class XmlEMailPublishServiceProdTest {
  private static final String RECEIVER_ADDRESS = "test-to@mail.com";
  private static final String SENDER_ADDRESS = "export@neuris";
  private static final Instant PUBLISH_DATE = Instant.parse("2020-05-05T10:21:35.00Z");
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final String DELIVER_DATE =
      LocalDate.now(Clock.system(ZoneId.of("Europe/Berlin"))).format(DATE_FORMATTER);
  private static final String PROD_MAIL_SUBJECT =
      "id=juris name=NeuRIS da=R df=X dt=N mod=T ld=" + DELIVER_DATE + " vg=test-document-number";
  private static final UUID TEST_UUID = UUID.fromString("88888888-4444-4444-4444-121212121212");
  private static final XmlPublication EXPECTED_BEFORE_SAVE_PROD =
      XmlPublication.builder()
          .documentUnitUuid(TEST_UUID)
          .receiverAddress(RECEIVER_ADDRESS)
          .mailSubject(PROD_MAIL_SUBJECT)
          .xml("xml")
          .statusCode("200")
          .statusMessages(List.of("succeed"))
          .fileName("test.xml")
          .publishDate(PUBLISH_DATE)
          .build();

  private static final XmlPublication SAVED_XML_MAIL_PROD =
      XmlPublication.builder()
          .documentUnitUuid(TEST_UUID)
          .receiverAddress(RECEIVER_ADDRESS)
          .mailSubject(PROD_MAIL_SUBJECT)
          .xml("xml")
          .statusCode("200")
          .statusMessages(List.of("succeed"))
          .fileName("test.xml")
          .publishDate(PUBLISH_DATE)
          .build();
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
        DocumentUnit.builder()
            .uuid(TEST_UUID)
            .documentNumber("test-document-number")
            .coreData(
                CoreData.builder()
                    .court(Court.builder().location("testLocation").label("testLabel").build())
                    .build())
            .filename("file_name")
            .build();
    when(xmlExporter.generateXml(any(DocumentUnit.class))).thenReturn(FORMATTED_XML);

    when(repository.save(EXPECTED_BEFORE_SAVE_PROD)).thenReturn(SAVED_XML_MAIL_PROD);
  }

  @Test
  void testPublishWithProdSubjectAndOriginalCourtAndFileNumber()
      throws ParserConfigurationException, TransformerException {

    StepVerifier.create(service.publish(documentUnit, RECEIVER_ADDRESS))
        .consumeNextWith(
            response -> {
              assertThat(response.mailSubject()).isEqualTo(PROD_MAIL_SUBJECT);
            })
        .verifyComplete();

    verify(xmlExporter).generateXml(documentUnit);
    verify(repository).save(EXPECTED_BEFORE_SAVE_PROD);
    verify(mailSender)
        .sendMail(
            SENDER_ADDRESS,
            RECEIVER_ADDRESS,
            SAVED_XML_MAIL_PROD.mailSubject(),
            "neuris",
            Collections.singletonList(
                Attachment.builder()
                    .fileName(SAVED_XML_MAIL_PROD.fileName())
                    .fileContent(SAVED_XML_MAIL_PROD.xml())
                    .build()),
            SAVED_XML_MAIL_PROD.documentUnitUuid().toString());
  }
}
