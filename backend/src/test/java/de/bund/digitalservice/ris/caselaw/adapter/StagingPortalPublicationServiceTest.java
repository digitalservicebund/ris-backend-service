package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
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
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import de.bund.digitalservice.ris.caselaw.domain.ShortTexts;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilderFactory;
import net.sf.saxon.TransformerFactoryImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StagingPortalPublicationServiceTest {

  static DocumentationUnitRepository documentationUnitRepository;
  static InternalPortalBucket caseLawBucket;
  static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
  static XmlUtilService xmlUtilService = new XmlUtilService(new TransformerFactoryImpl());
  static StagingPortalPublicationService stagingPortalPublicationService;
  static DocumentationUnit testDocumentUnit;
  static UUID testUUID;
  static ObjectMapper objectMapper;

  @BeforeAll
  static void setUpBeforeClass() {
    documentationUnitRepository = mock(DocumentationUnitRepository.class);
    caseLawBucket = mock(InternalPortalBucket.class);
    objectMapper = mock(ObjectMapper.class);
    stagingPortalPublicationService =
        new StagingPortalPublicationService(
            documentationUnitRepository,
            xmlUtilService,
            documentBuilderFactory,
            caseLawBucket,
            objectMapper);

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

    testUUID = UUID.randomUUID();
    testDocumentUnit =
        DocumentationUnit.builder()
            .uuid(testUUID)
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
            .documentNumber("testDocumentNumber")
            .longTexts(LongTexts.builder().caseFacts("<p>Example content 1</p>").build())
            .shortTexts(ShortTexts.builder().build())
            .previousDecisions(List.of(related1, related2))
            .build();
  }

  @BeforeEach
  void mockReset() throws JsonProcessingException {
    Mockito.reset(caseLawBucket);
    Mockito.reset(objectMapper);
    when(objectMapper.writeValueAsString(any())).thenReturn("");
  }

  @Test
  @DisplayName("Should publish single documentation unit succesfully")
  void publishSuccessfully() throws DocumentationUnitNotExistsException {
    UUID documentationUnitId = UUID.randomUUID();
    when(documentationUnitRepository.findByUuid(documentationUnitId)).thenReturn(testDocumentUnit);

    stagingPortalPublicationService.publishDocumentationUnitWithChangelog(documentationUnitId);

    verify(caseLawBucket, times(2)).save(anyString(), anyString());
  }

  @Test
  @DisplayName("Publish fails with empty core data")
  void failLdmlTransformation() throws DocumentationUnitNotExistsException {
    DocumentationUnit invalidTestDocumentUnit =
        testDocumentUnit.toBuilder().coreData(CoreData.builder().build()).build();
    UUID documentationUnitId = UUID.randomUUID();
    when(documentationUnitRepository.findByUuid(documentationUnitId))
        .thenReturn(invalidTestDocumentUnit);

    assertThatExceptionOfType(LdmlTransformationException.class)
        .isThrownBy(
            () ->
                stagingPortalPublicationService.publishDocumentationUnitWithChangelog(
                    documentationUnitId))
        .withMessageContaining("LDML validation failed.");
    verify(caseLawBucket, times(0)).save(anyString(), anyString());
  }

  @Test
  @DisplayName("Publish fails with missing judgement body")
  void failMissingJudgementBody() throws DocumentationUnitNotExistsException {
    DocumentationUnit invalidTestDocumentUnit =
        testDocumentUnit.toBuilder().longTexts(null).build();
    UUID documentationUnitId = UUID.randomUUID();
    when(documentationUnitRepository.findByUuid(documentationUnitId))
        .thenReturn(invalidTestDocumentUnit);

    assertThatExceptionOfType(LdmlTransformationException.class)
        .isThrownBy(
            () ->
                stagingPortalPublicationService.publishDocumentationUnitWithChangelog(
                    documentationUnitId))
        .withMessageContaining("Missing judgment body.");
    verify(caseLawBucket, times(0)).save(anyString(), anyString());
  }

  @Test
  @DisplayName("Should fail when changelog file cannot be created")
  void failChangelogFileFailure()
      throws DocumentationUnitNotExistsException, JsonProcessingException {
    UUID documentationUnitId = UUID.randomUUID();
    when(documentationUnitRepository.findByUuid(documentationUnitId)).thenReturn(testDocumentUnit);
    when(objectMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

    assertThatExceptionOfType(PublishException.class)
        .isThrownBy(
            () ->
                stagingPortalPublicationService.publishDocumentationUnitWithChangelog(
                    documentationUnitId))
        .withMessageContaining(
            "Could not publish documentation unit to portal, because changelog file could not be created.");
    verify(caseLawBucket, times(0)).save(anyString(), anyString());
  }

  @Test
  @DisplayName("Should fail when changelog file cannot be saved to bucket")
  void failWithBucketException() throws DocumentationUnitNotExistsException {
    UUID documentationUnitId = UUID.randomUUID();
    when(documentationUnitRepository.findByUuid(documentationUnitId)).thenReturn(testDocumentUnit);

    doThrow(BucketException.class).when(caseLawBucket).save(contains("changelogs/"), anyString());

    assertThatExceptionOfType(PublishException.class)
        .isThrownBy(
            () ->
                stagingPortalPublicationService.publishDocumentationUnitWithChangelog(
                    documentationUnitId))
        .withMessageContaining("Could not save changelog to bucket");
  }

  @Test
  @DisplayName("Should fail when ldml file cannot be saved to bucket")
  void failWithBucketException2() throws DocumentationUnitNotExistsException {
    UUID documentationUnitId = UUID.randomUUID();
    when(documentationUnitRepository.findByUuid(documentationUnitId)).thenReturn(testDocumentUnit);
    doThrow(BucketException.class).when(caseLawBucket).save(contains(".xml"), anyString());

    assertThatExceptionOfType(PublishException.class)
        .isThrownBy(
            () ->
                stagingPortalPublicationService.publishDocumentationUnitWithChangelog(
                    documentationUnitId))
        .withMessageContaining("Could not save LDML to bucket");
  }
}
