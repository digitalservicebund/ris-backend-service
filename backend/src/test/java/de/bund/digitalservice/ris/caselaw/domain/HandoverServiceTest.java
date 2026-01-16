package de.bund.digitalservice.ris.caselaw.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentInlineDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentInlineRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import jakarta.validation.Validator;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({HandoverService.class})
class HandoverServiceTest {
  private static final UUID TEST_UUID = UUID.fromString("88888888-4444-4444-4444-121212121212");
  private static final String ISSUER_ADDRESS = "test-issuer@exporter.neuris";

  @MockitoSpyBean private HandoverService service;
  @MockitoBean private DocumentationUnitHistoryLogService historyLogService;

  @MockitoBean private DatabaseDocumentationUnitRepository documentationUnitRepository;

  @MockitoBean private LegalPeriodicalEditionRepository editionRepository;
  @MockitoBean private DocumentationUnitRepository repository;
  @MockitoBean private DocumentationUnitService documentationUnitService;
  @MockitoBean private DocumentNumberService documentNumberService;
  @MockitoBean private DocumentNumberRecyclingService documentNumberRecyclingService;
  @MockitoBean private MailService mailService;
  @MockitoBean private HandoverReportRepository handoverReportRepository;
  @MockitoBean private DeltaMigrationRepository deltaMigrationRepository;
  @MockitoBean private DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @MockitoBean private AttachmentService attachmentService;
  @MockitoBean private Validator validator;
  @MockitoBean private AttachmentInlineRepository attachmentInlineRepository;
  @MockitoBean private FeatureToggleService featureToggleService;

  @Test
  void testHandoverByEmail() throws DocumentationUnitNotExistsException {
    when(repository.findByUuid(TEST_UUID)).thenReturn(Decision.builder().build());
    HandoverMail handoverMail =
        HandoverMail.builder()
            .entityId(TEST_UUID)
            .entityType(HandoverEntityType.DOCUMENTATION_UNIT)
            .receiverAddress("receiver address")
            .mailSubject("subject")
            .attachments(
                List.of(MailAttachment.builder().fileName("filename").fileContent("xml").build()))
            .success(true)
            .statusMessages(List.of("status messages"))
            .handoverDate(Instant.now())
            .build();
    when(mailService.handOver(eq(Decision.builder().build()), anyString(), anyString()))
        .thenReturn(handoverMail);
    var mailResponse = service.handoverDocumentationUnitAsMail(TEST_UUID, ISSUER_ADDRESS, null);
    assertThat(mailResponse).usingRecursiveComparison().isEqualTo(handoverMail);
    verify(repository).findByUuid(TEST_UUID);
    verify(mailService).handOver(eq(Decision.builder().build()), anyString(), anyString());
  }

  @Test
  void testEditionHandoverByEmail() throws IOException {
    when(editionRepository.findById(TEST_UUID))
        .thenReturn(Optional.of(LegalPeriodicalEdition.builder().build()));
    HandoverMail handoverMail =
        HandoverMail.builder()
            .entityId(TEST_UUID)
            .entityType(HandoverEntityType.EDITION)
            .receiverAddress("receiver address")
            .mailSubject("subject")
            .attachments(
                List.of(
                    MailAttachment.builder().fileName("filename 1").fileContent("xml1").build(),
                    MailAttachment.builder().fileName("filename 2").fileContent("xml2").build()))
            .success(true)
            .statusMessages(List.of("status messages"))
            .handoverDate(Instant.now())
            .build();
    when(mailService.handOver(
            eq(LegalPeriodicalEdition.builder().build()), anyString(), anyString()))
        .thenReturn(handoverMail);
    var mailResponse = service.handoverEditionAsMail(TEST_UUID, ISSUER_ADDRESS);
    assertThat(mailResponse).usingRecursiveComparison().isEqualTo(handoverMail);
    verify(editionRepository).findById(TEST_UUID);
    verify(mailService)
        .handOver(eq(LegalPeriodicalEdition.builder().build()), anyString(), anyString());
  }

