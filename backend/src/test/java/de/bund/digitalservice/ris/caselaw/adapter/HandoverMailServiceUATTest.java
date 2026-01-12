package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentInlineDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentInlineRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseLegalPeriodicalEditionRepository;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.FeatureToggleService;
import de.bund.digitalservice.ris.caselaw.domain.HandoverEntityType;
import de.bund.digitalservice.ris.caselaw.domain.HandoverException;
import de.bund.digitalservice.ris.caselaw.domain.HandoverMail;
import de.bund.digitalservice.ris.caselaw.domain.HandoverRepository;
import de.bund.digitalservice.ris.caselaw.domain.HttpMailSender;
import de.bund.digitalservice.ris.caselaw.domain.LegalPeriodicalEdition;
import de.bund.digitalservice.ris.caselaw.domain.MailAttachment;
import de.bund.digitalservice.ris.caselaw.domain.MailAttachmentImage;
import de.bund.digitalservice.ris.caselaw.domain.Reference;
import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.TextCheckService;
import de.bund.digitalservice.ris.caselaw.domain.XmlExporter;
import de.bund.digitalservice.ris.caselaw.domain.XmlTransformationResult;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalPeriodical;
import de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words.IgnoredTextCheckWordRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({HandoverMailService.class, TextCheckService.class})
@TestPropertySource(
    properties = {
      "mail.exporter.jurisUsername=test-user",
      "mail.exporter.senderAddress=export@neuris"
    })
@ActiveProfiles(profiles = {"uat"})
class HandoverMailServiceUATTest {
  private static final String RECEIVER_ADDRESS = "test-to@mail.com";
  private static final String ISSUER_ADDRESS = "neuris-user@example.com";
  private static final String SENDER_ADDRESS = "export@neuris";
  private static final String JURIS_USERNAME = "test-user";
  private static final Instant CREATED_DATE = Instant.parse("2020-05-05T10:21:35.00Z");
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final String DELIVER_DATE =
      LocalDate.now(Clock.system(ZoneId.of("Europe/Berlin"))).format(DATE_FORMATTER);
  private static final String DOC_UNIT_MAIL_SUBJECT =
      "id=juris name=%s da=R df=X dt=N mod=T ld=%s vg=test-document-number"
          .formatted(JURIS_USERNAME, DELIVER_DATE);

  private static final UUID TEST_UUID = UUID.fromString("88888888-4444-4444-4444-121212121212");

  private static final String EDITION_MAIL_SUBJECT =
      "id=juris name=%s da=R df=X dt=F mod=T ld=%s vg=edition-%s"
          .formatted(JURIS_USERNAME, DELIVER_DATE, TEST_UUID);

  private static final HandoverMail DOC_UNIT_SAVED_MAIL =
      HandoverMail.builder()
          .entityId(TEST_UUID)
          .entityType(HandoverEntityType.DOCUMENTATION_UNIT)
          .receiverAddress(RECEIVER_ADDRESS)
          .mailSubject(DOC_UNIT_MAIL_SUBJECT)
          .attachments(
              List.of(MailAttachment.builder().fileName("test.xml").fileContent("xml").build()))
          .imageAttachments(Collections.emptyList())
          .success(true)
          .statusMessages(List.of("succeed"))
          .handoverDate(CREATED_DATE)
          .issuerAddress(ISSUER_ADDRESS)
          .build();

  private static final HandoverMail EDITION_SAVED_MAIL =
      HandoverMail.builder()
          .entityId(TEST_UUID)
          .entityType(HandoverEntityType.EDITION)
          .receiverAddress(RECEIVER_ADDRESS)
          .mailSubject(EDITION_MAIL_SUBJECT)
          .attachments(
              List.of(
                  MailAttachment.builder().fileName("test2.xml").fileContent("xml 2").build(),
                  MailAttachment.builder().fileName("test1.xml").fileContent("xml 1").build()))
          .imageAttachments(Collections.emptyList())
          .success(true)
          .statusMessages(List.of("succeed", "succeed"))
          .handoverDate(CREATED_DATE)
          .issuerAddress(ISSUER_ADDRESS)
          .build();
  private static final XmlTransformationResult DOC_UNIT_XML =
      new XmlTransformationResult("xml", true, List.of("succeed"), "test.xml", CREATED_DATE);

