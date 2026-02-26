package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.FrbrElement;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.FrbrThis;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Identification;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Judgment;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.Meta;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentInlineDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AttachmentRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseAttachmentInlineRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalEffectDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PendingProceedingDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PreviousDecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PublishedDocumentationSnapshotEntity;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PublishedDocumentationSnapshotRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.RelatedPendingProceedingDTO;
import de.bund.digitalservice.ris.caselaw.adapter.exception.BucketException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.ChangelogException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.LdmlTransformationException;
import de.bund.digitalservice.ris.caselaw.adapter.exception.PublishException;
import de.bund.digitalservice.ris.caselaw.adapter.publication.CaselawCitationPublishService;
import de.bund.digitalservice.ris.caselaw.adapter.publication.CaselawCitationSyncService;
import de.bund.digitalservice.ris.caselaw.adapter.publication.ManualPortalPublicationResult.RelatedPendingProceedingPublicationResult;
import de.bund.digitalservice.ris.caselaw.adapter.publication.PortalBucket;
import de.bund.digitalservice.ris.caselaw.adapter.publication.PortalPublicationService;
import de.bund.digitalservice.ris.caselaw.adapter.publication.uli.UliCitationPublishService;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DecisionTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml.PortalTransformer;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitHistoryLogService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.FeatureToggleService;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLogEventType;
import de.bund.digitalservice.ris.caselaw.domain.LdmlTransformationResult;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceeding;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceedingShortTexts;
import de.bund.digitalservice.ris.caselaw.domain.PortalPublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.RelatedPendingProceeding;
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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.data.mapping.MappingException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(SpringExtension.class)
class PortalPublicationServiceTest {

  @MockitoBean private DocumentationUnitRepository documentationUnitRepository;
  @MockitoBean private DatabaseDocumentationUnitRepository databaseDocumentationUnitRepository;
  @MockitoBean private AttachmentRepository attachmentRepository;
  @MockitoBean private DatabaseAttachmentInlineRepository attachmentInlineRepository;
  @MockitoBean private PortalBucket caseLawBucket;
  @MockitoBean private XmlUtilService xmlUtilService;
  @MockitoBean private ObjectMapper objectMapper;
  @MockitoBean private PortalTransformer portalTransformer;
  @MockitoBean private FeatureToggleService featureToggleService;
  @MockitoBean private DocumentationUnitHistoryLogService historyLogService;
  @MockitoBean private PublishedDocumentationSnapshotRepository snapshotRepository;
  @MockitoBean private CaselawCitationPublishService caselawCitationPublishService;
  @MockitoBean private CaselawCitationSyncService caselawCitationSyncService;
  @MockitoBean private UliCitationPublishService uliCitationPublishService;

  private ArgumentCaptor<PublishedDocumentationSnapshotEntity> snapshotCaptor =
      ArgumentCaptor.forClass(PublishedDocumentationSnapshotEntity.class);

  private static Decision testDocumentUnit;
  private static DecisionDTO testDocumentUnitDTO;
  private static String testDocumentNumber;
  private static CaseLawLdml testLdml;

  private PortalPublicationService subject;

  private static PendingProceeding relatedPendingProceeding =
      PendingProceeding.builder()
          .uuid(UUID.randomUUID())
          .documentNumber("Test document number 3")
          .portalPublicationStatus(PortalPublicationStatus.PUBLISHED)
          .coreData(
              CoreData.builder()
                  .fileNumbers(List.of("FN-1"))
                  .decisionDate(LocalDate.of(2024, 1, 1))
                  .court(Court.builder().type("Court").build())
                  .build())
          .shortTexts(PendingProceedingShortTexts.builder().legalIssue("Issue").build())
          .build();
  private static PendingProceeding relatedPendingProceedingWithResolutionNote =
      PendingProceeding.builder()
          .uuid(UUID.randomUUID())
          .documentNumber("Test document number 4")
          .portalPublicationStatus(PortalPublicationStatus.PUBLISHED)
          .coreData(
              CoreData.builder()
                  .fileNumbers(List.of("FN-2"))
                  .decisionDate(LocalDate.of(2024, 2, 2))
                  .court(Court.builder().type("Court").build())
                  .build())
          .shortTexts(
              PendingProceedingShortTexts.builder()
                  .legalIssue("Issue")
                  .resolutionNote("Resolution note")
                  .build())
          .build();
  private static PendingProceeding resolvedRelatedPendingProceeding =
      PendingProceeding.builder()
          .uuid(UUID.randomUUID())
          .documentNumber("Test document number 5")
          .portalPublicationStatus(PortalPublicationStatus.PUBLISHED)
          .coreData(
              CoreData.builder()
                  .isResolved(true)
                  .fileNumbers(List.of("FN-3"))
                  .decisionDate(LocalDate.of(2023, 3, 3))
                  .court(Court.builder().type("Court").build())
                  .build())
          .shortTexts(PendingProceedingShortTexts.builder().legalIssue("Issue").build())
          .build();