  @Test
  void testHandoverByEmail_withoutDocumentationUnitForUuid()
      throws DocumentationUnitNotExistsException {

    when(repository.findByUuid(TEST_UUID)).thenThrow(DocumentationUnitNotExistsException.class);

    Assertions.assertThrows(
        DocumentationUnitNotExistsException.class,
        () -> service.handoverDocumentationUnitAsMail(TEST_UUID, ISSUER_ADDRESS, null));
    verify(repository).findByUuid(TEST_UUID);
    verify(mailService, never()).handOver(eq(Decision.builder().build()), anyString(), anyString());
  }

  @Test
  void testHandoverByEmail_withImagesAndFeatureFlagDisabled_shouldThrowHandoverException()
      throws HandoverException, DocumentationUnitNotExistsException {
    Decision decision =
        Decision.builder()
            .uuid(TEST_UUID)
            .status(Status.builder().publicationStatus(PublicationStatus.UNPUBLISHED).build())
            .managementData(ManagementData.builder().build())
            .build();
    when(featureToggleService.isEnabled("neuris.image-handover")).thenReturn(false);
    when(repository.findByUuid(TEST_UUID)).thenReturn(decision);
    when(attachmentInlineRepository.findAllByDocumentationUnitId(TEST_UUID))
        .thenReturn(List.of(AttachmentInlineDTO.builder().format("jpg").build()));

    assertThatThrownBy(
            () -> service.handoverDocumentationUnitAsMail(TEST_UUID, ISSUER_ADDRESS, null))
        .isInstanceOf(HandoverException.class)
        .hasMessageContaining("Handing over documentation unit with images is not allowed");

    verify(mailService, never()).handOver(eq(Decision.builder().build()), anyString(), anyString());
  }

  @Test
  void testHandoverByEmail_withImagesAndIsPublished_shouldThrowHandoverException()
      throws HandoverException, DocumentationUnitNotExistsException {
    Decision decision =
        Decision.builder()
            .uuid(TEST_UUID)
            .status(Status.builder().publicationStatus(PublicationStatus.PUBLISHED).build())
            .managementData(ManagementData.builder().build())
            .build();
    when(featureToggleService.isEnabled("neuris.image-handover")).thenReturn(true);
    when(repository.findByUuid(TEST_UUID)).thenReturn(decision);
    when(attachmentInlineRepository.findAllByDocumentationUnitId(TEST_UUID))
        .thenReturn(List.of(AttachmentInlineDTO.builder().format("jpg").build()));

    assertThatThrownBy(
            () -> service.handoverDocumentationUnitAsMail(TEST_UUID, ISSUER_ADDRESS, null))
        .isInstanceOf(HandoverException.class)
        .hasMessageContaining(
            "Handing over with images is only allowed for decisions created in NeuRIS");

    verify(mailService, never()).handOver(eq(Decision.builder().build()), anyString(), anyString());
  }

  @Test
  void testHandoverByEmail_withImagesAndIsMigrated_shouldThrowHandoverException()
      throws HandoverException, DocumentationUnitNotExistsException {
    Decision decision =
        Decision.builder()
            .uuid(TEST_UUID)
            .status(Status.builder().publicationStatus(PublicationStatus.UNPUBLISHED).build())
            .managementData(ManagementData.builder().createdByName("Migration").build())
            .build();
    when(featureToggleService.isEnabled("neuris.image-handover")).thenReturn(true);
    when(repository.findByUuid(TEST_UUID)).thenReturn(decision);
    when(attachmentInlineRepository.findAllByDocumentationUnitId(TEST_UUID))
        .thenReturn(List.of(AttachmentInlineDTO.builder().format("jpg").build()));

    assertThatThrownBy(
            () -> service.handoverDocumentationUnitAsMail(TEST_UUID, ISSUER_ADDRESS, null))
        .isInstanceOf(HandoverException.class)
        .hasMessageContaining(
            "Handing over with images is only allowed for decisions created in NeuRIS");

    verify(mailService, never()).handOver(eq(Decision.builder().build()), anyString(), anyString());
  }