  private static final List<XmlTransformationResult> EDITION_XML =
      List.of(
          new XmlTransformationResult("xml 1", true, List.of("succeed"), "test1.xml", CREATED_DATE),
          new XmlTransformationResult(
              "xml 2", true, List.of("succeed"), "test2.xml", CREATED_DATE));

  private Decision decision;

  private final LegalPeriodical legalPeriodical =
      LegalPeriodical.builder().abbreviation("ABC").build();

  private LegalPeriodicalEdition edition;

  @Autowired private HandoverMailService service;

  @MockitoBean private XmlExporter xmlExporter;

  @MockitoBean private FeatureToggleService featureToggleService;

  @MockitoBean private HandoverRepository repository;

  @MockitoBean private DatabaseDocumentationUnitRepository databaseDocumentationUnitRepository;

  @MockitoBean private DocumentationUnitRepository documentationUnitRepository;

  @MockitoBean private DatabaseLegalPeriodicalEditionRepository editionRepository;

  @MockitoBean private HttpMailSender mailSender;

  @MockitoBean private IgnoredTextCheckWordRepository ignoredTextCheckWordRepository;

  @MockitoBean private AttachmentInlineRepository attachmentInlineRepository;

  @BeforeEach
  void setUp() throws ParserConfigurationException, TransformerException {
    decision =
        Decision.builder()
            .uuid(TEST_UUID)
            .documentNumber("test-document-number")
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .fileNumbers(Collections.emptyList())
                    .build())
            .build();

    when(featureToggleService.isEnabled("neuris.text-check-noindex-handover")).thenReturn(true);

