package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentRepository;
import de.bund.digitalservice.ris.caselaw.adapter.exception.BucketException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.ChangelogException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.LdmlTransformationException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.PublishException;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitHistoryLogService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.FeatureToggleService;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLogEventType;
import de.bund.digitalservice.ris.caselaw.domain.LdmlTransformationResult;
import de.bund.digitalservice.ris.caselaw.domain.LongTexts;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceeding;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceedingShortTexts;
import de.bund.digitalservice.ris.caselaw.domain.PortalPublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import de.bund.digitalservice.ris.caselaw.domain.ShortTexts;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class PortalPublicationServiceTest {

  @MockitoBean private DocumentationUnitRepository documentationUnitRepository;
  @MockitoBean private AttachmentRepository attachmentRepository;
  @MockitoBean private PortalBucket caseLawBucket;
  @MockitoBean private XmlUtilService xmlUtilService;
  @MockitoBean private ObjectMapper objectMapper;
  @MockitoBean private PortalTransformer portalTransformer;
  @MockitoBean private FeatureToggleService featureToggleService;
  @MockitoBean private DocumentationUnitHistoryLogService historyLogService;

  private static Decision testDocumentUnit;
  private static String testDocumentNumber;
  private static CaseLawLdml testLdml;

  private PortalPublicationService subject;

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
            .portalPublicationStatus(PortalPublicationStatus.UNPUBLISHED)
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
        new PortalPublicationService(
            documentationUnitRepository,
            attachmentRepository,
            xmlUtilService,
            caseLawBucket,
            objectMapper,
            portalTransformer,
            featureToggleService,
            historyLogService);
    when(objectMapper.writeValueAsString(any())).thenReturn("");
    when(featureToggleService.isEnabled("neuris.portal-publication")).thenReturn(true);
    when(featureToggleService.isEnabled("neuris.regular-changelogs")).thenReturn(true);
  }

  @Nested
  class PublishDocumentationUnit {
    @Test
    void publishDocumentationUnit_withAttachments_shouldSaveToBucket()
        throws DocumentationUnitNotExistsException {
      String transformed = "ldml";
      when(documentationUnitRepository.findByDocumentNumber(testDocumentNumber))
          .thenReturn(testDocumentUnit);
      when(portalTransformer.transformToLdml(testDocumentUnit)).thenReturn(testLdml);
      when(xmlUtilService.ldmlToString(any())).thenReturn(Optional.of(transformed));
      var content = new byte[] {1};
      when(attachmentRepository.findAllByDocumentationUnitId(testDocumentUnit.uuid()))
          .thenReturn(
              List.of(
                  AttachmentDTO.builder()
                      .filename("originalentscheidung")
                      .format("docx")
                      .uploadTimestamp(Instant.now())
                      .build(),
                  AttachmentDTO.builder()
                      .filename("bild1.png")
                      .format("png")
                      .content(content)
                      .uploadTimestamp(Instant.now())
                      .build()));

      subject.publishDocumentationUnit(testDocumentNumber);

      verify(caseLawBucket).save(withPrefix(testDocumentNumber), transformed);
      verify(caseLawBucket).saveBytes(testDocumentNumber + "/bild1.png", content);
      verify(caseLawBucket, never())
          .saveBytes(eq(testDocumentNumber + "/originalenscheidung"), any(byte[].class));
    }

    @Test
    void
        publishDocumentationUnit_withDocumentNumberDoesNotExist_shouldThrowDocumentationUnitNotExistsException()
            throws DocumentationUnitNotExistsException {
      String invalidDocumentNumber = "abcd123456789";
      when(documentationUnitRepository.findByDocumentNumber(invalidDocumentNumber))
          .thenThrow(new DocumentationUnitNotExistsException(invalidDocumentNumber));

      assertThatExceptionOfType(DocumentationUnitNotExistsException.class)
          .isThrownBy(() -> subject.publishDocumentationUnit(invalidDocumentNumber))
          .withMessageContaining("Documentation unit does not exist: " + invalidDocumentNumber);
    }

    @Test
    void publishDocumentationUnit_withMissingCoreDataLdml_shouldThrowLdmlTransformationException()
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
    void publishDocumentationUnit_withStringParsingIssue_shouldThrowLdmlTransformationException()
        throws DocumentationUnitNotExistsException {
      when(documentationUnitRepository.findByDocumentNumber(testDocumentNumber))
          .thenReturn(testDocumentUnit);
      when(xmlUtilService.ldmlToString(any())).thenReturn(Optional.empty());

      assertThatExceptionOfType(LdmlTransformationException.class)
          .isThrownBy(() -> subject.publishDocumentationUnit(testDocumentNumber))
          .withMessageContaining("Could not parse transformed LDML as string.");
    }

    @Nested
    class PublishDocumentationUnitWithChangeLog {
      @Test
      void publishDocumentationUnitWithChangeLog_withFirstPublish_shouldPublishSuccessfully()
          throws DocumentationUnitNotExistsException {
        UUID documentationUnitId = UUID.randomUUID();
        String transformed = "<akn:akomaNtoso />";
        User user = mock(User.class);
        when(documentationUnitRepository.findByUuid(documentationUnitId))
            .thenReturn(testDocumentUnit);
        when(portalTransformer.transformToLdml(testDocumentUnit)).thenReturn(testLdml);
        when(xmlUtilService.ldmlToString(testLdml)).thenReturn(Optional.of(transformed));

        subject.publishDocumentationUnitWithChangelog(documentationUnitId, user);

        verify(caseLawBucket).save(withPrefix(testDocumentNumber), transformed);
        verify(historyLogService)
            .saveHistoryLog(
                testDocumentUnit.uuid(),
                user,
                HistoryLogEventType.PORTAL_PUBLICATION,
                "Dokeinheit im Portal veröffentlicht");
        verify(documentationUnitRepository)
            .updatePortalPublicationStatus(
                testDocumentUnit.uuid(), PortalPublicationStatus.PUBLISHED);
        verify(documentationUnitRepository, never())
            .savePublicationDateTime(testDocumentUnit.uuid());
      }

      @Test
      void publishDocumentationUnitWithChangelog_withPendingProceeding_shouldPublishSuccessfully()
          throws DocumentationUnitNotExistsException {
        User user = mock(User.class);
        PendingProceeding pendingProceeding =
            PendingProceeding.builder()
                .uuid(UUID.randomUUID())
                .documentNumber(testDocumentNumber)
                .build();
        when(documentationUnitRepository.findByUuid(pendingProceeding.uuid()))
            .thenReturn(pendingProceeding);
        when(portalTransformer.transformToLdml(any())).thenReturn(testLdml);
        String transformed = "<akn:akomaNtoso />";
        when(xmlUtilService.ldmlToString(testLdml)).thenReturn(Optional.of(transformed));

        subject.publishDocumentationUnitWithChangelog(pendingProceeding.uuid(), user);

        verify(caseLawBucket).save(withPrefix(testDocumentNumber), transformed);
        verify(historyLogService)
            .saveHistoryLog(
                pendingProceeding.uuid(),
                user,
                HistoryLogEventType.PORTAL_PUBLICATION,
                "Dokeinheit im Portal veröffentlicht");
        verify(documentationUnitRepository)
            .updatePortalPublicationStatus(
                pendingProceeding.uuid(), PortalPublicationStatus.PUBLISHED);
      }

      @Test
      void publishDocumentationUnitWithChangeLog_withRepublish_shouldPublishSuccessfully()
          throws DocumentationUnitNotExistsException {
        UUID documentationUnitId = UUID.randomUUID();
        String transformed = "<akn:akomaNtoso />";
        User user = mock(User.class);
        Decision docUnit =
            testDocumentUnit.toBuilder()
                .portalPublicationStatus(PortalPublicationStatus.PUBLISHED)
                .build();

        when(documentationUnitRepository.findByUuid(documentationUnitId)).thenReturn(docUnit);
        when(portalTransformer.transformToLdml(docUnit)).thenReturn(testLdml);
        when(xmlUtilService.ldmlToString(testLdml)).thenReturn(Optional.of(transformed));

        subject.publishDocumentationUnitWithChangelog(documentationUnitId, user);

        verify(caseLawBucket).save(withPrefix(testDocumentNumber), transformed);
        verify(historyLogService)
            .saveHistoryLog(
                docUnit.uuid(),
                user,
                HistoryLogEventType.PORTAL_PUBLICATION,
                "Dokeinheit im Portal veröffentlicht");
        verify(documentationUnitRepository, never())
            .updatePortalPublicationStatus(docUnit.uuid(), PortalPublicationStatus.PUBLISHED);
        verify(documentationUnitRepository).savePublicationDateTime(docUnit.uuid());
      }

      @Test
      void
          publishDocumentationUnitWithChangeLog_withPublishAfterWithdraw_shouldPublishSuccessfully()
              throws DocumentationUnitNotExistsException {
        UUID documentationUnitId = UUID.randomUUID();
        String transformed = "<akn:akomaNtoso />";
        User user = mock(User.class);
        Decision docUnit =
            testDocumentUnit.toBuilder()
                .portalPublicationStatus(PortalPublicationStatus.WITHDRAWN)
                .build();

        when(documentationUnitRepository.findByUuid(documentationUnitId)).thenReturn(docUnit);
        when(portalTransformer.transformToLdml(docUnit)).thenReturn(testLdml);
        when(xmlUtilService.ldmlToString(testLdml)).thenReturn(Optional.of(transformed));

        subject.publishDocumentationUnitWithChangelog(documentationUnitId, user);

        verify(caseLawBucket).save(withPrefix(testDocumentNumber), transformed);
        verify(historyLogService)
            .saveHistoryLog(
                docUnit.uuid(),
                user,
                HistoryLogEventType.PORTAL_PUBLICATION,
                "Dokeinheit im Portal veröffentlicht");
        verify(documentationUnitRepository)
            .updatePortalPublicationStatus(docUnit.uuid(), PortalPublicationStatus.PUBLISHED);
        verify(documentationUnitRepository, never()).savePublicationDateTime(docUnit.uuid());
      }

      @Test
      void publishDocumentationUnitWithChangeLog_withFeatureDisabled_shouldNotPublish()
          throws DocumentationUnitNotExistsException {
        UUID documentationUnitId = UUID.randomUUID();
        String transformed = "<akn:akomaNtoso />";
        User user = mock(User.class);
        when(documentationUnitRepository.findByUuid(documentationUnitId))
            .thenReturn(testDocumentUnit);
        when(portalTransformer.transformToLdml(testDocumentUnit)).thenReturn(testLdml);
        when(xmlUtilService.ldmlToString(testLdml)).thenReturn(Optional.of(transformed));
        when(featureToggleService.isEnabled("neuris.portal-publication")).thenReturn(false);

        subject.publishDocumentationUnitWithChangelog(documentationUnitId, user);

        verify(caseLawBucket, never()).save(any(), any());
      }

      @Test
      void
          publishDocumentationUnitWithChangeLog_withEmptyCoreData_shouldThrowLdmlTransformationException()
              throws DocumentationUnitNotExistsException {
        UUID documentationUnitId = UUID.randomUUID();
        User user = mock(User.class);
        when(documentationUnitRepository.findByUuid(documentationUnitId))
            .thenReturn(testDocumentUnit);
        when(portalTransformer.transformToLdml(testDocumentUnit))
            .thenThrow(new LdmlTransformationException("LDML validation failed.", new Exception()));

        assertThatExceptionOfType(LdmlTransformationException.class)
            .isThrownBy(
                () -> subject.publishDocumentationUnitWithChangelog(documentationUnitId, user))
            .withMessageContaining("LDML validation failed.");
        verify(caseLawBucket, never()).save(anyString(), anyString());
        verify(historyLogService)
            .saveHistoryLog(
                documentationUnitId,
                user,
                HistoryLogEventType.PORTAL_PUBLICATION,
                "Dokeinheit konnte nicht im Portal veröffentlicht werden");
      }

      @Test
      void
          publishDocumentationUnitWithChangeLog_withMissingJudgmentBody_shouldThrowLdmlTransformationException()
              throws DocumentationUnitNotExistsException {
        UUID documentationUnitId = UUID.randomUUID();
        User user = mock(User.class);
        when(documentationUnitRepository.findByUuid(documentationUnitId))
            .thenReturn(testDocumentUnit);
        when(portalTransformer.transformToLdml(testDocumentUnit))
            .thenThrow(new LdmlTransformationException("Missing judgment body.", new Exception()));

        assertThatExceptionOfType(LdmlTransformationException.class)
            .isThrownBy(
                () -> subject.publishDocumentationUnitWithChangelog(documentationUnitId, user))
            .withMessageContaining("Missing judgment body.");
        verify(caseLawBucket, never()).save(anyString(), anyString());
        verify(historyLogService)
            .saveHistoryLog(
                documentationUnitId,
                user,
                HistoryLogEventType.PORTAL_PUBLICATION,
                "Dokeinheit konnte nicht im Portal veröffentlicht werden");
      }

      @Test
      @DisplayName("Should fail when changelog file cannot be created")
      void
          publishDocumentationUnitWithChangeLog_withChangelogFileCreationError_shouldThrowPublishException()
              throws DocumentationUnitNotExistsException, JsonProcessingException {
        UUID documentationUnitId = UUID.randomUUID();
        User user = mock(User.class);
        when(documentationUnitRepository.findByUuid(documentationUnitId))
            .thenReturn(testDocumentUnit);
        when(portalTransformer.transformToLdml(testDocumentUnit)).thenReturn(testLdml);
        when(xmlUtilService.ldmlToString(testLdml)).thenReturn(Optional.of("<akn:akomaNtoso />"));
        when(caseLawBucket.getAllFilenamesByPath(testDocumentUnit.documentNumber() + "/"))
            .thenReturn(new ArrayList<>(), List.of(withPrefix(testDocumentNumber)));

        when(objectMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

        assertThatExceptionOfType(PublishException.class)
            .isThrownBy(
                () -> subject.publishDocumentationUnitWithChangelog(documentationUnitId, user))
            .withMessageContaining("Could not save changelog to bucket");
        verify(caseLawBucket).delete(withPrefix(testDocumentNumber));
        verify(historyLogService)
            .saveHistoryLog(
                documentationUnitId,
                user,
                HistoryLogEventType.PORTAL_PUBLICATION,
                "Dokeinheit konnte nicht im Portal veröffentlicht werden");
      }

      @Test
      void
          publishDocumentationUnitWithChangeLog_saveChangelogWithBucketException_shouldThrowPublishException()
              throws DocumentationUnitNotExistsException {
        UUID documentationUnitId = UUID.randomUUID();
        User user = mock(User.class);
        when(documentationUnitRepository.findByUuid(documentationUnitId))
            .thenReturn(testDocumentUnit);
        when(portalTransformer.transformToLdml(testDocumentUnit)).thenReturn(testLdml);
        when(xmlUtilService.ldmlToString(testLdml)).thenReturn(Optional.of("<akn:akomaNtoso />"));
        when(caseLawBucket.getAllFilenamesByPath(testDocumentNumber + "/"))
            .thenReturn(new ArrayList<>(), List.of(withPrefix(testDocumentNumber)));

        doThrow(BucketException.class)
            .when(caseLawBucket)
            .save(contains("changelogs/"), anyString());

        assertThatExceptionOfType(PublishException.class)
            .isThrownBy(
                () -> subject.publishDocumentationUnitWithChangelog(documentationUnitId, user))
            .withMessageContaining("Could not save changelog to bucket");
        verify(caseLawBucket).delete(withPrefix(testDocumentNumber));
        verify(historyLogService)
            .saveHistoryLog(
                documentationUnitId,
                user,
                HistoryLogEventType.PORTAL_PUBLICATION,
                "Dokeinheit konnte nicht im Portal veröffentlicht werden");
      }

      @Test
      void
          publishDocumentationUnitWithChangeLog_saveLdmlWithBucketException_shouldThrowPublishException()
              throws DocumentationUnitNotExistsException {
        UUID documentationUnitId = UUID.randomUUID();
        User user = mock(User.class);
        when(documentationUnitRepository.findByUuid(documentationUnitId))
            .thenReturn(testDocumentUnit);
        doThrow(BucketException.class).when(caseLawBucket).save(contains(".xml"), anyString());
        when(portalTransformer.transformToLdml(testDocumentUnit)).thenReturn(testLdml);
        when(xmlUtilService.ldmlToString(testLdml)).thenReturn(Optional.of("<akn:akomaNtoso />"));

        assertThatExceptionOfType(PublishException.class)
            .isThrownBy(
                () -> subject.publishDocumentationUnitWithChangelog(documentationUnitId, user))
            .withMessageContaining("Could not save LDML to bucket");
      }
    }

    @Nested
    class WithdrawDocumentationUnit {
      @Test
      void withdraw_shouldDeleteFromBucket() throws DocumentationUnitNotExistsException {
        when(documentationUnitRepository.findByDocumentNumber(testDocumentUnit.documentNumber()))
            .thenReturn(testDocumentUnit);
        when(caseLawBucket.getAllFilenamesByPath(testDocumentNumber + "/"))
            .thenReturn(List.of(withPrefix(testDocumentNumber)));

        subject.withdrawDocumentationUnit(testDocumentNumber);

        verify(caseLawBucket).delete(withPrefix(testDocumentNumber));
      }

      @Test
      void withdraw_withBucketException_shouldThrowPublishException() {
        when(caseLawBucket.getAllFilenamesByPath(testDocumentNumber + "/"))
            .thenReturn(List.of(withPrefix(testDocumentNumber)));
        doThrow(BucketException.class).when(caseLawBucket).delete(withPrefix(testDocumentNumber));

        assertThatExceptionOfType(PublishException.class)
            .isThrownBy(() -> subject.withdrawDocumentationUnit(testDocumentNumber))
            .withMessageContaining("Could not delete LDML from bucket.");
      }
    }

    @Nested
    class WithdrawDocumentationUnitWithChangelog {
      @Test
      void withdrawWithChangelog_shouldDeleteFromBucketAndWriteDeletionChangelog()
          throws DocumentationUnitNotExistsException, JsonProcessingException {
        Decision decision =
            Decision.builder()
                .uuid(UUID.randomUUID())
                .documentNumber(testDocumentNumber)
                .portalPublicationStatus(PortalPublicationStatus.PUBLISHED)
                .build();
        when(documentationUnitRepository.findByUuid(decision.uuid())).thenReturn(decision);
        when(caseLawBucket.getAllFilenamesByPath(testDocumentNumber + "/"))
            .thenReturn(List.of(withPrefix(testDocumentNumber)));
        User user = mock(User.class);
        var changelogContent =
            """
                {"changed":[],"deleted":[TEST123456789/TEST123456789.xml]}
                """;
        when(objectMapper.writeValueAsString(any())).thenReturn(changelogContent);

        subject.withdrawDocumentationUnitWithChangelog(decision.uuid(), user);

        verify(caseLawBucket).delete(withPrefix(testDocumentNumber));
        verify(caseLawBucket)
            .save(
                contains("changelog"),
                contains("\"deleted\":[" + withPrefix(testDocumentNumber) + "]"));
        verify(historyLogService)
            .saveHistoryLog(
                decision.uuid(),
                user,
                HistoryLogEventType.PORTAL_PUBLICATION,
                "Dokeinheit wurde aus dem Portal zurückgezogen");
        verify(documentationUnitRepository)
            .updatePortalPublicationStatus(decision.uuid(), PortalPublicationStatus.WITHDRAWN);
        verify(documentationUnitRepository, never()).savePublicationDateTime(decision.uuid());
      }

      @Test
      void withdrawWithChangelog_withBucketException_shouldThrowPublishException()
          throws DocumentationUnitNotExistsException {
        UUID uuid = UUID.randomUUID();
        Decision decision =
            Decision.builder()
                .uuid(uuid)
                .documentNumber(testDocumentNumber)
                .portalPublicationStatus(PortalPublicationStatus.PUBLISHED)
                .build();
        when(documentationUnitRepository.findByUuid(uuid)).thenReturn(decision);
        when(caseLawBucket.getAllFilenamesByPath(testDocumentNumber + "/"))
            .thenReturn(List.of(withPrefix(testDocumentNumber)));
        User user = mock(User.class);
        doThrow(BucketException.class).when(caseLawBucket).delete(withPrefix(testDocumentNumber));

        assertThatExceptionOfType(PublishException.class)
            .isThrownBy(() -> subject.withdrawDocumentationUnitWithChangelog(uuid, user))
            .withMessageContaining("Could not delete LDML from bucket.");
      }

      @Test
      void withdrawWithChangelog_withJsonProcessingException_shouldThrowChangelogException()
          throws DocumentationUnitNotExistsException, JsonProcessingException {
        UUID uuid = UUID.randomUUID();
        Decision decision =
            Decision.builder()
                .uuid(uuid)
                .documentNumber(testDocumentNumber)
                .portalPublicationStatus(PortalPublicationStatus.PUBLISHED)
                .build();
        when(documentationUnitRepository.findByUuid(uuid)).thenReturn(decision);
        when(caseLawBucket.getAllFilenamesByPath(testDocumentNumber + "/"))
            .thenReturn(List.of(withPrefix(testDocumentNumber)));
        User user = mock(User.class);
        when(objectMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

        assertThatExceptionOfType(ChangelogException.class)
            .isThrownBy(() -> subject.withdrawDocumentationUnitWithChangelog(uuid, user))
            .withMessageContaining("Could not create changelog file");
      }

      @Test
      void
          withdrawWithChangelog_withDocumentationNotExists_shouldThrowDocumentationUnitNotExistsException()
              throws DocumentationUnitNotExistsException {
        UUID uuid = UUID.randomUUID();
        when(documentationUnitRepository.findByUuid(uuid))
            .thenThrow(new DocumentationUnitNotExistsException());
        User user = mock(User.class);

        assertThatExceptionOfType(DocumentationUnitNotExistsException.class)
            .isThrownBy(() -> subject.withdrawDocumentationUnitWithChangelog(uuid, user))
            .withMessageContaining("Documentation unit does not exist");
      }
    }

    @Nested
    class UploadChangelog {
      @Test
      void uploadChangelog_shouldUpload() throws JsonProcessingException {
        var changelogContent =
            """
                {"changed":["1/1.xml"],"deleted":[]}
                """;
        when(objectMapper.writeValueAsString(any())).thenReturn(changelogContent);

        subject.uploadChangelog(List.of("123/123.xml"), List.of("456/456.xml"));

        verify(caseLawBucket).save(contains("changelogs/"), eq(changelogContent));
      }

      @Test
      void uploadChangelog_withDisabledFeatureFlag_shouldDoNothing() {
        when(featureToggleService.isEnabled("neuris.regular-changelogs")).thenReturn(false);

        subject.uploadChangelog(List.of(), List.of());

        verify(caseLawBucket, never()).save(contains("changelogs/"), anyString());
      }
    }

    @Nested
    class UploadDeletionChangelog {
      @Test
      void uploadDeletionChangelog_shouldUpload() throws JsonProcessingException {
        var changelogContent =
            """
                {"deleted":[123/123.xml]}
                """;
        when(objectMapper.writeValueAsString(any())).thenReturn(changelogContent);

        subject.uploadDeletionChangelog(List.of("123/123.xml"));

        verify(caseLawBucket).save(contains("changelogs/"), eq(changelogContent));
      }
    }

    @Nested
    class UploadFullReindexChangelog {
      @Test
      void uploadFullReindexChangelog_withRegularChangelogsEnabled_shouldNotUpload() {
        subject.uploadFullReindexChangelog();

        verify(caseLawBucket, never()).save(contains("changelogs/"), anyString());
      }

      @Test
      void uploadFullReindexChangelog_withRegularChangelogsDisabled_shouldUpload()
          throws JsonProcessingException {
        var changelogContent =
            """
                {"changeAll":true}
                """;
        when(objectMapper.writeValueAsString(any())).thenReturn(changelogContent);
        when(featureToggleService.isEnabled("neuris.regular-changelogs")).thenReturn(false);

        subject.uploadFullReindexChangelog();

        verify(caseLawBucket).save(contains("changelogs/"), eq(changelogContent));
      }
    }

    @Nested
    class CreateLdmlPreview {
      @Test
      void createLdmlPreview_withValidDecision_shouldThrowDocumentationUnitNotExistsException()
          throws DocumentationUnitNotExistsException {
        // Arrange
        when(documentationUnitRepository.findByUuid(testDocumentUnit.uuid()))
            .thenThrow(DocumentationUnitNotExistsException.class);
        // Act + Assert
        assertThatThrownBy(() -> subject.createLdmlPreview(testDocumentUnit.uuid()))
            .isInstanceOf(DocumentationUnitNotExistsException.class);
      }

      @Test
      void createLdmlPreview_withValidDecision_shouldReturnLdml()
          throws DocumentationUnitNotExistsException {
        // Arrange
        String transformed = "ldml";
        when(documentationUnitRepository.findByUuid(testDocumentUnit.uuid()))
            .thenReturn(testDocumentUnit);
        when(portalTransformer.transformToLdml(testDocumentUnit)).thenReturn(testLdml);
        when(xmlUtilService.ldmlToString(any())).thenReturn(Optional.of(transformed));
        // Act
        LdmlTransformationResult result = subject.createLdmlPreview(testDocumentUnit.uuid());
        // Assert
        assertThat(result)
            .isEqualTo(LdmlTransformationResult.builder().ldml(transformed).success(true).build());
      }

      @Test
      void createLdmlPreview_withInvalidDecision_shouldThrowLdmlTransformationException()
          throws DocumentationUnitNotExistsException {
        // Arrange
        var uuid = testDocumentUnit.uuid();
        when(documentationUnitRepository.findByUuid(uuid)).thenReturn(testDocumentUnit);
        when(portalTransformer.transformToLdml(testDocumentUnit)).thenReturn(testLdml);
        when(xmlUtilService.ldmlToString(any())).thenReturn(Optional.empty());
        // Act + Assert
        assertThatThrownBy(() -> subject.createLdmlPreview(uuid))
            .isInstanceOf(LdmlTransformationException.class)
            .hasMessage("Could not parse transformed LDML as string.");
      }

      @Test
      void createLdmlPreview_withPendingProceeding_shouldReturnLdml()
          throws DocumentationUnitNotExistsException {
        // Arrange
        String transformed = "ldml";
        var uuid = UUID.randomUUID();
        var testPendingProceeding =
            PendingProceeding.builder()
                .uuid(uuid)
                .coreData(
                    CoreData.builder()
                        .ecli("testecli")
                        .court(
                            Court.builder()
                                .type("testCourtType")
                                .location("testCourtLocation")
                                .build())
                        .documentType(
                            DocumentType.builder().label("testDocumentTypeAbbreviation").build())
                        .legalEffect("ja")
                        .fileNumbers(List.of("testFileNumber"))
                        .decisionDate(LocalDate.of(2020, 1, 1))
                        .build())
                .documentNumber(testDocumentNumber)
                .shortTexts(
                    PendingProceedingShortTexts.builder()
                        .admissionOfAppeal("AdmissionOfAppeal")
                        .build())
                .build();
        when(documentationUnitRepository.findByUuid(uuid)).thenReturn(testPendingProceeding);
        when(portalTransformer.transformToLdml(testPendingProceeding)).thenReturn(testLdml);
        when(xmlUtilService.ldmlToString(any())).thenReturn(Optional.of(transformed));
        // Act
        LdmlTransformationResult result = subject.createLdmlPreview(testDocumentUnit.uuid());
        // Assert
        assertThat(result)
            .isEqualTo(LdmlTransformationResult.builder().ldml(transformed).success(true).build());
      }
    }
  }

  private String withPrefix(String documentNumber) {
    return documentNumber + "/" + documentNumber + ".xml";
  }
}