  @Test
  void testHandoverByEmail_withImagesInWrongFormat_shouldThrowHandoverException()
      throws HandoverException, DocumentationUnitNotExistsException {
    Decision decision =
        Decision.builder()
            .uuid(TEST_UUID)
            .status(Status.builder().publicationStatus(PublicationStatus.UNPUBLISHED).build())
            .managementData(ManagementData.builder().build())
            .build();
    when(featureToggleService.isEnabled("neuris.image-handover")).thenReturn(true);
    when(repository.findByUuid(TEST_UUID)).thenReturn(decision);
    when(attachmentInlineRepository.findAllByDocumentationUnitId(TEST_UUID))
        .thenReturn(
            List.of(
                AttachmentInlineDTO.builder().format("jpg").build(),
                AttachmentInlineDTO.builder().format("wmf").build()));

    assertThatThrownBy(
            () -> service.handoverDocumentationUnitAsMail(TEST_UUID, ISSUER_ADDRESS, null))
        .isInstanceOf(HandoverException.class)
        .hasMessageContaining(
            "Handing over images is only allowed for jpg/jpeg, png or gif format");

    verify(mailService, never()).handOver(eq(Decision.builder().build()), anyString(), anyString());
  }

  @Test
  void testHandoverEditionByEmail_withoutEditionForUuid() {

    when(editionRepository.findById(TEST_UUID)).thenReturn(Optional.empty());

    Assertions.assertThrows(
        IOException.class, () -> service.handoverEditionAsMail(TEST_UUID, ISSUER_ADDRESS));
    verify(editionRepository).findById(TEST_UUID);
    verify(mailService, never())
        .handOver(eq(LegalPeriodicalEdition.builder().build()), anyString(), anyString());
  }

  @Test
  void testGetLastHandoverMailForDocumentationUnit() {
    HandoverMail handoverMail =
        HandoverMail.builder()
            .entityId(TEST_UUID)
            .entityType(HandoverEntityType.DOCUMENTATION_UNIT)
            .receiverAddress("receiver address")
            .mailSubject("subject")
            .mailSubject("subject")
            .attachments(
                List.of(MailAttachment.builder().fileName("filename").fileContent("xml").build()))
            .success(true)
            .statusMessages(List.of("message"))
            .handoverDate(Instant.now().minus(2, java.time.temporal.ChronoUnit.DAYS))
            .build();
    when(mailService.getHandoverResult(TEST_UUID, HandoverEntityType.DOCUMENTATION_UNIT))
        .thenReturn(List.of(handoverMail));
    when(handoverReportRepository.getAllByEntityId(TEST_UUID)).thenReturn(Collections.emptyList());
    DeltaMigration deltaMigration =
        DeltaMigration.builder()
            .migratedDate(Instant.now().minus(1, java.time.temporal.ChronoUnit.DAYS))
            .xml("<test><element></element></test>")
            .build();
    when(deltaMigrationRepository.getLatestMigration(TEST_UUID)).thenReturn(deltaMigration);

    var actual = service.getEventLog(TEST_UUID, HandoverEntityType.DOCUMENTATION_UNIT);
    assertThat(actual.get(1)).usingRecursiveComparison().isEqualTo(handoverMail);
    assertThat(actual.get(0))
        .usingRecursiveComparison()
        .isEqualTo(
            deltaMigration.toBuilder()
                .xml(
                    """
                <?xml version="1.0" encoding="UTF-8"?>
                <test>
                   <element/>
                </test>
                """)
                .build());

    verify(mailService).getHandoverResult(TEST_UUID, HandoverEntityType.DOCUMENTATION_UNIT);
    verify(deltaMigrationRepository).getLatestMigration(TEST_UUID);
  }

