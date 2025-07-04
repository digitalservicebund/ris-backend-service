package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.FrbrElement;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.FrbrThis;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Identification;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Judgment;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Meta;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentRepository;
import de.bund.digitalservice.ris.caselaw.adapter.exception.BucketException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.LdmlTransformationException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.PublishException;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.LongTexts;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceeding;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import de.bund.digitalservice.ris.caselaw.domain.ShortTexts;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class PrototypePortalPublicationServiceTest {

  @MockitoBean private DocumentationUnitRepository documentationUnitRepository;
  @MockitoBean private AttachmentRepository attachmentRepository;
  @MockitoBean private PrototypePortalBucket portalBucket;
  @MockitoBean private XmlUtilService xmlUtilService;
  @MockitoBean private ObjectMapper objectMapper;
  @MockitoBean private PortalTransformer portalTransformer;
  @MockitoBean private RiiService riiService;

  private static Decision testDocumentUnit;
  private static String testDocumentNumber;
  private static CaseLawLdml testLdml;

  private PrototypePortalPublicationService subject;

  @BeforeAll
  static void setUpBeforeClass() {
    PreviousDecision related1 =
        PreviousDecision.builder()
            .decisionDate(LocalDate.of(2020, 1, 1))
            .court(Court.builder().type("Test court type").build())
            .documentType(DocumentType.builder().label("Test decision type").build())
            .fileNumber("Test file number")
            .documentNumber("Test document number 1")
            .build();
    PreviousDecision related2 =
        related1.toBuilder().documentNumber("Test document number 2").build();

    testDocumentNumber = "TEST123456789";
    testDocumentUnit =
        Decision.builder()
            .uuid(UUID.randomUUID())
            .coreData(
                CoreData.builder()
                    .ecli("testecli")
                    .court(
                        Court.builder().type("testCourtType").location("testCourtLocation").build())
                    .documentType(
                        DocumentType.builder().label("testDocumentTypeAbbreviation").build())
                    .legalEffect("ja")
                    .fileNumbers(List.of("testFileNumber"))
                    .decisionDate(LocalDate.of(2020, 1, 1))
                    .build())
            .documentNumber(testDocumentNumber)
            .longTexts(LongTexts.builder().caseFacts("<p>Example content 1</p>").build())
            .shortTexts(ShortTexts.builder().build())
            .previousDecisions(List.of(related1, related2))
            .build();

    testLdml =
        CaseLawLdml.builder()
            .judgment(
                Judgment.builder()
                    .meta(
                        Meta.builder()
                            .identification(
                                Identification.builder()
                                    .frbrWork(
                                        FrbrElement.builder()
                                            .frbrThis(new FrbrThis(testDocumentNumber))
                                            .build())
                                    .build())
                            .build())
                    .build())
            .build();
  }

  @BeforeEach
  void mockReset() throws JsonProcessingException {
    subject =
        new PrototypePortalPublicationService(
            documentationUnitRepository,
            attachmentRepository,
            xmlUtilService,
            portalBucket,
            objectMapper,
            portalTransformer,
            riiService);
    when(objectMapper.writeValueAsString(any())).thenReturn("");
  }

  @Test
  void publish_documentNumberDoesNotExist_shouldThrowDocumentationUnitNotExistsException()
      throws DocumentationUnitNotExistsException {
    String invalidDocumentNumber = "abcd123456789";
    when(documentationUnitRepository.findByDocumentNumber(invalidDocumentNumber))
        .thenThrow(new DocumentationUnitNotExistsException(invalidDocumentNumber));

    assertThatExceptionOfType(DocumentationUnitNotExistsException.class)
        .isThrownBy(() -> subject.publishDocumentationUnit(invalidDocumentNumber))
        .withMessageContaining("Documentation unit does not exist: " + invalidDocumentNumber);
  }

  @Test
  void publish_withMissingCoreDataLdml_shouldThrowLdmlTransformationException()
      throws DocumentationUnitNotExistsException {
    when(documentationUnitRepository.findByDocumentNumber(testDocumentNumber))
        .thenReturn(testDocumentUnit);
    when(portalTransformer.transformToLdml(testDocumentUnit))
        .thenThrow(new LdmlTransformationException("LDML validation failed.", new Exception()));

    assertThatExceptionOfType(LdmlTransformationException.class)
        .isThrownBy(() -> subject.publishDocumentationUnit(testDocumentNumber))
        .withMessageContaining("LDML validation failed.");
  }

  @Test
  void publish_withStringParsingIssue_shouldThrowLdmlTransformationException()
      throws DocumentationUnitNotExistsException {
    when(documentationUnitRepository.findByDocumentNumber(testDocumentNumber))
        .thenReturn(testDocumentUnit);
    when(xmlUtilService.ldmlToString(any())).thenReturn(Optional.empty());

    assertThatExceptionOfType(LdmlTransformationException.class)
        .isThrownBy(() -> subject.publishDocumentationUnit(testDocumentNumber))
        .withMessageContaining("Could not parse transformed LDML as string.");
  }

  @Test
  void publish_withValidData_shouldSaveToBucket() throws DocumentationUnitNotExistsException {
    String transformed = "ldml";
    when(documentationUnitRepository.findByDocumentNumber(testDocumentNumber))
        .thenReturn(testDocumentUnit);
    when(portalTransformer.transformToLdml(testDocumentUnit)).thenReturn(testLdml);
    when(xmlUtilService.ldmlToString(any())).thenReturn(Optional.of(transformed));

    subject.publishDocumentationUnit(testDocumentNumber);

    verify(portalBucket, times(1)).save(testDocumentNumber + ".xml", transformed);
    verify(portalBucket, times(1))
        .save(testDocumentNumber + "/" + testDocumentNumber + ".xml", transformed);
  }

  @Test
  void publish_withValidData_shouldThrowBucketException()
      throws DocumentationUnitNotExistsException {
    String transformed = "ldml";
    when(documentationUnitRepository.findByDocumentNumber(testDocumentNumber))
        .thenReturn(testDocumentUnit);
    when(portalTransformer.transformToLdml(testDocumentUnit)).thenReturn(testLdml);
    when(xmlUtilService.ldmlToString(any())).thenReturn(Optional.of(transformed));
    doThrow(BucketException.class).when(portalBucket).save(anyString(), anyString());

    assertThatExceptionOfType(PublishException.class)
        .isThrownBy(() -> subject.publishDocumentationUnit(testDocumentNumber))
        .withMessageContaining("Could not save LDML to bucket.");
  }

  @Test
  void publish_withPendingProceeding_shouldDoNothing() throws DocumentationUnitNotExistsException {
    PendingProceeding pendingProceeding = PendingProceeding.builder().build();
    when(documentationUnitRepository.findByDocumentNumber(testDocumentNumber))
        .thenReturn(pendingProceeding);

    subject.publishDocumentationUnit(testDocumentNumber);

    verify(portalBucket, never()).save(anyString(), anyString());
    verify(xmlUtilService, never()).ldmlToString(any());
  }

  @Test
  void delete_shouldDeleteFromBucket() {
    when(portalBucket.getAllFilenamesByPath(testDocumentNumber + "/"))
        .thenReturn(List.of(testDocumentNumber + "/" + testDocumentNumber + ".xml"));
    subject.deleteDocumentationUnit(testDocumentNumber);

    verify(portalBucket, times(1)).delete(testDocumentNumber + ".xml");
    verify(portalBucket, times(1)).delete(testDocumentNumber + "/" + testDocumentNumber + ".xml");
  }

  @Test
  void delete_shouldThrow() {
    subject.deleteDocumentationUnit(testDocumentNumber);
    when(portalBucket.getAllFilenamesByPath(testDocumentNumber + "/"))
        .thenReturn(List.of(testDocumentNumber + "/" + testDocumentNumber + ".xml"));
    doThrow(BucketException.class)
        .when(portalBucket)
        .delete(testDocumentNumber + "/" + testDocumentNumber + ".xml");

    assertThatExceptionOfType(PublishException.class)
        .isThrownBy(() -> subject.deleteDocumentationUnit(testDocumentNumber))
        .withMessageContaining("Could not delete LDML from bucket.");
  }

  @Test
  void delete_shouldThrowTemp() {
    doThrow(BucketException.class).when(portalBucket).delete(testDocumentNumber + ".xml");

    assertThatExceptionOfType(PublishException.class)
        .isThrownBy(() -> subject.deleteDocumentationUnit(testDocumentNumber))
        .withMessageContaining("Could not delete LDML from bucket.");
  }

  @Test
  void uploadChangelog_shouldDoNothing() {
    subject.uploadChangelog(List.of(), List.of());

    verify(portalBucket, never()).save(contains("changelogs/"), anyString());
  }

  // currently disabled for prototype
  //  @Test
  //  void uploadChangelog_shouldThrowException() throws JsonProcessingException {
  //    doThrow(JsonProcessingException.class).when(objectMapper).writeValueAsString(any());
  //
  //    assertThatExceptionOfType(JsonProcessingException.class)
  //        .isThrownBy(() -> subject.uploadChangelog(List.of(), List.of()));
  //  }

  @Test
  void sanityCheck_shouldDeleteDocumentNumbersInPortalButNotInRii() throws JsonProcessingException {
    when(riiService.fetchRiiDocumentNumbers()).thenReturn(List.of("123", "456"));
    when(portalBucket.getAllFilenames()).thenReturn(List.of("123.xml", "456.xml", "789.xml"));
    ArgumentCaptor<String> fileNameCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> fileContentCaptor = ArgumentCaptor.forClass(String.class);
    when(objectMapper.writeValueAsString(new ChangelogUpdateDelete(null, List.of("789.xml"))))
        .thenReturn(
            """
                {"deleted":["789.xml"]}""");

    subject.logPortalPublicationSanityCheck();

    verify(portalBucket).delete("789.xml");
    verify(portalBucket).save(fileNameCaptor.capture(), fileContentCaptor.capture());
    assertThat(fileNameCaptor.getValue()).contains("changelogs");
    assertThat(fileContentCaptor.getValue())
        .isEqualTo(
            """
                {"deleted":["789.xml"]}""");
  }
}
