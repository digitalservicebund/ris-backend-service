package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.bund.digitalservice.ris.caselaw.adapter.exception.BucketException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.LdmlTransformationException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.PublishException;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
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
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PrototypePortalPublicationServiceTest {

  static DocumentationUnitRepository documentationUnitRepository;
  static PrototypePortalBucket prototypePortalBucket;
  static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
  static XmlUtilService xmlUtilService;
  static PrototypePortalPublicationService subject;
  static DocumentationUnit testDocumentUnit;
  static String testDocumentNumber;
  static ObjectMapper objectMapper;
  static RiiService riiService;

  @BeforeAll
  static void setUpBeforeClass() {
    documentationUnitRepository = mock(DocumentationUnitRepository.class);
    prototypePortalBucket = mock(PrototypePortalBucket.class);
    objectMapper = mock(ObjectMapper.class);
    xmlUtilService = mock(XmlUtilService.class);
    riiService = mock(RiiService.class);
    subject =
        new PrototypePortalPublicationService(
            documentationUnitRepository,
            xmlUtilService,
            documentBuilderFactory,
            prototypePortalBucket,
            objectMapper,
            riiService);

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
        DocumentationUnit.builder()
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
  }

  @BeforeEach
  void mockReset() throws JsonProcessingException {
    Mockito.reset(prototypePortalBucket);
    Mockito.reset(objectMapper);
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
    DocumentationUnit invalidTestDocumentUnit =
        testDocumentUnit.toBuilder().coreData(CoreData.builder().build()).build();
    when(documentationUnitRepository.findByDocumentNumber(testDocumentNumber))
        .thenReturn(invalidTestDocumentUnit);

    var documentNumber = invalidTestDocumentUnit.documentNumber();
    assertThatExceptionOfType(LdmlTransformationException.class)
        .isThrownBy(() -> subject.publishDocumentationUnit(documentNumber))
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
    when(xmlUtilService.ldmlToString(any())).thenReturn(Optional.of(transformed));

    subject.publishDocumentationUnit(testDocumentNumber);

    verify(prototypePortalBucket, times(1)).save(contains(testDocumentNumber), eq(transformed));
  }

  @Test
  void publish_withValidData_shouldThrowBucketException()
      throws DocumentationUnitNotExistsException {
    String transformed = "ldml";
    when(documentationUnitRepository.findByDocumentNumber(testDocumentNumber))
        .thenReturn(testDocumentUnit);
    when(xmlUtilService.ldmlToString(any())).thenReturn(Optional.of(transformed));
    doThrow(BucketException.class).when(prototypePortalBucket).save(anyString(), anyString());

    assertThatExceptionOfType(PublishException.class)
        .isThrownBy(() -> subject.publishDocumentationUnit(testDocumentNumber))
        .withMessageContaining("Could not save LDML to bucket.");
  }

  @Test
  void publish_withPendingProceeding_shouldDoNothing() throws DocumentationUnitNotExistsException {
    PendingProceeding pendingProceeding = PendingProceeding.builder().build();
    when(documentationUnitRepository.findByDocumentNumber(testDocumentNumber))
        .thenReturn(pendingProceeding);

    verify(prototypePortalBucket, never()).save(anyString(), anyString());
    verify(xmlUtilService, never()).ldmlToString(any());
  }

  @Test
  void delete_shouldDeleteFromBucket() {
    subject.deleteDocumentationUnit(testDocumentNumber);

    verify(prototypePortalBucket, times(1)).delete(testDocumentNumber + ".xml");
  }

  @Test
  void delete_shouldThrow() {
    doThrow(BucketException.class).when(prototypePortalBucket).delete(testDocumentNumber + ".xml");

    assertThatExceptionOfType(PublishException.class)
        .isThrownBy(() -> subject.deleteDocumentationUnit(testDocumentNumber))
        .withMessageContaining("Could not delete LDML from bucket.");
  }

  @Test
  void uploadChangelog_shouldDoNothing() {
    subject.uploadChangelog(List.of(), List.of());

    verify(prototypePortalBucket, never()).save(contains("changelogs/"), anyString());
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
    when(prototypePortalBucket.getAllFilenames())
        .thenReturn(List.of("123.xml", "456.xml", "789.xml"));
    ArgumentCaptor<String> fileNameCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> fileContentCaptor = ArgumentCaptor.forClass(String.class);
    when(objectMapper.writeValueAsString(new Changelog(null, List.of("789.xml"), null)))
        .thenReturn(
            """
                {"deleted":["789.xml"]}""");

    subject.logPortalPublicationSanityCheck();

    verify(prototypePortalBucket).delete("789.xml");
    verify(prototypePortalBucket).save(fileNameCaptor.capture(), fileContentCaptor.capture());
    assertThat(fileNameCaptor.getValue()).contains("changelogs");
    assertThat(fileContentCaptor.getValue())
        .isEqualTo(
            """
                {"deleted":["789.xml"]}""");
  }
}