  @Test
  void testGetLastHandoverMailForEdition() {
    HandoverMail handoverMail =
        HandoverMail.builder()
            .entityId(TEST_UUID)
            .entityType(HandoverEntityType.EDITION)
            .receiverAddress("receiver address")
            .mailSubject("subject")
            .mailSubject("subject")
            .attachments(
                List.of(MailAttachment.builder().fileName("filename").fileContent("xml").build()))
            .success(true)
            .statusMessages(List.of("message"))
            .handoverDate(Instant.now().minus(2, java.time.temporal.ChronoUnit.DAYS))
            .build();
    when(mailService.getHandoverResult(TEST_UUID, HandoverEntityType.EDITION))
        .thenReturn(List.of(handoverMail));

    var actual = service.getEventLog(TEST_UUID, HandoverEntityType.EDITION);
    assertThat(actual.get(0)).usingRecursiveComparison().isEqualTo(handoverMail);

    verify(mailService).getHandoverResult(TEST_UUID, HandoverEntityType.EDITION);
  }

  @Test
  void testGetLastMigrated() {
    DeltaMigration deltaMigration =
        DeltaMigration.builder()
            .migratedDate(Instant.now().minus(1, java.time.temporal.ChronoUnit.DAYS))
            .xml("<test><element></element></test>")
            .build();
    when(deltaMigrationRepository.getLatestMigration(TEST_UUID)).thenReturn(deltaMigration);

    var actual = service.getEventLog(TEST_UUID, HandoverEntityType.DOCUMENTATION_UNIT);
    assertThat(actual.get(0))
        .usingRecursiveComparison()
        .isEqualTo(
            deltaMigration.toBuilder()
                .xml(
"""
<?xml version="1.0" encoding="UTF-8"?>
<test>
   <element/>
</test>
""")
                .build());

    verify(deltaMigrationRepository).getLatestMigration(TEST_UUID);
  }

  static Stream<Arguments> provideEntityTypes() {
    return Stream.of(
        Arguments.of(HandoverEntityType.DOCUMENTATION_UNIT),
        Arguments.of(HandoverEntityType.EDITION));
  }

  @ParameterizedTest
  @MethodSource("provideEntityTypes")
  void testGetLastHandoverReport(HandoverEntityType entityType) {
    HandoverReport report = new HandoverReport(TEST_UUID, "<html></html>", Instant.now());

    when(handoverReportRepository.getAllByEntityId(TEST_UUID)).thenReturn(List.of(report));
    when(mailService.getHandoverResult(TEST_UUID, entityType)).thenReturn(List.of());
    when(deltaMigrationRepository.getLatestMigration(TEST_UUID)).thenReturn(null);

    var events = service.getEventLog(TEST_UUID, entityType);

    assertThat(events.get(0)).usingRecursiveComparison().isEqualTo(report);
    verify(mailService).getHandoverResult(TEST_UUID, entityType);
  }