    edition =
        LegalPeriodicalEdition.builder()
            .legalPeriodical(legalPeriodical)
            .id(TEST_UUID)
            .references(
                List.of(
                    Reference.builder()
                        .citation("2004, 1")
                        .legalPeriodical(legalPeriodical)
                        .documentationUnit(
                            RelatedDocumentationUnit.builder()
                                .documentNumber("document-number-1")
                                .build())
                        .build(),
                    Reference.builder()
                        .citation("2004, 2")
                        .legalPeriodical(legalPeriodical)
                        .documentationUnit(
                            RelatedDocumentationUnit.builder()
                                .documentNumber("document-number-2")
                                .build())
                        .build()))
            .build();
    when(xmlExporter.transformToXml(any(Decision.class), anyBoolean())).thenReturn(DOC_UNIT_XML);
    when(xmlExporter.transformToXml(any(LegalPeriodicalEdition.class))).thenReturn(EDITION_XML);
    when(repository.save(DOC_UNIT_SAVED_MAIL)).thenReturn(DOC_UNIT_SAVED_MAIL);
    when(repository.save(EDITION_SAVED_MAIL)).thenReturn(EDITION_SAVED_MAIL);
  }

  @Test
  void testSendDocumentationUnit() throws ParserConfigurationException, TransformerException {
    var response = service.handOver(decision, RECEIVER_ADDRESS, ISSUER_ADDRESS);

    assertThat(response).usingRecursiveComparison().isEqualTo(DOC_UNIT_SAVED_MAIL);

    verify(xmlExporter)
        .transformToXml(
            decision.toBuilder()
                .documentNumber("TESTtest-document-number")
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
                .build(),
            false);

    verify(repository).save(DOC_UNIT_SAVED_MAIL);
    verify(mailSender)
        .sendMail(
            SENDER_ADDRESS,
            RECEIVER_ADDRESS,
            DOC_UNIT_SAVED_MAIL.mailSubject(),
            "neuris",
            Collections.singletonList(
                MailAttachment.builder()
                    .fileName(DOC_UNIT_SAVED_MAIL.attachments().getFirst().fileName())
                    .fileContent(DOC_UNIT_SAVED_MAIL.attachments().getFirst().fileContent())
                    .build()),
            Collections.emptyList(),
            DOC_UNIT_SAVED_MAIL.entityId().toString());
  }

  @Test
  void testSendEdition() throws ParserConfigurationException, TransformerException {
    var response = service.handOver(edition, RECEIVER_ADDRESS, ISSUER_ADDRESS);

    assertThat(response).usingRecursiveComparison().isEqualTo(EDITION_SAVED_MAIL);

    verify(xmlExporter).transformToXml(edition);
    verify(repository).save(EDITION_SAVED_MAIL);
    verify(mailSender)
        .sendMail(
            SENDER_ADDRESS,
            RECEIVER_ADDRESS,
            EDITION_SAVED_MAIL.mailSubject(),
            "neuris",
            EDITION_SAVED_MAIL.attachments(),
            Collections.emptyList(),
            EDITION_SAVED_MAIL.entityId().toString());
  }

  @Test
  void testSendEditionWithTwoReferencesForSameDocUnit()
      throws ParserConfigurationException, TransformerException {
    var editionWithTwoReferencesForSameDocUnit =
        LegalPeriodicalEdition.builder()
            .legalPeriodical(legalPeriodical)
            .id(TEST_UUID)
            .references(
                List.of(
                    Reference.builder()
                        .citation("2004, 1")
                        .legalPeriodical(LegalPeriodical.builder().abbreviation("ABC").build())
                        .documentationUnit(
                            RelatedDocumentationUnit.builder()
                                .documentNumber("document-number-1")
                                .build())
                        .build(),
                    Reference.builder()
                        .citation("2004, 2")
                        .legalPeriodical(LegalPeriodical.builder().abbreviation("ABC").build())
                        .documentationUnit(
                            RelatedDocumentationUnit.builder()
                                .documentNumber("document-number-1")
                                .build())
                        .build()))
            .build();

    var savedEditionWithNumberedAttachments =
        HandoverMail.builder()
            .entityId(TEST_UUID)
            .entityType(HandoverEntityType.EDITION)
            .receiverAddress(RECEIVER_ADDRESS)
            .mailSubject(EDITION_MAIL_SUBJECT)
            .attachments(
                List.of(
                    MailAttachment.builder()
                        .fileName("document-number-1_1.xml")
                        .fileContent("xml 1")
                        .build(),
                    MailAttachment.builder()
                        .fileName("document-number-1_2.xml")
                        .fileContent("xml 2")
                        .build()))
            .imageAttachments(Collections.emptyList())
            .success(true)
            .statusMessages(List.of("succeed", "succeed"))
            .handoverDate(CREATED_DATE)
            .issuerAddress(ISSUER_ADDRESS)
            .build();

    when(xmlExporter.transformToXml(editionWithTwoReferencesForSameDocUnit))
        .thenReturn(
            List.of(
                new XmlTransformationResult(
                    "xml 1", true, List.of("succeed"), "document-number-1.xml", CREATED_DATE),
                new XmlTransformationResult(
                    "xml 2", true, List.of("succeed"), "document-number-1.xml", CREATED_DATE)));

    when(repository.save(savedEditionWithNumberedAttachments))
        .thenReturn(savedEditionWithNumberedAttachments);

    var response =
        service.handOver(editionWithTwoReferencesForSameDocUnit, RECEIVER_ADDRESS, ISSUER_ADDRESS);

    assertThat(response).usingRecursiveComparison().isEqualTo(savedEditionWithNumberedAttachments);

    verify(xmlExporter).transformToXml(editionWithTwoReferencesForSameDocUnit);
    verify(repository).save(savedEditionWithNumberedAttachments);
    verify(mailSender)
        .sendMail(
            SENDER_ADDRESS,
            RECEIVER_ADDRESS,
            EDITION_SAVED_MAIL.mailSubject(),
            "neuris",
            savedEditionWithNumberedAttachments.attachments(),
            Collections.emptyList(),
            EDITION_SAVED_MAIL.entityId().toString());
  }

  @Test
  void testSendDocumentationUnit_withValidationError()
      throws ParserConfigurationException, TransformerException {
    var xmlWithValidationError =
        new XmlTransformationResult(
            "xml", false, List.of("status-message"), "test.xml", CREATED_DATE);
    var expected =
        HandoverMail.builder()
            .entityId(TEST_UUID)
            .statusMessages(List.of("status-message"))
            .success(false)
            .build();

    when(xmlExporter.transformToXml(any(Decision.class), anyBoolean()))
        .thenReturn(xmlWithValidationError);

    var response = service.handOver(decision, RECEIVER_ADDRESS, ISSUER_ADDRESS);
    assertThat(response).usingRecursiveComparison().isEqualTo(expected);

    verify(repository, never()).save(any(HandoverMail.class));
    verify(mailSender, never())
        .sendMail(
            anyString(), anyString(), anyString(), anyString(), anyList(), anyList(), anyString());
  }

  @Test
  void testSendEdition_withValidationError()
      throws ParserConfigurationException, TransformerException {
    var xmlWithValidationError =
        new XmlTransformationResult(
            "xml", false, List.of("status-message"), "test.xml", CREATED_DATE);
    var expected =
        HandoverMail.builder()
            .entityId(TEST_UUID)
            .statusMessages(List.of("status-message"))
            .success(false)
            .build();

    when(xmlExporter.transformToXml(any(LegalPeriodicalEdition.class)))
        .thenReturn(List.of(xmlWithValidationError));

    var response = service.handOver(edition, RECEIVER_ADDRESS, ISSUER_ADDRESS);
    assertThat(response).usingRecursiveComparison().isEqualTo(expected);

    verify(repository, never()).save(any(HandoverMail.class));
    verify(mailSender, never())
        .sendMail(
            anyString(), anyString(), anyString(), anyString(), anyList(), anyList(), anyString());
  }

  @Test
  void testSendDocumentationUnit_withExceptionFromXmlExporter()
      throws ParserConfigurationException, TransformerException {
    when(xmlExporter.transformToXml(any(Decision.class), anyBoolean()))
        .thenThrow(ParserConfigurationException.class);

    HandoverException ex =
        Assertions.assertThrows(
            HandoverException.class,
            () -> service.handOver(decision, RECEIVER_ADDRESS, ISSUER_ADDRESS));
    Assertions.assertEquals("Couldn't generate xml for documentationUnit.", ex.getMessage());

    verify(repository, never()).save(any(HandoverMail.class));
    verify(mailSender, never())
        .sendMail(
            anyString(), anyString(), anyString(), anyString(), anyList(), anyList(), anyString());
  }

  @Test
  void testSendEdition_withExceptionFromXmlExporter()
      throws ParserConfigurationException, TransformerException {
    when(xmlExporter.transformToXml(any(LegalPeriodicalEdition.class)))
        .thenThrow(ParserConfigurationException.class);

    HandoverException ex =
        Assertions.assertThrows(
            HandoverException.class,
            () -> service.handOver(edition, RECEIVER_ADDRESS, ISSUER_ADDRESS));
    Assertions.assertEquals("Couldn't generate xml for edition.", ex.getMessage());

    verify(repository, never()).save(any(HandoverMail.class));
    verify(mailSender, never())
        .sendMail(
            anyString(), anyString(), anyString(), anyString(), anyList(), anyList(), anyString());
  }

  @Test
  void testSendDocumentationUnit_withoutDocumentNumber() {
    decision = decision.toBuilder().documentNumber(null).build();

    // Call the method and check for the exception
    Throwable throwable =
        Assert.assertThrows(
            HandoverException.class,
            () -> service.handOver(decision, RECEIVER_ADDRESS, ISSUER_ADDRESS));

    assertThat(throwable.getMessage())
        .isEqualTo("No document number has been set in the document unit.");

    // Verify that repository.save and mailSender.sendMail were not called
    verify(repository, never()).save(any(HandoverMail.class));
    verify(mailSender, never())
        .sendMail(
            anyString(), anyString(), anyString(), anyString(), anyList(), anyList(), anyString());
  }

  @Test
  void testSendEdition_withoutEditionId() {
    edition = edition.toBuilder().id(null).build();

    // Call the method and check for the exception
    Throwable throwable =
        Assert.assertThrows(
            HandoverException.class,
            () -> service.handOver(edition, RECEIVER_ADDRESS, ISSUER_ADDRESS));

    assertThat(throwable.getMessage()).isEqualTo("No id has been set in the edition.");

    // Verify that repository.save and mailSender.sendMail were not called
    verify(repository, never()).save(any(HandoverMail.class));
    verify(mailSender, never())
        .sendMail(
            anyString(), anyString(), anyString(), anyString(), anyList(), anyList(), anyString());
  }

  @Test
  void testSend_withExceptionBySaving() {
    when(repository.save(DOC_UNIT_SAVED_MAIL)).thenThrow(IllegalArgumentException.class);

    Assert.assertThrows(
        IllegalArgumentException.class,
        () -> service.handOver(decision, RECEIVER_ADDRESS, ISSUER_ADDRESS));

    verify(repository).save(any(HandoverMail.class));
    verify(mailSender)
        .sendMail(
            anyString(), anyString(), anyString(), anyString(), anyList(), anyList(), anyString());
  }

  @Test
  void testSend_withoutToReceiverAddressSet() {
    Throwable throwable =
        Assert.assertThrows(HandoverException.class, () -> service.handOver(decision, null, null));

    assertThat(throwable.getMessage()).isEqualTo("No receiver mail address is set");

    verify(repository, never()).save(any(HandoverMail.class));
    verify(mailSender, never())
        .sendMail(
            anyString(), anyString(), anyString(), anyString(), anyList(), anyList(), anyString());
  }

  @Test
  void testSend_withExceptionBySendingEmail() {
    doThrow(HandoverException.class)
        .when(mailSender)
        .sendMail(
            SENDER_ADDRESS,
            RECEIVER_ADDRESS,
            DOC_UNIT_MAIL_SUBJECT,
            "neuris",
            Collections.singletonList(
                MailAttachment.builder().fileName("test.xml").fileContent("xml").build()),
            Collections.emptyList(),
            TEST_UUID.toString());

    Assert.assertThrows(
        HandoverException.class,
        () -> service.handOver(decision, RECEIVER_ADDRESS, ISSUER_ADDRESS));

    verify(repository, never()).save(any(HandoverMail.class));
    verify(mailSender)
        .sendMail(
            SENDER_ADDRESS,
            RECEIVER_ADDRESS,
            DOC_UNIT_MAIL_SUBJECT,
            "neuris",
            Collections.singletonList(
                MailAttachment.builder().fileName("test.xml").fileContent("xml").build()),
            Collections.emptyList(),
            TEST_UUID.toString());
  }

  // Method providing the parameters for the test
  static Stream<Arguments> provideEntityTypes() {
    return Stream.of(
        Arguments.of(HandoverEntityType.DOCUMENTATION_UNIT),
        Arguments.of(HandoverEntityType.EDITION));
  }

  @ParameterizedTest
  @MethodSource("provideEntityTypes")
  void testGetLastHandoverXmlMail(HandoverEntityType entityType) {
    List<HandoverMail> list = List.of(DOC_UNIT_SAVED_MAIL);

    when(repository.getHandoversByEntity(TEST_UUID, entityType)).thenReturn(list);

    var response = service.getHandoverResult(TEST_UUID, entityType);

    assertThat(response.get(0)).usingRecursiveComparison().isEqualTo(DOC_UNIT_SAVED_MAIL);
    verify(repository).getHandoversByEntity(TEST_UUID, entityType);
  }

  @Test
  void testSendDocumentationUnitWithImages()
      throws ParserConfigurationException, TransformerException {
    decision =
        Decision.builder()
            .uuid(TEST_UUID)
            .documentNumber("test-document-number")
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .fileNumbers(Collections.emptyList())
                    .build())
            .build();
    when(attachmentInlineRepository.findAllByDocumentationUnitId(TEST_UUID))
        .thenReturn(
            List.of(
                AttachmentInlineDTO.builder().filename("foo.png").build(),
                AttachmentInlineDTO.builder().filename("bar.jpeg").build(),
                AttachmentInlineDTO.builder().filename("baz.jpg").build(),
                AttachmentInlineDTO.builder().filename("qux.gif").build(),
                AttachmentInlineDTO.builder().filename("quux.JPEG").build(),
                AttachmentInlineDTO.builder().filename("corge.GIF").build()));

    HandoverMail savedMail =
        DOC_UNIT_SAVED_MAIL.toBuilder()
            .imageAttachments(
                List.of(
                    MailAttachmentImage.builder()
                        .fileName("test-document-number_ds_foo.png")
                        .build(),
                    MailAttachmentImage.builder()
                        .fileName("test-document-number_ds_bar.jpg")
                        .build(),
                    MailAttachmentImage.builder()
                        .fileName("test-document-number_ds_baz.jpg")
                        .build(),
                    MailAttachmentImage.builder()
                        .fileName("test-document-number_ds_qux.gif")
                        .build(),
                    MailAttachmentImage.builder()
                        .fileName("test-document-number_ds_quux.jpg")
                        .build(),
                    MailAttachmentImage.builder()
                        .fileName("test-document-number_ds_corge.gif")
                        .build()))
            .build();

    when(repository.save(savedMail)).thenReturn(savedMail);

    var response = service.handOver(decision, RECEIVER_ADDRESS, ISSUER_ADDRESS);

    assertThat(response).usingRecursiveComparison().isEqualTo(savedMail);

    verify(xmlExporter)
        .transformToXml(
            decision.toBuilder()
                .documentNumber("TESTtest-document-number")
                .coreData(
                    decision.coreData().toBuilder()
                        .court(
                            Court.builder()
                                .label("VGH Mannheim")
                                .location("Mannheim")
                                .type("VGH")
                                .build())
                        .fileNumbers(List.of("TEST"))
                        .build())
                .build(),
            false);

    verify(repository).save(savedMail);
    verify(mailSender)
        .sendMail(
            SENDER_ADDRESS,
            RECEIVER_ADDRESS,
            DOC_UNIT_SAVED_MAIL.mailSubject(),
            "neuris",
            Collections.singletonList(
                MailAttachment.builder()
                    .fileName(savedMail.attachments().getFirst().fileName())
                    .fileContent(savedMail.attachments().getFirst().fileContent())
                    .build()),
            List.of(
                MailAttachmentImage.builder().fileName("test-document-number_ds_foo.png").build(),
                MailAttachmentImage.builder().fileName("test-document-number_ds_bar.jpg").build(),
                MailAttachmentImage.builder().fileName("test-document-number_ds_baz.jpg").build(),
                MailAttachmentImage.builder().fileName("test-document-number_ds_qux.gif").build(),
                MailAttachmentImage.builder().fileName("test-document-number_ds_quux.jpg").build(),
                MailAttachmentImage.builder()
                    .fileName("test-document-number_ds_corge.gif")
                    .build()),
            DOC_UNIT_SAVED_MAIL.entityId().toString());
  }
}
