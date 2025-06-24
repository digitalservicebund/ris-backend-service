package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.doThrow;
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
import de.bund.digitalservice.ris.caselaw.adapter.exception.BucketException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.LdmlTransformationException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.PublishException;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.LongTexts;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class StagingPortalPublicationServiceTest {

  @MockitoBean private DocumentationUnitRepository documentationUnitRepository;
  @MockitoBean private PortalBucket caseLawBucket;
  @MockitoBean private XmlUtilService xmlUtilService;
  @MockitoBean private ObjectMapper objectMapper;
  @MockitoBean private PortalTransformer portalTransformer;

  private static Decision testDocumentUnit;
  private static String testDocumentNumber;
  private static CaseLawLdml testLdml;

  private StagingPortalPublicationService subject;

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

    UUID testUUID = UUID.randomUUID();
    testDocumentNumber = "TEST123456789";
    testDocumentUnit =
        Decision.builder()
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
        new StagingPortalPublicationService(
            documentationUnitRepository,
            xmlUtilService,
            caseLawBucket,
            objectMapper,
            portalTransformer);
    when(objectMapper.writeValueAsString(any())).thenReturn("");
  }

  @Test
  @DisplayName("Should publish single documentation unit successfully")
  void publishSuccessfully() throws DocumentationUnitNotExistsException {
    UUID documentationUnitId = UUID.randomUUID();
    String transformed = "<akn:akomaNtoso />";
    when(documentationUnitRepository.findByUuid(documentationUnitId)).thenReturn(testDocumentUnit);
    when(portalTransformer.transformToLdml(testDocumentUnit)).thenReturn(testLdml);
    when(xmlUtilService.ldmlToString(testLdml)).thenReturn(Optional.of(transformed));

    subject.publishDocumentationUnitWithChangelog(documentationUnitId);

    verify(caseLawBucket, times(1))
        .save(testDocumentNumber + "/" + testDocumentNumber + ".xml", transformed);
  }

  @Test
  @DisplayName("Publish fails with empty core data")
  void failLdmlTransformation() throws DocumentationUnitNotExistsException {
    UUID documentationUnitId = UUID.randomUUID();
    when(documentationUnitRepository.findByUuid(documentationUnitId)).thenReturn(testDocumentUnit);
    when(portalTransformer.transformToLdml(testDocumentUnit))
        .thenThrow(new LdmlTransformationException("LDML validation failed.", new Exception()));

    assertThatExceptionOfType(LdmlTransformationException.class)
        .isThrownBy(() -> subject.publishDocumentationUnitWithChangelog(documentationUnitId))
        .withMessageContaining("LDML validation failed.");
    verify(caseLawBucket, times(0)).save(anyString(), anyString());
  }

  @Test
  @DisplayName("Publish fails with missing judgement body")
  void failMissingJudgementBody() throws DocumentationUnitNotExistsException {
    UUID documentationUnitId = UUID.randomUUID();
    when(documentationUnitRepository.findByUuid(documentationUnitId)).thenReturn(testDocumentUnit);
    when(portalTransformer.transformToLdml(testDocumentUnit))
        .thenThrow(new LdmlTransformationException("Missing judgment body.", new Exception()));

    assertThatExceptionOfType(LdmlTransformationException.class)
        .isThrownBy(() -> subject.publishDocumentationUnitWithChangelog(documentationUnitId))
        .withMessageContaining("Missing judgment body.");
    verify(caseLawBucket, times(0)).save(anyString(), anyString());
  }

  // changelog currently disabled
  //  @Test
  //  @DisplayName("Should fail when changelog file cannot be created")
  //  void failChangelogFileFailure()
  //      throws DocumentationUnitNotExistsException, JsonProcessingException {
  //    UUID documentationUnitId = UUID.randomUUID();
  //
  // when(documentationUnitRepository.findByUuid(documentationUnitId)).thenReturn(testDocumentUnit);
  //    when(objectMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);
  //    when(portalTransformer.transformToLdml(testDocumentUnit)).thenReturn(testLdml);
  //    when(xmlUtilService.ldmlToString(testLdml)).thenReturn(Optional.of("<akn:akomaNtoso />"));
  //
  //    assertThatExceptionOfType(PublishException.class)
  //        .isThrownBy(() -> subject.publishDocumentationUnitWithChangelog(documentationUnitId))
  //        .withMessageContaining("Could not save changelog to bucket");
  //    verify(caseLawBucket).delete(testDocumentUnit.documentNumber() + ".xml");
  //  }

  // changelog currently disabled
  //  @Test
  //  @DisplayName("Should fail when changelog file cannot be saved to bucket")
  //  void failWithBucketException() throws DocumentationUnitNotExistsException {
  //    UUID documentationUnitId = UUID.randomUUID();
  //
  // when(documentationUnitRepository.findByUuid(documentationUnitId)).thenReturn(testDocumentUnit);
  //    when(portalTransformer.transformToLdml(testDocumentUnit)).thenReturn(testLdml);
  //    when(xmlUtilService.ldmlToString(testLdml)).thenReturn(Optional.of("<akn:akomaNtoso />"));
  //
  //    doThrow(BucketException.class).when(caseLawBucket).save(contains("changelogs/"),
  // anyString());
  //
  //    assertThatExceptionOfType(PublishException.class)
  //        .isThrownBy(() -> subject.publishDocumentationUnitWithChangelog(documentationUnitId))
  //        .withMessageContaining("Could not save changelog to bucket");
  //    verify(caseLawBucket).delete(testDocumentUnit.documentNumber() + ".xml");
  //  }

  @Test
  @DisplayName("Should fail when ldml file cannot be saved to bucket")
  void failWithBucketException2() throws DocumentationUnitNotExistsException {
    UUID documentationUnitId = UUID.randomUUID();
    when(documentationUnitRepository.findByUuid(documentationUnitId)).thenReturn(testDocumentUnit);
    doThrow(BucketException.class).when(caseLawBucket).save(contains(".xml"), anyString());
    when(portalTransformer.transformToLdml(testDocumentUnit)).thenReturn(testLdml);
    when(xmlUtilService.ldmlToString(testLdml)).thenReturn(Optional.of("<akn:akomaNtoso />"));

    assertThatExceptionOfType(PublishException.class)
        .isThrownBy(() -> subject.publishDocumentationUnitWithChangelog(documentationUnitId))
        .withMessageContaining("Could not save LDML to bucket");
  }
}