  @ParameterizedTest
  @MethodSource("provideEntityTypes")
  void testGetSortedEventLog(HandoverEntityType entityType) {
    Instant newest = Instant.now();
    Instant secondNewest = newest.minusSeconds(61);
    Instant thirdNewest = secondNewest.minusSeconds(61);
    Instant fourthNewest = thirdNewest.minusSeconds(61);
    Instant fifthNewest = fourthNewest.minusSeconds(61);

    HandoverReport report1 = new HandoverReport(TEST_UUID, "<html></html>", newest);

    HandoverMail xml1 =
        HandoverMail.builder()
            .entityId(TEST_UUID)
            .entityType(entityType)
            .receiverAddress("receiver address")
            .mailSubject("subject")
            .mailSubject("subject")
            .attachments(
                List.of(MailAttachment.builder().fileName("filename").fileContent("xml").build()))
            .success(true)
            .statusMessages(List.of("message"))
            .handoverDate(secondNewest)
            .build();

    HandoverReport report2 = new HandoverReport(TEST_UUID, "<html></html>", thirdNewest);

    HandoverMail xml2 =
        HandoverMail.builder()
            .entityId(TEST_UUID)
            .entityType(entityType)
            .receiverAddress("receiver address")
            .mailSubject("subject")
            .mailSubject("subject")
            .attachments(
                List.of(MailAttachment.builder().fileName("filename").fileContent("xml").build()))
            .success(true)
            .statusMessages(List.of("message"))
            .handoverDate(fourthNewest)
            .build();

    DeltaMigration deltaMigration = DeltaMigration.builder().migratedDate(fifthNewest).build();

    when(handoverReportRepository.getAllByEntityId(TEST_UUID))
        .thenReturn(List.of(report2, report1));
    when(mailService.getHandoverResult(TEST_UUID, entityType)).thenReturn(List.of(xml2, xml1));
    when(deltaMigrationRepository.getLatestMigration(TEST_UUID)).thenReturn(deltaMigration);

    List<EventRecord> list = service.getEventLog(TEST_UUID, entityType);
    assertThat(list).hasSize(5);
    assertThat(list.get(0)).usingRecursiveComparison().isEqualTo(report1);
    assertThat(list.get(1)).usingRecursiveComparison().isEqualTo(xml1);
    assertThat(list.get(2)).usingRecursiveComparison().isEqualTo(report2);
    assertThat(list.get(3)).usingRecursiveComparison().isEqualTo(xml2);
    assertThat(list.get(4)).usingRecursiveComparison().isEqualTo(deltaMigration);
    verify(mailService).getHandoverResult(TEST_UUID, entityType);
  }

  @Test
  void testPreviewXml() throws DocumentationUnitNotExistsException {
    Decision testDecision = Decision.builder().build();
    XmlTransformationResult mockXmlTransformationResult =
        new XmlTransformationResult("some xml", true, List.of("success"), "foo.xml", Instant.now());
    when(repository.findByUuid(TEST_UUID)).thenReturn(testDecision);
    when(mailService.getXmlPreview(testDecision, true)).thenReturn(mockXmlTransformationResult);

    Assertions.assertEquals(mockXmlTransformationResult, service.createPreviewXml(TEST_UUID, true));
  }

  @Test
  void testPreviewEditionXml() throws IOException {
    LegalPeriodicalEdition testEdition = LegalPeriodicalEdition.builder().build();
    List<XmlTransformationResult> mockXmlTransformationResult =
        List.of(
            new XmlTransformationResult(
                "Fundstelle 1 XML", true, List.of("success"), "foo1.xml", Instant.now()),
            new XmlTransformationResult(
                "Fundstelle 2 XML", true, List.of("success"), "foo2.xml", Instant.now()));
    when(editionRepository.findById(TEST_UUID)).thenReturn(Optional.ofNullable(testEdition));
    when(mailService.getXmlPreview(testEdition)).thenReturn(mockXmlTransformationResult);

    Assertions.assertEquals(
        mockXmlTransformationResult, service.createEditionPreviewXml(TEST_UUID));
  }

  @Test
  void testPrettifyXml() {
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><child>value</child></root>";
    String prettyXml = HandoverService.prettifyXml(xml);
    assertThat(prettyXml)
        .isEqualTo(
"""
<?xml version="1.0" encoding="UTF-8"?>
<root>
   <child>value</child>
</root>
""");
  }

  @Test
  void testPrettifyXml_withNoIndexTags_shouldNotSplitLines() {
    String xml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><p>noindex <noindex>tags</noindex> should be in one line</p></root>";

    String prettyXml = HandoverService.prettifyXml(xml);
    assertThat(prettyXml)
        .isEqualTo(
"""
<?xml version="1.0" encoding="UTF-8"?>
<root>
   <p>noindex <noindex>tags</noindex> should be in one line</p>
</root>
""");
  }
}