  @BeforeAll
  static void setUpBeforeClass() {
    PreviousDecisionDTO related1Dto =
        PreviousDecisionDTO.builder()
            .date(LocalDate.of(2020, 1, 1))
            .court(CourtDTO.builder().type("Test court type").build())
            .documentType(DocumentTypeDTO.builder().label("Test decision type").build())
            .fileNumber("Test file number")
            .documentNumber("Test document number 1")
            .build();
    PreviousDecisionDTO related2Dto =
        related1Dto.toBuilder().documentNumber("Test document number 2").build();

    UUID testUUID = UUID.randomUUID();
    testDocumentNumber = "TEST123456789";
    testDocumentUnitDTO =
        DecisionDTO.builder()
            .id(testUUID)
            .ecli("testecli")
            .court(CourtDTO.builder().type("testCourtType").location("testCourtLocation").build())
            .documentType(DocumentTypeDTO.builder().label("testDocumentTypeAbbreviation").build())
            .legalEffect(LegalEffectDTO.JA)
            .fileNumbers(List.of(FileNumberDTO.builder().value("testFileNumber").build()))
            .date(LocalDate.of(2020, 1, 1))
            .documentNumber(testDocumentNumber)
            .portalPublicationStatus(PortalPublicationStatus.UNPUBLISHED)
            .caseFacts("<p>Example content 1</p>")
            .previousDecisions(List.of(related1Dto, related2Dto))
            .relatedPendingProceedings(
                List.of(
                    RelatedPendingProceedingDTO.builder()
                        .documentNumber(relatedPendingProceeding.documentNumber())
                        .build(),
                    RelatedPendingProceedingDTO.builder()
                        .documentNumber(relatedPendingProceedingWithResolutionNote.documentNumber())
                        .build(),
                    RelatedPendingProceedingDTO.builder()
                        .documentNumber(resolvedRelatedPendingProceeding.documentNumber())
                        .build()))
            .build();
    testDocumentUnit = DecisionTransformer.transformToDomain(testDocumentUnitDTO, null);

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
  void mockReset() {
    subject =
        new PortalPublicationService(
            documentationUnitRepository,
            databaseDocumentationUnitRepository,
            xmlUtilService,
            caseLawBucket,
            objectMapper,
            portalTransformer,
            featureToggleService,
            historyLogService,
            attachmentInlineRepository,
            snapshotRepository,
            caselawCitationSyncService,
            caselawCitationPublishService,
            uliCitationPublishService);
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
      when(documentationUnitRepository.loadDocumentationUnitDTO(testDocumentNumber))
          .thenReturn(testDocumentUnitDTO);
      when(portalTransformer.transformToLdml(any())).thenReturn(testLdml);
      when(xmlUtilService.ldmlToString(any())).thenReturn(Optional.of(transformed));
      var content = new byte[] {1};
      when(attachmentInlineRepository.findAllByDocumentationUnitId(testDocumentUnit.uuid()))
          .thenReturn(
              List.of(
                  AttachmentInlineDTO.builder()
                      .filename("bild1.png")
                      .format("png")
                      .content(content)
                      .uploadTimestamp(Instant.now())
                      .build()));
      when(attachmentRepository.findAllByDocumentationUnitId(testDocumentUnit.uuid()))
          .thenReturn(
              List.of(
                  AttachmentDTO.builder()
                      .filename("originalentscheidung")
                      .format("docx")
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
      when(documentationUnitRepository.loadDocumentationUnitDTO(invalidDocumentNumber))
          .thenThrow(new DocumentationUnitNotExistsException(invalidDocumentNumber));

      assertThatExceptionOfType(DocumentationUnitNotExistsException.class)
          .isThrownBy(() -> subject.publishDocumentationUnit(invalidDocumentNumber))
          .withMessageContaining("Documentation unit does not exist: " + invalidDocumentNumber);
    }

    @Test
    void publishDocumentationUnit_withMissingCoreDataLdml_shouldThrowLdmlTransformationException()
        throws DocumentationUnitNotExistsException {
      when(documentationUnitRepository.loadDocumentationUnitDTO(testDocumentNumber))
          .thenReturn(testDocumentUnitDTO);
      when(portalTransformer.transformToLdml(any()))
          .thenThrow(new LdmlTransformationException("LDML validation failed.", new Exception()));

      assertThatExceptionOfType(LdmlTransformationException.class)
          .isThrownBy(() -> subject.publishDocumentationUnit(testDocumentNumber))
          .withMessageContaining("LDML validation failed.");
    }

    @Test
    void publishDocumentationUnit_withStringParsingIssue_shouldThrowLdmlTransformationException()
        throws DocumentationUnitNotExistsException {
      when(documentationUnitRepository.loadDocumentationUnitDTO(testDocumentNumber))
          .thenReturn(testDocumentUnitDTO);
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
        when(documentationUnitRepository.loadDocumentationUnitDTO(documentationUnitId))
            .thenReturn(testDocumentUnitDTO);
        when(portalTransformer.transformToLdml(any())).thenReturn(testLdml);
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
        verify(documentationUnitRepository).savePublicationDateTime(testDocumentUnit.uuid());
      }

      @Test
      void publishDocumentationUnitWithChangelog_withPendingProceeding_shouldPublishSuccessfully()
          throws DocumentationUnitNotExistsException {
        User user = mock(User.class);
        PendingProceedingDTO pendingProceeding =
            PendingProceedingDTO.builder()
                .id(UUID.randomUUID())
                .documentNumber(testDocumentNumber)
                .build();
        when(documentationUnitRepository.loadDocumentationUnitDTO(pendingProceeding.getId()))
            .thenReturn(pendingProceeding);
        when(portalTransformer.transformToLdml(any())).thenReturn(testLdml);
        String transformed = "<akn:akomaNtoso />";
        when(xmlUtilService.ldmlToString(testLdml)).thenReturn(Optional.of(transformed));

        subject.publishDocumentationUnitWithChangelog(pendingProceeding.getId(), user);

        verify(caseLawBucket).save(withPrefix(testDocumentNumber), transformed);
        verify(historyLogService)
            .saveHistoryLog(
                pendingProceeding.getId(),
                user,
                HistoryLogEventType.PORTAL_PUBLICATION,
                "Dokeinheit im Portal veröffentlicht");
        verify(documentationUnitRepository)
            .updatePortalPublicationStatus(
                pendingProceeding.getId(), PortalPublicationStatus.PUBLISHED);
      }

      @Test
      void publishDocumentationUnitWithChangeLog_withRepublish_shouldPublishSuccessfully()
          throws DocumentationUnitNotExistsException {
        UUID documentationUnitId = UUID.randomUUID();
        String transformed = "<akn:akomaNtoso />";
        User user = mock(User.class);
        DecisionDTO docUnit =
            testDocumentUnitDTO.toBuilder()
                .portalPublicationStatus(PortalPublicationStatus.PUBLISHED)
                .build();

        when(documentationUnitRepository.loadDocumentationUnitDTO(documentationUnitId))
            .thenReturn(docUnit);
        when(portalTransformer.transformToLdml(any())).thenReturn(testLdml);
        when(xmlUtilService.ldmlToString(testLdml)).thenReturn(Optional.of(transformed));

        subject.publishDocumentationUnitWithChangelog(documentationUnitId, user);

        verify(caseLawBucket).save(withPrefix(testDocumentNumber), transformed);
        verify(historyLogService)
            .saveHistoryLog(
                docUnit.getId(),
                user,
                HistoryLogEventType.PORTAL_PUBLICATION,
                "Dokeinheit im Portal veröffentlicht");
        verify(documentationUnitRepository, never())
            .updatePortalPublicationStatus(docUnit.getId(), PortalPublicationStatus.PUBLISHED);
        verify(documentationUnitRepository).savePublicationDateTime(docUnit.getId());
      }

      @Test
      void
          publishDocumentationUnitWithChangeLog_withPublishAfterWithdraw_shouldPublishSuccessfully()
              throws DocumentationUnitNotExistsException {
        UUID documentationUnitId = UUID.randomUUID();
        String transformed = "<akn:akomaNtoso />";
        User user = mock(User.class);
        DecisionDTO docUnit =
            testDocumentUnitDTO.toBuilder()
                .portalPublicationStatus(PortalPublicationStatus.WITHDRAWN)
                .build();

        when(documentationUnitRepository.loadDocumentationUnitDTO(documentationUnitId))
            .thenReturn(docUnit);
        when(portalTransformer.transformToLdml(any())).thenReturn(testLdml);
        when(xmlUtilService.ldmlToString(testLdml)).thenReturn(Optional.of(transformed));

        subject.publishDocumentationUnitWithChangelog(documentationUnitId, user);

        verify(caseLawBucket).save(withPrefix(testDocumentNumber), transformed);
        verify(historyLogService)
            .saveHistoryLog(
                docUnit.getId(),
                user,
                HistoryLogEventType.PORTAL_PUBLICATION,
                "Dokeinheit im Portal veröffentlicht");
        verify(documentationUnitRepository)
            .updatePortalPublicationStatus(docUnit.getId(), PortalPublicationStatus.PUBLISHED);
        verify(documentationUnitRepository).savePublicationDateTime(docUnit.getId());
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
        when(documentationUnitRepository.loadDocumentationUnitDTO(documentationUnitId))
            .thenReturn(testDocumentUnitDTO);
        when(portalTransformer.transformToLdml(any()))
            .thenThrow(new LdmlTransformationException("LDML validation failed.", new Exception()));

        assertThatExceptionOfType(LdmlTransformationException.class)
            .isThrownBy(
                () -> subject.publishDocumentationUnitWithChangelog(documentationUnitId, user))
            .withMessageContaining("LDML validation failed.");
        verify(caseLawBucket, never()).save(anyString(), anyString());
      }

      @Test
      void
          publishDocumentationUnitWithChangeLog_withMissingJudgmentBody_shouldThrowLdmlTransformationException()
              throws DocumentationUnitNotExistsException {
        UUID documentationUnitId = UUID.randomUUID();
        User user = mock(User.class);
        when(documentationUnitRepository.loadDocumentationUnitDTO(documentationUnitId))
            .thenReturn(testDocumentUnitDTO);
        when(portalTransformer.transformToLdml(any()))
            .thenThrow(new LdmlTransformationException("Missing judgment body.", new Exception()));

        assertThatExceptionOfType(LdmlTransformationException.class)
            .isThrownBy(
                () -> subject.publishDocumentationUnitWithChangelog(documentationUnitId, user))
            .withMessageContaining("Missing judgment body.");
        verify(caseLawBucket, never()).save(anyString(), anyString());
      }

      @Test
      @DisplayName("Should fail when changelog file cannot be created")
      void
          publishDocumentationUnitWithChangeLog_withChangelogFileCreationError_shouldThrowPublishException()
              throws DocumentationUnitNotExistsException, JacksonException {
        UUID documentationUnitId = UUID.randomUUID();
        User user = mock(User.class);
        when(documentationUnitRepository.loadDocumentationUnitDTO(documentationUnitId))
            .thenReturn(testDocumentUnitDTO);
        when(portalTransformer.transformToLdml(any())).thenReturn(testLdml);
        when(xmlUtilService.ldmlToString(testLdml)).thenReturn(Optional.of("<akn:akomaNtoso />"));
        when(caseLawBucket.getAllFilenamesByPath(testDocumentUnit.documentNumber() + "/"))
            .thenReturn(new ArrayList<>(), List.of(withPrefix(testDocumentNumber)));

        when(objectMapper.writeValueAsString(any())).thenThrow(JacksonException.class);

        assertThatExceptionOfType(PublishException.class)
            .isThrownBy(
                () -> subject.publishDocumentationUnitWithChangelog(documentationUnitId, user))
            .withMessageContaining("Could not save changelog to bucket");
        verify(caseLawBucket).delete(withPrefix(testDocumentNumber));
      }

      @Test
      void
          publishDocumentationUnitWithChangeLog_saveChangelogWithBucketException_shouldThrowPublishException()
              throws DocumentationUnitNotExistsException {
        UUID documentationUnitId = UUID.randomUUID();
        User user = mock(User.class);
        when(documentationUnitRepository.loadDocumentationUnitDTO(documentationUnitId))
            .thenReturn(testDocumentUnitDTO);
        when(portalTransformer.transformToLdml(any())).thenReturn(testLdml);
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
      }

      @Test
      void
          publishDocumentationUnitWithChangeLog_saveLdmlWithBucketException_shouldThrowPublishException()
              throws DocumentationUnitNotExistsException {
        UUID documentationUnitId = UUID.randomUUID();
        User user = mock(User.class);
        when(documentationUnitRepository.loadDocumentationUnitDTO(documentationUnitId))
            .thenReturn(testDocumentUnitDTO);
        doThrow(BucketException.class).when(caseLawBucket).save(contains(".xml"), anyString());
        when(portalTransformer.transformToLdml(any())).thenReturn(testLdml);
        when(xmlUtilService.ldmlToString(testLdml)).thenReturn(Optional.of("<akn:akomaNtoso />"));

        assertThatExceptionOfType(PublishException.class)
            .isThrownBy(
                () -> subject.publishDocumentationUnitWithChangelog(documentationUnitId, user))
            .withMessageContaining("Could not save LDML to bucket");
      }

      @Nested
      @Disabled(
          "This is no longer important for the portal focus, so i don't want to fix these tests (there are still integration tests checking them)")
      class RelatedPendingProceedingsResolution {

        @Test
        void
            publishDocumentationUnitWithChangeLog_withRelatedPendingProceedings_shouldResolveUnresolvedPendingProceedings()
                throws DocumentationUnitNotExistsException {
          UUID documentationUnitId = UUID.randomUUID();
          User user = mock(User.class);
          when(documentationUnitRepository.findByUuid(documentationUnitId))
              .thenReturn(testDocumentUnit);
          when(portalTransformer.transformToLdml(any())).thenReturn(testLdml);
          when(xmlUtilService.ldmlToString(testLdml)).thenReturn(Optional.of("<akn:akomaNtoso />"));
          when(documentationUnitRepository.findByDocumentNumber(
                  relatedPendingProceeding.documentNumber()))
              .thenReturn(relatedPendingProceeding);
          when(documentationUnitRepository.findByDocumentNumber(
                  relatedPendingProceedingWithResolutionNote.documentNumber()))
              .thenReturn(relatedPendingProceedingWithResolutionNote);
          when(documentationUnitRepository.findByDocumentNumber(
                  resolvedRelatedPendingProceeding.documentNumber()))
              .thenReturn(resolvedRelatedPendingProceeding);
          when(documentationUnitRepository.findByUuid(relatedPendingProceeding.uuid()))
              .thenReturn(relatedPendingProceeding);
          when(documentationUnitRepository.findByUuid(
                  relatedPendingProceedingWithResolutionNote.uuid()))
              .thenReturn(relatedPendingProceedingWithResolutionNote);

          var result = subject.publishDocumentationUnitWithChangelog(documentationUnitId, user);

          assertThat(result.relatedPendingProceedingsPublicationResult())
              .isEqualTo(RelatedPendingProceedingPublicationResult.SUCCESS);

          // unresolved without existing note: should be resolved and note auto-filled
          verify(documentationUnitRepository, times(1))
              .save(
                  argThat(
                      documentationUnit ->
                          documentationUnit
                                  .documentNumber()
                                  .equals(relatedPendingProceeding.documentNumber())
                              && documentationUnit.coreData().isResolved()
                              && ((PendingProceeding) documentationUnit)
                                  .shortTexts()
                                  .resolutionNote()
                                  .equals("Erledigt durch TEST123456789")),
                  any());

          // unresolved with existing note: should be resolved and keep existing note
          verify(documentationUnitRepository, times(1))
              .save(
                  argThat(
                      documentationUnit ->
                          documentationUnit
                                  .documentNumber()
                                  .equals(
                                      relatedPendingProceedingWithResolutionNote.documentNumber())
                              && documentationUnit.coreData().isResolved()
                              && ((PendingProceeding) documentationUnit)
                                  .shortTexts()
                                  .resolutionNote()
                                  .equals("Resolution note")),
                  any());
          verify(documentationUnitRepository, times(0))
              .save(
                  argThat(
                      documentationUnit ->
                          documentationUnit
                              .documentNumber()
                              .equals(resolvedRelatedPendingProceeding.documentNumber())),
                  any());
        }

        @Test
        void
            publishDocumentationUnitWithChangeLog_withNoRelatedPendingProceedings_shouldReturnNoAction()
                throws DocumentationUnitNotExistsException {
          UUID documentationUnitId = UUID.randomUUID();
          User user = mock(User.class);
          Decision decisionWithNoLinks =
              testDocumentUnit.toBuilder().contentRelatedIndexing(null).build();
          when(documentationUnitRepository.findByUuid(documentationUnitId))
              .thenReturn(decisionWithNoLinks);
          when(portalTransformer.transformToLdml(any())).thenReturn(testLdml);
          when(xmlUtilService.ldmlToString(testLdml)).thenReturn(Optional.of("<akn/>"));

          var result = subject.publishDocumentationUnitWithChangelog(documentationUnitId, user);

          assertThat(result.relatedPendingProceedingsPublicationResult())
              .isEqualTo(RelatedPendingProceedingPublicationResult.NO_ACTION);
          verify(documentationUnitRepository, never()).save(any(), any());
        }

        @Test
        void
            publishDocumentationUnitWithChangeLog_withUnpublishedRelatedPendingProceeding_shouldReturnError()
                throws DocumentationUnitNotExistsException {
          UUID documentationUnitId = UUID.randomUUID();
          User user = mock(User.class);
          PendingProceeding unpublished =
              relatedPendingProceeding.toBuilder()
                  .documentNumber("PP-UNPUBLISHED")
                  .portalPublicationStatus(PortalPublicationStatus.UNPUBLISHED)
                  .build();
          Decision decision =
              testDocumentUnit.toBuilder()
                  .contentRelatedIndexing(
                      ContentRelatedIndexing.builder()
                          .relatedPendingProceedings(
                              List.of(
                                  RelatedPendingProceeding.builder()
                                      .documentNumber(unpublished.documentNumber())
                                      .build()))
                          .build())
                  .build();
          when(documentationUnitRepository.findByUuid(documentationUnitId)).thenReturn(decision);
          when(documentationUnitRepository.findByDocumentNumber(unpublished.documentNumber()))
              .thenReturn(unpublished);
          when(portalTransformer.transformToLdml(any())).thenReturn(testLdml);
          when(xmlUtilService.ldmlToString(testLdml)).thenReturn(Optional.of("<akn/>"));

          var result = subject.publishDocumentationUnitWithChangelog(documentationUnitId, user);

          assertThat(result.relatedPendingProceedingsPublicationResult())
              .isEqualTo(RelatedPendingProceedingPublicationResult.ERROR);
          verify(documentationUnitRepository, never()).save(eq(unpublished), any());
        }

        @Test
        void
            publishDocumentationUnitWithChangeLog_withResolvedPendingProceeding_shouldReturnNoAction()
                throws DocumentationUnitNotExistsException {
          UUID documentationUnitId = UUID.randomUUID();
          User user = mock(User.class);
          Decision decision =
              testDocumentUnit.toBuilder()
                  .contentRelatedIndexing(
                      ContentRelatedIndexing.builder()
                          .relatedPendingProceedings(
                              List.of(
                                  RelatedPendingProceeding.builder()
                                      .documentNumber(
                                          resolvedRelatedPendingProceeding.documentNumber())
                                      .build()))
                          .build())
                  .build();
          when(documentationUnitRepository.findByUuid(documentationUnitId)).thenReturn(decision);
          when(documentationUnitRepository.findByDocumentNumber(
                  resolvedRelatedPendingProceeding.documentNumber()))
              .thenReturn(resolvedRelatedPendingProceeding);
          when(portalTransformer.transformToLdml(any())).thenReturn(testLdml);
          when(xmlUtilService.ldmlToString(testLdml)).thenReturn(Optional.of("<akn/>"));

          var result = subject.publishDocumentationUnitWithChangelog(documentationUnitId, user);

          assertThat(result.relatedPendingProceedingsPublicationResult())
              .isEqualTo(RelatedPendingProceedingPublicationResult.NO_ACTION);
          verify(documentationUnitRepository, never()).save(any(), any());
        }

        @Test
        void publishDocumentationUnitWithChangeLog_withMissingRequiredAttributes_shouldReturnError()
            throws DocumentationUnitNotExistsException {
          UUID documentationUnitId = UUID.randomUUID();
          User user = mock(User.class);
          PendingProceeding missingRequired =
              relatedPendingProceeding.toBuilder()
                  .documentNumber("PP-MISSING")
                  .shortTexts(PendingProceedingShortTexts.builder().build()) // missing legalIssue
                  .build();
          Decision decision =
              testDocumentUnit.toBuilder()
                  .contentRelatedIndexing(
                      ContentRelatedIndexing.builder()
                          .relatedPendingProceedings(
                              List.of(
                                  RelatedPendingProceeding.builder()
                                      .documentNumber(missingRequired.documentNumber())
                                      .build()))
                          .build())
                  .build();
          when(documentationUnitRepository.findByUuid(documentationUnitId)).thenReturn(decision);
          when(documentationUnitRepository.findByDocumentNumber(missingRequired.documentNumber()))
              .thenReturn(missingRequired);
          when(portalTransformer.transformToLdml(any())).thenReturn(testLdml);
          when(xmlUtilService.ldmlToString(testLdml)).thenReturn(Optional.of("<akn/>"));

          var result = subject.publishDocumentationUnitWithChangelog(documentationUnitId, user);

          assertThat(result.relatedPendingProceedingsPublicationResult())
              .isEqualTo(RelatedPendingProceedingPublicationResult.ERROR);
          verify(documentationUnitRepository, never()).save(eq(missingRequired), any());
        }

        @Test
        void
            publishDocumentationUnitWithChangeLog_whenPublishingRelatedPendingProceedingFails_shouldReturnError()
                throws DocumentationUnitNotExistsException {
          UUID documentationUnitId = UUID.randomUUID();
          User user = mock(User.class);
          PendingProceeding toPublish =
              relatedPendingProceeding.toBuilder().documentNumber("PP-PUBLISH-FAIL").build();
          Decision decision =
              testDocumentUnit.toBuilder()
                  .contentRelatedIndexing(
                      ContentRelatedIndexing.builder()
                          .relatedPendingProceedings(
                              List.of(
                                  RelatedPendingProceeding.builder()
                                      .documentNumber(toPublish.documentNumber())
                                      .build()))
                          .build())
                  .build();
          when(documentationUnitRepository.findByUuid(documentationUnitId)).thenReturn(decision);
          when(documentationUnitRepository.findByDocumentNumber(toPublish.documentNumber()))
              .thenReturn(toPublish);
          when(documentationUnitRepository.findByUuid(toPublish.uuid())).thenReturn(toPublish);
          // First publish (decision) succeeds, second publish (linked pending proceeding) fails
          when(portalTransformer.transformToLdml(any()))
              .thenReturn(testLdml)
              .thenThrow(
                  new LdmlTransformationException("LDML validation failed.", new Exception()));
          when(xmlUtilService.ldmlToString(testLdml)).thenReturn(Optional.of("<akn/>"));

          var result = subject.publishDocumentationUnitWithChangelog(documentationUnitId, user);

          assertThat(result.relatedPendingProceedingsPublicationResult())
              .isEqualTo(RelatedPendingProceedingPublicationResult.ERROR);
        }

        @Test
        void publishDocumentationUnitWithChangeLog_whenSavingUpdatedLinkedFails_shouldReturnError()
            throws DocumentationUnitNotExistsException {
          UUID documentationUnitId = UUID.randomUUID();
          User user = mock(User.class);
          PendingProceeding toSave =
              relatedPendingProceeding.toBuilder().documentNumber("PP-SAVE-FAIL").build();
          Decision decision =
              testDocumentUnit.toBuilder()
                  .contentRelatedIndexing(
                      ContentRelatedIndexing.builder()
                          .relatedPendingProceedings(
                              List.of(
                                  RelatedPendingProceeding.builder()
                                      .documentNumber(toSave.documentNumber())
                                      .build()))
                          .build())
                  .build();
          when(documentationUnitRepository.findByUuid(documentationUnitId)).thenReturn(decision);
          when(documentationUnitRepository.findByDocumentNumber(toSave.documentNumber()))
              .thenReturn(toSave);
          when(portalTransformer.transformToLdml(any())).thenReturn(testLdml);
          when(xmlUtilService.ldmlToString(testLdml)).thenReturn(Optional.of("<akn/>"));
          doThrow(new MappingException("fail"))
              .when(documentationUnitRepository)
              .save(any(), any());

          var result = subject.publishDocumentationUnitWithChangelog(documentationUnitId, user);

          assertThat(result.relatedPendingProceedingsPublicationResult())
              .isEqualTo(RelatedPendingProceedingPublicationResult.ERROR);
        }
      }
    }

    @Nested
    class WithdrawDocumentationUnit {
      @Test
      void withdraw_shouldDeleteFromBucket() throws DocumentationUnitNotExistsException {
        when(databaseDocumentationUnitRepository.findByDocumentNumber(
                testDocumentUnitDTO.getDocumentNumber()))
            .thenReturn(Optional.of(testDocumentUnitDTO));
        when(caseLawBucket.getAllFilenamesByPath(testDocumentNumber + "/"))
            .thenReturn(List.of(withPrefix(testDocumentNumber)));

        subject.withdrawDocumentationUnit(testDocumentNumber);

        verify(caseLawBucket).delete(withPrefix(testDocumentNumber));
        verify(documentationUnitRepository)
            .updatePortalPublicationStatus(
                testDocumentUnitDTO.getId(), PortalPublicationStatus.WITHDRAWN);
        verify(documentationUnitRepository, never())
            .savePublicationDateTime(testDocumentUnitDTO.getId());
      }

      @Test
      void withdraw_withNonExistingDocUnit_shouldDeleteFromBucket()
          throws DocumentationUnitNotExistsException {
        when(documentationUnitRepository.findByDocumentNumber(testDocumentUnit.documentNumber()))
            .thenThrow(DocumentationUnitNotExistsException.class);
        when(caseLawBucket.getAllFilenamesByPath(testDocumentNumber + "/"))
            .thenReturn(List.of(withPrefix(testDocumentNumber)));

        var result = subject.withdrawDocumentationUnit(testDocumentNumber);

        assertThat(result.changedPaths()).isEmpty();
        assertThat(result.deletedPaths()).containsExactly(withPrefix(testDocumentNumber));

        verify(caseLawBucket).delete(withPrefix(testDocumentNumber));
        verify(documentationUnitRepository, never()).updatePortalPublicationStatus(any(), any());
        verify(documentationUnitRepository, never())
            .savePublicationDateTime(testDocumentUnit.uuid());
      }

      @Test
      void withdraw_withBucketException_shouldThrowPublishException() {
        when(caseLawBucket.getAllFilenamesByPath(testDocumentNumber + "/"))
            .thenReturn(List.of(withPrefix(testDocumentNumber)));
        doThrow(BucketException.class).when(caseLawBucket).delete(withPrefix(testDocumentNumber));

        assertThatExceptionOfType(PublishException.class)
            .isThrownBy(() -> subject.withdrawDocumentationUnit(testDocumentNumber))
            .withMessageContaining("Could not delete LDML from bucket.");
        verify(documentationUnitRepository, never())
            .updatePortalPublicationStatus(
                testDocumentUnit.uuid(), PortalPublicationStatus.WITHDRAWN);
        verify(documentationUnitRepository, never())
            .savePublicationDateTime(testDocumentUnit.uuid());
      }
    }

    @Nested
    class WithdrawDocumentationUnitWithChangelog {
      @Test
      void withdrawWithChangelog_shouldDeleteFromBucketAndWriteDeletionChangelog()
          throws DocumentationUnitNotExistsException {
        DecisionDTO decision =
            DecisionDTO.builder()
                .id(UUID.randomUUID())
                .documentNumber(testDocumentNumber)
                .portalPublicationStatus(PortalPublicationStatus.PUBLISHED)
                .build();
        when(databaseDocumentationUnitRepository.findById(decision.getId()))
            .thenReturn(Optional.of(decision));
        when(caseLawBucket.getAllFilenamesByPath(testDocumentNumber + "/"))
            .thenReturn(List.of(withPrefix(testDocumentNumber)));
        User user = mock(User.class);
        var changelogContent =
            """
                {"changed":[],"deleted":[TEST123456789/TEST123456789.xml]}
                """;
        when(objectMapper.writeValueAsString(any())).thenReturn(changelogContent);

        subject.withdrawDocumentationUnitWithChangelog(decision.getId(), user);

        verify(caseLawBucket).delete(withPrefix(testDocumentNumber));
        verify(caseLawBucket)
            .save(
                contains("changelog"),
                contains("\"deleted\":[" + withPrefix(testDocumentNumber) + "]"));
        verify(historyLogService)
            .saveHistoryLog(
                decision.getId(),
                user,
                HistoryLogEventType.PORTAL_PUBLICATION,
                "Dokeinheit wurde aus dem Portal zurückgezogen");
        verify(documentationUnitRepository)
            .updatePortalPublicationStatus(decision.getId(), PortalPublicationStatus.WITHDRAWN);
        verify(documentationUnitRepository, never()).savePublicationDateTime(decision.getId());
      }

      @Test
      void withdrawWithChangelog_withBucketException_shouldThrowPublishException() {
        UUID uuid = UUID.randomUUID();
        DecisionDTO decision =
            DecisionDTO.builder()
                .id(uuid)
                .documentNumber(testDocumentNumber)
                .portalPublicationStatus(PortalPublicationStatus.PUBLISHED)
                .build();
        when(databaseDocumentationUnitRepository.findById(uuid)).thenReturn(Optional.of(decision));
        when(caseLawBucket.getAllFilenamesByPath(testDocumentNumber + "/"))
            .thenReturn(List.of(withPrefix(testDocumentNumber)));
        User user = mock(User.class);
        doThrow(BucketException.class).when(caseLawBucket).delete(withPrefix(testDocumentNumber));

        assertThatExceptionOfType(PublishException.class)
            .isThrownBy(() -> subject.withdrawDocumentationUnitWithChangelog(uuid, user))
            .withMessageContaining("Could not delete LDML from bucket.");
        verify(historyLogService)
            .saveHistoryLog(
                uuid,
                user,
                HistoryLogEventType.PORTAL_PUBLICATION,
                "Dokeinheit konnte nicht aus dem Portal zurückgezogen werden");
      }

      @Test
      void withdrawWithChangelog_withJacksonException_shouldThrowChangelogException()
          throws JacksonException {
        UUID uuid = UUID.randomUUID();
        DecisionDTO decision =
            DecisionDTO.builder()
                .id(uuid)
                .documentNumber(testDocumentNumber)
                .portalPublicationStatus(PortalPublicationStatus.PUBLISHED)
                .build();
        when(databaseDocumentationUnitRepository.findById(uuid)).thenReturn(Optional.of(decision));
        when(caseLawBucket.getAllFilenamesByPath(testDocumentNumber + "/"))
            .thenReturn(List.of(withPrefix(testDocumentNumber)));
        User user = mock(User.class);
        when(objectMapper.writeValueAsString(any())).thenThrow(JacksonException.class);

        assertThatExceptionOfType(ChangelogException.class)
            .isThrownBy(() -> subject.withdrawDocumentationUnitWithChangelog(uuid, user))
            .withMessageContaining("Could not create changelog file");
        verify(historyLogService)
            .saveHistoryLog(
                uuid,
                user,
                HistoryLogEventType.PORTAL_PUBLICATION,
                "Dokeinheit konnte nicht aus dem Portal zurückgezogen werden");
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
        verify(historyLogService)
            .saveHistoryLog(
                uuid,
                user,
                HistoryLogEventType.PORTAL_PUBLICATION,
                "Dokeinheit konnte nicht aus dem Portal zurückgezogen werden");
      }
    }

    @Nested
    class UploadChangelog {
      @Test
      void uploadChangelog_shouldUpload() {
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
      void uploadDeletionChangelog_shouldUpload() {
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
      void uploadFullReindexChangelog_withRegularChangelogsDisabled_shouldUpload() {
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

  @Test
  void testPublishSnapshots_withoutSnapshotInDatabase_saveNewSnapshot()
      throws DocumentationUnitNotExistsException {
    List<UUID> documentationUnitIds = List.of(testDocumentUnitDTO.getId());

    when(documentationUnitRepository.findAllByCurrentStatus(PublicationStatus.PUBLISHED, 0, 10))
        .thenReturn(documentationUnitIds);
    when(documentationUnitRepository.loadDocumentationUnitDTO(testDocumentUnitDTO.getId()))
        .thenReturn(testDocumentUnitDTO);
    when(snapshotRepository.findByDocumentationUnitId(testDocumentUnitDTO.getId()))
        .thenReturn(Optional.empty());

    subject.publishSnapshots(0, 10);

    verify(snapshotRepository, times(1)).save(snapshotCaptor.capture());
    assertThat(snapshotCaptor.getValue())
        .extracting("documentationUnitId", "json")
        .containsExactly(testDocumentUnitDTO.getId(), testDocumentUnit);
  }

  @Test
  void testPublishSnapshots_withPendingProceeding_shouldNotSave()
      throws DocumentationUnitNotExistsException {
    UUID pendingProceedingUuid = UUID.randomUUID();
    List<UUID> documentationUnits = List.of(pendingProceedingUuid);

    when(documentationUnitRepository.findAllByCurrentStatus(PublicationStatus.PUBLISHED, 0, 10))
        .thenReturn(documentationUnits);
    when(documentationUnitRepository.findByUuid(pendingProceedingUuid))
        .thenReturn(PendingProceeding.builder().uuid(pendingProceedingUuid).build());

    subject.publishSnapshots(0, 10);

    verify(snapshotRepository, never()).save(any(PublishedDocumentationSnapshotEntity.class));
  }

  @Test
  void testPublishSnapshots_withExistingSnapshot_overwriteTheOldSnapshot()
      throws DocumentationUnitNotExistsException {
    List<UUID> documentationUnits = List.of(testDocumentUnit.uuid());
    PublishedDocumentationSnapshotEntity existingSnapshot =
        PublishedDocumentationSnapshotEntity.builder()
            .documentationUnitId(testDocumentUnit.uuid())
            .build();

    when(documentationUnitRepository.findAllByCurrentStatus(PublicationStatus.PUBLISHED, 0, 10))
        .thenReturn(documentationUnits);
    when(documentationUnitRepository.loadDocumentationUnitDTO(testDocumentUnit.uuid()))
        .thenReturn(testDocumentUnitDTO);
    when(snapshotRepository.findByDocumentationUnitId(testDocumentUnit.uuid()))
        .thenReturn(Optional.of(existingSnapshot));

    subject.publishSnapshots(0, 10);

    verify(snapshotRepository, times(1)).save(snapshotCaptor.capture());
    assertThat(snapshotCaptor.getValue())
        .extracting("documentationUnitId", "json")
        .containsExactly(testDocumentUnit.uuid(), testDocumentUnit);
  }

  private String withPrefix(String documentNumber) {
    return documentNumber + "/" + documentNumber + ".xml";
  }
}
