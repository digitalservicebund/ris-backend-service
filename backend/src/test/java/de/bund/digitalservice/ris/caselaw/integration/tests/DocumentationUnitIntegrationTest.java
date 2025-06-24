package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDSDocOffice;
import static de.bund.digitalservice.ris.caselaw.domain.PublicationStatus.PUBLISHED;
import static de.bund.digitalservice.ris.caselaw.domain.PublicationStatus.PUBLISHING;
import static de.bund.digitalservice.ris.caselaw.domain.PublicationStatus.UNPUBLISHED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import de.bund.digitalservice.ris.caselaw.EntityBuilderTestUtil;
import de.bund.digitalservice.ris.caselaw.SliceTestImpl;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentNumberPatternConfig;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseCourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDeletedDocumentationIdsRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentCategoryRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentNumberRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseFileNumberRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseLegalPeriodicalRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseProcedureRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseRegionRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeletedDocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingFileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentCategoryDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LeadingDecisionNormReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalPeriodicalDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.OriginalXmlDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.OriginalXmlRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PendingProceedingDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PreviousDecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ProcedureDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.RegionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationOfficeTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.LegalPeriodicalTransformer;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentService;
import de.bund.digitalservice.ris.caselaw.domain.BulkAssignProcedureRequest;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DateUtil;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitCreationParameters;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitHistoryLogRepository;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitHistoryLogService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitListItem;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitSearchInput;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitStatusService;
import de.bund.digitalservice.ris.caselaw.domain.HandoverReportRepository;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLog;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLogEventType;
import de.bund.digitalservice.ris.caselaw.domain.Image;
import de.bund.digitalservice.ris.caselaw.domain.InboxStatus;
import de.bund.digitalservice.ris.caselaw.domain.MailService;
import de.bund.digitalservice.ris.caselaw.domain.ManagementData;
import de.bund.digitalservice.ris.caselaw.domain.NormReference;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.Reference;
import de.bund.digitalservice.ris.caselaw.domain.ReferenceType;
import de.bund.digitalservice.ris.caselaw.domain.RelatedDocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.ShortTexts;
import de.bund.digitalservice.ris.caselaw.domain.SingleNorm;
import de.bund.digitalservice.ris.caselaw.domain.SourceValue;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisBodySpec;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;

@Sql(scripts = {"classpath:courts_init.sql"})
@Sql(
    scripts = {"classpath:courts_cleanup.sql"},
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class DocumentationUnitIntegrationTest extends BaseIntegrationTest {

  @Autowired private RisWebTestClient risWebTestClient;
  @Autowired private DatabaseDocumentationUnitRepository repository;
  @Autowired private DatabaseFileNumberRepository fileNumberRepository;
  @Autowired private DatabaseDocumentTypeRepository databaseDocumentTypeRepository;
  @Autowired private DatabaseDocumentCategoryRepository databaseDocumentCategoryRepository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @Autowired private DatabaseCourtRepository databaseCourtRepository;
  @Autowired private DatabaseRegionRepository regionRepository;
  @Autowired private DatabaseDeletedDocumentationIdsRepository deletedDocumentationIdsRepository;
  @Autowired private DatabaseLegalPeriodicalRepository legalPeriodicalRepository;
  @Autowired private OriginalXmlRepository originalXmlRepository;
  @Autowired private DatabaseDocumentNumberRepository databaseDocumentNumberRepository;
  @Autowired private DocumentationUnitStatusService documentationUnitStatusService;
  @Autowired private DocumentationUnitHistoryLogRepository historyLogRepository;
  @Autowired private DatabaseProcedureRepository procedureRepository;
  @Autowired private DocumentationUnitHistoryLogService historyLogService;

  @MockitoBean private MailService mailService;
  @MockitoBean private AttachmentService attachmentService;
  @MockitoBean private HandoverReportRepository handoverReportRepository;
  @MockitoBean DocumentNumberPatternConfig documentNumberPatternConfig;

  private final DocumentationOffice docOffice = buildDSDocOffice();
  private DocumentationOfficeDTO documentationOffice;

  @BeforeEach
  void setUp() {
    documentationOffice =
        documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation());
  }

  @AfterEach
  void cleanUp() {
    fileNumberRepository.deleteAll();
    repository.deleteAll();
    databaseDocumentTypeRepository.deleteAll();
    databaseDocumentCategoryRepository.deleteAll();
    databaseDocumentNumberRepository.deleteAll();
    procedureRepository.deleteAll();
  }

  @Test
  void testForCorrectDbEntryAfterNewDocumentationUnitCreation() {
    when(documentNumberPatternConfig.getDocumentNumberPatterns())
        .thenReturn(Map.of("DS", "XXRE0******YY"));

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/new")
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().documentNumber()).startsWith("XXRE0");
              assertThat(response.getResponseBody().status())
                  .isEqualTo(
                      Status.builder().publicationStatus(UNPUBLISHED).withError(false).build());
            });

    List<DocumentationUnitDTO> list = repository.findAll();
    assertThat(list).hasSize(1);

    User user = User.builder().documentationOffice(docOffice).build();
    var historyLogs = historyLogRepository.findByDocumentationUnitId(list.getFirst().getId(), user);
    assertThat(historyLogs).hasSize(1);
    assertThat(historyLogs.getFirst().eventType()).isEqualTo(HistoryLogEventType.CREATE);
    assertThat(historyLogs.getFirst().documentationOffice()).isEqualTo("DS");
    assertThat(historyLogs.getFirst().description()).isEqualTo("Dokeinheit angelegt");
    assertThat(historyLogs.getFirst().createdBy()).isEqualTo("testUser");
    assertThat(historyLogs.getFirst().createdAt())
        .isCloseTo(Instant.now(), within(5, ChronoUnit.SECONDS));
  }

  @Test
  void testParametrizedDocumentationUnitCreation() {
    when(documentNumberPatternConfig.getDocumentNumberPatterns())
        .thenReturn(Map.of("DS", "XXRE0******YY"));

    var court = databaseCourtRepository.findBySearchStr("AG Aachen", 100).getFirst();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/new")
        .bodyValue(
            DocumentationUnitCreationParameters.builder()
                .court(Court.builder().id(court.getId()).type("AG").location("Aachen").build())
                .fileNumber("abc")
                .decisionDate(LocalDate.of(2021, 1, 1))
                .build())
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().coreData().court().id())
                  .isEqualTo(court.getId());
              assertThat(response.getResponseBody().coreData().court().type()).isEqualTo("AG");
              assertThat(response.getResponseBody().coreData().court().location())
                  .isEqualTo("Aachen");
              assertThat(response.getResponseBody().coreData().fileNumbers()).hasSize(1);
              assertThat(response.getResponseBody().coreData().fileNumbers().getFirst())
                  .isEqualTo("abc");
            });

    List<DocumentationUnitDTO> list = repository.findAll();
    assertThat(list).hasSize(1);
  }

  @Test
  void test_deleteDocumentationUnit_shouldRecycleDocumentNumber() {
    when(documentNumberPatternConfig.getDocumentNumberPatterns())
        .thenReturn(Map.of("DS", "XXRE0******YY"));
    when(documentNumberPatternConfig.hasValidPattern(anyString(), anyString()))
        .thenReturn(Boolean.TRUE);

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/new")
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().documentNumber()).startsWith("XXRE0");
            });

    assertThat(repository.findAll()).hasSize(1);
    var deletedDocumentationUnit = repository.findAll().get(0);
    var reusableDocumentNumber = deletedDocumentationUnit.getDocumentNumber();

    risWebTestClient
        .withDefaultLogin()
        .delete()
        .uri("/api/v1/caselaw/documentunits/" + deletedDocumentationUnit.getId())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .consumeWith(response -> assertThat(response.getResponseBody()).isNotNull());

    assertThat(repository.findAll()).isEmpty();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/new")
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().documentNumber())
                  .isEqualTo(reusableDocumentNumber);
            });
  }

  @Test
  void test_deleteMigratedDocumentationUnit_shouldSucceed() {
    when(documentNumberPatternConfig.getDocumentNumberPatterns())
        .thenReturn(Map.of("DS", "XXRE0******YY"));
    when(documentNumberPatternConfig.hasValidPattern(anyString(), anyString()))
        .thenReturn(Boolean.TRUE);
    when(mailService.getHandoverResult(any(), any())).thenReturn(List.of());
    when(handoverReportRepository.getAllByEntityId(any())).thenReturn(List.of());

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/new")
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().documentNumber()).startsWith("XXRE0");
            });

    assertThat(repository.findAll()).hasSize(1);
    var deletedDocumentationUnit = repository.findAll().get(0);
    var original =
        OriginalXmlDTO.builder()
            .documentationUnitId(deletedDocumentationUnit.getId())
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .content("<?xml version=\"1.0\" encoding=\"UTF-8\"?><juris-r></juris-r>")
            .build();
    originalXmlRepository.save(original);

    risWebTestClient
        .withDefaultLogin()
        .delete()
        .uri("/api/v1/caselaw/documentunits/" + deletedDocumentationUnit.getId())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .consumeWith(response -> assertThat(response.getResponseBody()).isNotNull());

    assertThat(repository.findAll()).isEmpty();
    assertThat(originalXmlRepository.findAll()).isEmpty();
  }

  @Test
  void testForFileNumbersDbEntryAfterUpdateByUuid() {
    DocumentationUnitDTO dto =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
            repository, documentationOffice, "1234567890123");

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(dto.getId())
            .documentNumber(dto.getDocumentNumber())
            .coreData(
                CoreData.builder()
                    .fileNumbers(List.of("AkteX"))
                    .documentationOffice(docOffice)
                    .build())
            .shortTexts(ShortTexts.builder().decisionName("decisionName").build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getId())
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().documentNumber()).isEqualTo("1234567890123");
              assertThat(response.getResponseBody().coreData().fileNumbers().get(0))
                  .isEqualTo("AkteX");
            });

    List<DocumentationUnitDTO> list = repository.findAll();
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getDocumentNumber()).isEqualTo("1234567890123");

    List<FileNumberDTO> fileNumberEntries = fileNumberRepository.findAll();
    assertThat(fileNumberEntries).hasSize(1);
    assertThat(fileNumberEntries.get(0).getValue()).isEqualTo("AkteX");
  }

  @Test
  void testDeleteLeadingDecisionNormReferencesForNonBGHDecisions() {
    CourtDTO bghCourt =
        databaseCourtRepository.save(
            CourtDTO.builder()
                .type("BGH")
                .isSuperiorCourt(true)
                .isForeignCourt(false)
                .jurisId(new Random().nextInt())
                .build());
    CourtDTO lgCourt =
        databaseCourtRepository.save(
            CourtDTO.builder()
                .type("LG")
                .isSuperiorCourt(false)
                .isForeignCourt(false)
                .jurisId(new Random().nextInt())
                .build());

    DocumentationUnitDTO dto =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
            repository,
            DecisionDTO.builder()
                .documentNumber("1234567890123")
                .leadingDecisionNormReferences(
                    List.of(
                        LeadingDecisionNormReferenceDTO.builder()
                            .normReference("BGB §1")
                            .rank(1)
                            .build()))
                .court(bghCourt)
                .documentationOffice(documentationOffice));

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(dto.getId())
            .documentNumber(dto.getDocumentNumber())
            .coreData(
                CoreData.builder()
                    .leadingDecisionNormReferences(List.of("BGB §1"))
                    .documentationOffice(docOffice)
                    .court(Court.builder().id(lgCourt.getId()).build())
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getId())
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().coreData().leadingDecisionNormReferences())
                  .isEmpty();
            });
  }

  @Test
  void testUpdateNormReferenceWithoutNormAbbreviationAndWithNormAbbreviationRawValue() {
    DocumentationUnitDTO dto =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
            repository, documentationOffice, "1234567890123");

    List<SingleNorm> singleNorms = List.of(SingleNorm.builder().singleNorm("Art 7 S 1").build());

    List<NormReference> norms =
        List.of(
            NormReference.builder()
                .normAbbreviation(null)
                .normAbbreviationRawValue("EWGAssRBes 1/80")
                .singleNorms(singleNorms)
                .build());

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(dto.getId())
            .documentNumber(dto.getDocumentNumber())
            .contentRelatedIndexing(ContentRelatedIndexing.builder().norms(norms).build())
            .coreData(CoreData.builder().documentationOffice(docOffice).build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getId())
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().documentNumber()).isEqualTo("1234567890123");
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .norms()
                          .get(0)
                          .singleNorms()
                          .get(0)
                          .singleNorm())
                  .isEqualTo("Art 7 S 1");
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .norms()
                          .get(0)
                          .normAbbreviation())
                  .isNull();
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .norms()
                          .get(0)
                          .normAbbreviationRawValue())
                  .isEqualTo("EWGAssRBes 1/80");
            });

    List<DocumentationUnitDTO> list = repository.findAll();
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getDocumentNumber()).isEqualTo("1234567890123");
  }

  @Test
  void
      testUpdateNormReferenceWithoutNormAbbreviationAndWithNormAbbreviationRawValue_shouldAlsoGroupNorms() {
    DocumentationUnitDTO dto =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
            repository, documentationOffice, "1234567890123");

    List<SingleNorm> singleNorms =
        List.of(
            SingleNorm.builder().singleNorm("Art 7 S 1").build(),
            SingleNorm.builder().singleNorm("Art 8 S 1").build());

    List<NormReference> norms =
        List.of(
            NormReference.builder()
                .normAbbreviation(null)
                .normAbbreviationRawValue("EWGAssRBes 1/80")
                .singleNorms(singleNorms)
                .build());

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(dto.getId())
            .documentNumber(dto.getDocumentNumber())
            .contentRelatedIndexing(ContentRelatedIndexing.builder().norms(norms).build())
            .coreData(CoreData.builder().documentationOffice(docOffice).build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getId())
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().documentNumber()).isEqualTo("1234567890123");
              assertThat(response.getResponseBody().contentRelatedIndexing().norms()).hasSize(1);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .norms()
                          .getFirst()
                          .singleNorms()
                          .getFirst()
                          .singleNorm())
                  .isEqualTo("Art 7 S 1");
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .norms()
                          .getFirst()
                          .singleNorms()
                          .get(1)
                          .singleNorm())
                  .isEqualTo("Art 8 S 1");
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .norms()
                          .getFirst()
                          .normAbbreviation())
                  .isNull();
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .norms()
                          .getFirst()
                          .normAbbreviationRawValue())
                  .isEqualTo("EWGAssRBes 1/80");
            });

    List<DocumentationUnitDTO> list = repository.findAll();
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getDocumentNumber()).isEqualTo("1234567890123");
  }

  @Test
  void testSetRegionForCourt() {
    RegionDTO region = regionRepository.save(RegionDTO.builder().code("DEU").build());

    CourtDTO bghCourt =
        databaseCourtRepository.save(
            CourtDTO.builder()
                .type("BGH")
                .location("Karlsruhe")
                .isSuperiorCourt(true)
                .isForeignCourt(false)
                .jurisId(new Random().nextInt())
                .region(region)
                .build());

    DocumentationUnitDTO dto =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
            repository, documentationOffice, "1234567890123");

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(dto.getId())
            .documentNumber(dto.getDocumentNumber())
            .coreData(
                CoreData.builder()
                    .documentationOffice(docOffice)
                    .court(Court.builder().id(bghCourt.getId()).build())
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + dto.getId())
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().coreData().court().region()).isEqualTo("DEU");
            });
  }

  @Test
  void testDocumentTypeToSetIdFromLookuptable() {
    var categoryB =
        databaseDocumentCategoryRepository.saveAndFlush(
            DocumentCategoryDTO.builder().label("B").build());
    var categoryR =
        databaseDocumentCategoryRepository.saveAndFlush(
            DocumentCategoryDTO.builder().label("R").build());
    var categoryC =
        databaseDocumentCategoryRepository.saveAndFlush(
            DocumentCategoryDTO.builder().label("C").build());

    databaseDocumentTypeRepository.save(
        DocumentTypeDTO.builder()
            .abbreviation("ABC")
            .category(categoryB)
            .label("ABC123")
            .multiple(true)
            .build());

    databaseDocumentTypeRepository.save(
        DocumentTypeDTO.builder()
            .abbreviation("ABC")
            .category(categoryC)
            .label("ABC123")
            .multiple(true)
            .build());

    var documentTypeDTOR =
        databaseDocumentTypeRepository.save(
            DocumentTypeDTO.builder()
                .abbreviation("ABC")
                .category(categoryR)
                .label("ABC123")
                .multiple(true)
                .build());

    // TODO find out why this is necessary when the whole test class is executed
    repository.deleteAll();

    DocumentationUnitDTO documentationUnitDto =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
            repository, documentationOffice, "1234567890123");

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(documentationUnitDto.getId())
            .documentNumber(documentationUnitDto.getDocumentNumber())
            .coreData(
                CoreData.builder()
                    .documentType(
                        DocumentType.builder()
                            .uuid(documentTypeDTOR.getId())
                            .jurisShortcut(documentTypeDTOR.getAbbreviation())
                            .label(documentTypeDTOR.getLabel())
                            .build())
                    .documentationOffice(docOffice)
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + decisionFromFrontend.uuid())
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().coreData().documentType().label())
                  .isEqualTo(documentTypeDTOR.getLabel());
              assertThat(response.getResponseBody().coreData().documentType().jurisShortcut())
                  .isEqualTo(documentTypeDTOR.getAbbreviation());
            });

    List<DocumentationUnitDTO> list = repository.findAll();
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getDocumentType().getId()).isEqualTo(documentTypeDTOR.getId());
    assertThat(list.get(0).getDocumentType()).isNotNull();
  }

  @Test
  void testUndoSettingDocumentType() {
    var docType =
        databaseDocumentTypeRepository.saveAndFlush(
            DocumentTypeDTO.builder().abbreviation("test").multiple(true).build());

    DocumentationUnitDTO dto =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
            repository,
            DecisionDTO.builder()
                .documentNumber("1234567890123")
                .documentType(docType)
                .documentationOffice(documentationOffice));

    assertThat(repository.findAll()).hasSize(1);
    assertThat(repository.findById(dto.getId())).isPresent();

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(dto.getId())
            .documentNumber(dto.getDocumentNumber())
            .coreData(CoreData.builder().documentationOffice(docOffice).documentType(null).build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + decisionFromFrontend.uuid())
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().coreData().documentType()).isNull();
            });

    List<DocumentationUnitDTO> list = repository.findAll();
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getDocumentType()).isNull();
  }

  @Test
  void testSearchResultsAreDeterministic() {
    var office = documentationOffice;

    var documentNumberToExclude = "KORE000000000";

    for (int i = 0; i < 21; i++) {
      var randomDocNumber =
          i == 0 ? documentNumberToExclude : RandomStringUtils.random(10, true, true);
      CourtDTO court =
          databaseCourtRepository.save(
              CourtDTO.builder()
                  .type("LG")
                  .location("Kassel")
                  .isSuperiorCourt(true)
                  .isForeignCourt(false)
                  .jurisId(i)
                  .build());

      DocumentationUnitDTO dto =
          EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
              repository,
              DecisionDTO.builder()
                  .documentNumber(randomDocNumber)
                  .court(court)
                  .documentationOffice(office));

      repository.findById(dto.getId()).get();
    }

    assertThat(repository.findAll()).hasSize(21);

    List<UUID> responseUUIDs = new ArrayList<>();

    PreviousDecision proceedingDecision = PreviousDecision.builder().build();
    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri(
            "/api/v1/caselaw/documentunits/"
                + "search-linkable-documentation-units?pg=0&sz=20&documentNumber="
                + documentNumberToExclude)
        .bodyValue(proceedingDecision)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new TypeReference<SliceTestImpl<RelatedDocumentationUnit>>() {})
        .consumeWith(
            response -> {
              List<RelatedDocumentationUnit> content = response.getResponseBody().getContent();
              assertThat(content).isNotNull();
              assertThat(content).hasSize(20);
              assertThat(content)
                  .extracting("documentNumber")
                  .doesNotContain(documentNumberToExclude);

              responseUUIDs.addAll(
                  content.stream().map(RelatedDocumentationUnit::getUuid).toList());
            });

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri(
            "/api/v1/caselaw/documentunits/"
                + "search-linkable-documentation-units?pg=0&sz=20&documentNumber="
                + documentNumberToExclude)
        .bodyValue(proceedingDecision)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(new TypeReference<SliceTestImpl<RelatedDocumentationUnit>>() {})
        .consumeWith(
            response -> {
              List<RelatedDocumentationUnit> content = response.getResponseBody().getContent();
              assertThat(content).isNotNull();

              List<UUID> responseUUIDs2 =
                  content.stream().map(RelatedDocumentationUnit::getUuid).toList();

              assertThat(responseUUIDs2).hasSize(20);
              assertThat(responseUUIDs2).isEqualTo(responseUUIDs);
            });
  }

  @Test
  void testSearchByDocumentationUnitSearchInput() {
    UUID otherDocOfficeUuid = documentationOfficeRepository.findByAbbreviation("BGH").getId();

    List<UUID> docOfficeIds =
        List.of(
            documentationOffice.getId(),
            documentationOffice.getId(),
            documentationOffice.getId(),
            documentationOffice.getId(),
            otherDocOfficeUuid,
            otherDocOfficeUuid);
    List<String> documentNumbers =
        List.of(
            "ABCD202300007",
            "EFGH202200123",
            "IJKL202101234",
            "MNOP202300099",
            "QRST202200102",
            "UVWX202311090");
    List<String> fileNumbers = List.of("jkl", "ghi", "def", "ABC", "mno", "pqr");
    List<String> courtTypes = List.of("MNO", "PQR", "STU", "VWX", "YZA", "BCD");
    List<String> courtLocations =
        List.of("Hamburg", "München", "Berlin", "Frankfurt", "Köln", "Leipzig");
    List<LocalDate> decisionDates =
        List.of(
            LocalDate.parse("2021-01-02"),
            LocalDate.parse("2022-02-03"),
            LocalDate.parse("2023-03-04"),
            LocalDate.parse("2023-08-01"),
            LocalDate.parse("2023-08-10"),
            LocalDate.parse("2023-09-10"));
    List<PublicationStatus> statuses =
        List.of(PUBLISHED, UNPUBLISHED, PUBLISHING, PUBLISHED, UNPUBLISHED, PUBLISHED);
    List<Boolean> errorStatuses = List.of(false, true, true, false, true, true);

    for (int i = 0; i < 6; i++) {

      CourtDTO court =
          databaseCourtRepository.save(
              CourtDTO.builder()
                  .type(courtTypes.get(i))
                  .location(courtLocations.get(i))
                  .isSuperiorCourt(true)
                  .isForeignCourt(false)
                  .jurisId(new Random().nextInt())
                  .build());

      EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
          repository,
          DecisionDTO.builder()
              .documentNumber(documentNumbers.get(i))
              .court(court)
              .date(decisionDates.get(i))
              .documentationOffice(DocumentationOfficeDTO.builder().id(docOfficeIds.get(i)).build())
              .fileNumbers(
                  List.of(
                      FileNumberDTO.builder().value(fileNumbers.get(i)).rank((long) i).build())),
          statuses.get(i),
          errorStatuses.get(i));
    }

    DocumentationOfficeDTO otherDocumentationOffice =
        documentationOfficeRepository.findByAbbreviation("BGH");
    String documentNumber = "1234567890123";

    EntityBuilderTestUtil.createAndSavePendingDocumentationUnit(
        repository, otherDocumentationOffice, documentationOffice, documentNumber);

    // no search criteria
    DocumentationUnitSearchInput searchInput = DocumentationUnitSearchInput.builder().build();
    // the unpublished one from the other docoffice is not in it, the others are ordered
    // by documentNumber
    assertThat(extractDocumentNumbersFromSearchCall(searchInput))
        .contains(
            "ABCD202300007", "EFGH202200123", "IJKL202101234", "MNOP202300099", "UVWX202311090");

    // pending docunits are only visible in the big search, if I am the owning docoffice (not the
    // creating)
    assertThat(extractDocumentNumbersFromSearchCall(searchInput)).doesNotContain("1234567890123");

    // by documentNumber
    searchInput = DocumentationUnitSearchInput.builder().documentNumber("abc").build();
    assertThat(extractDocumentNumbersFromSearchCall(searchInput)).containsExactly("ABCD202300007");

    // by fileNumber
    searchInput = DocumentationUnitSearchInput.builder().fileNumber("abc").build();
    assertThat(extractDocumentNumbersFromSearchCall(searchInput)).containsExactly("MNOP202300099");

    // by fileNumber start
    searchInput = DocumentationUnitSearchInput.builder().fileNumber("ab").build();
    assertThat(extractDocumentNumbersFromSearchCall(searchInput)).containsExactly("MNOP202300099");

    // by fileNumber ending without wildcard (%) should not return anything
    searchInput = DocumentationUnitSearchInput.builder().fileNumber("bc").build();
    assertThat(extractDocumentNumbersFromSearchCall(searchInput)).isEmpty();

    // by fileNumber ending
    searchInput = DocumentationUnitSearchInput.builder().fileNumber("%bc").build();
    assertThat(extractDocumentNumbersFromSearchCall(searchInput)).containsExactly("MNOP202300099");

    // by documentNumber & fileNumber
    searchInput =
        DocumentationUnitSearchInput.builder().fileNumber("abc").documentNumber("abc").build();
    assertThat(extractDocumentNumbersFromSearchCall(searchInput)).isEmpty();

    // by court
    searchInput =
        DocumentationUnitSearchInput.builder().courtType("pqr").courtLocation("münchen").build();

    assertThat(extractDocumentNumbersFromSearchCall(searchInput)).containsExactly("EFGH202200123");

    // by decisionDate
    searchInput = DocumentationUnitSearchInput.builder().decisionDate(decisionDates.get(2)).build();

    assertThat(extractDocumentNumbersFromSearchCall(searchInput)).containsExactly("IJKL202101234");

    // by status
    searchInput =
        DocumentationUnitSearchInput.builder()
            .status(Status.builder().publicationStatus(UNPUBLISHED).build())
            .build();

    assertThat(extractDocumentNumbersFromSearchCall(searchInput)).containsExactly("EFGH202200123");

    searchInput =
        DocumentationUnitSearchInput.builder()
            .status(Status.builder().publicationStatus(PUBLISHED).build())
            .build();

    assertThat(extractDocumentNumbersFromSearchCall(searchInput))
        .contains("ABCD202300007", "MNOP202300099", "UVWX202311090");

    // by error status
    searchInput =
        DocumentationUnitSearchInput.builder()
            .status(Status.builder().withError(true).build())
            .build();
    // the docunit with error from the other docoffice should not appear
    assertThat(extractDocumentNumbersFromSearchCall(searchInput))
        .contains("EFGH202200123", "IJKL202101234");

    // by documentation office
    searchInput = DocumentationUnitSearchInput.builder().myDocOfficeOnly(true).build();
    assertThat(extractDocumentNumbersFromSearchCall(searchInput))
        .contains("ABCD202300007", "EFGH202200123", "IJKL202101234", "MNOP202300099");

    // between two decision dates
    LocalDate start = LocalDate.parse("2022-02-01");
    LocalDate end = LocalDate.parse("2023-08-05");
    searchInput =
        DocumentationUnitSearchInput.builder().decisionDate(start).decisionDateEnd(end).build();
    assertThat(extractDocumentNumbersFromSearchCall(searchInput))
        .contains("EFGH202200123", "IJKL202101234", "MNOP202300099");

    // all combined
    searchInput =
        DocumentationUnitSearchInput.builder()
            .documentNumber("abc")
            .courtType("MNO")
            .courtLocation("Hamburg")
            .decisionDate(decisionDates.get(0))
            .status(Status.builder().publicationStatus(PUBLISHED).build())
            .build();

    assertThat(extractDocumentNumbersFromSearchCall(searchInput)).contains("ABCD202300007");
  }

  @Test
  void testSearchByFileNumber_withFileNumberAndDeviatingFileNumber_shouldOnlyReturnOneResult() {

    EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        repository,
        DecisionDTO.builder()
            .documentNumber("documentNumber")
            .date(LocalDate.parse("2021-01-02"))
            .documentationOffice(documentationOffice)
            .fileNumbers(
                List.of(FileNumberDTO.builder().value("Vf. 19-VIII-22 (e.A.)").rank(1L).build()))
            .deviatingFileNumbers(
                List.of(
                    DeviatingFileNumberDTO.builder().value("Vf.19-VIII-22 ea").rank(1L).build())));

    DocumentationUnitSearchInput searchInput =
        DocumentationUnitSearchInput.builder().fileNumber("Vf.").build();
    assertThat(extractDocumentNumbersFromSearchCall(searchInput)).hasSize(1);
    assertThat(extractDocumentNumbersFromSearchCall(searchInput).get(0)).contains("documentNumber");
  }

  private List<String> extractDocumentNumbersFromSearchCall(
      DocumentationUnitSearchInput searchInput) {

    MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
    queryParams.add("pg", "0");
    queryParams.add("sz", "30");

    if (searchInput.documentNumber() != null) {
      queryParams.add("documentNumber", searchInput.documentNumber());
    }

    if (searchInput.fileNumber() != null) {
      queryParams.add("fileNumber", searchInput.fileNumber());
    }

    if (searchInput.courtType() != null) {
      queryParams.add("courtType", searchInput.courtType());
    }

    if (searchInput.courtLocation() != null) {
      queryParams.add("courtLocation", searchInput.courtLocation());
    }

    if (searchInput.decisionDate() != null) {
      queryParams.add("decisionDate", searchInput.decisionDate().toString());
    }

    if (searchInput.decisionDateEnd() != null) {
      queryParams.add("decisionDateEnd", searchInput.decisionDateEnd().toString());
    }

    if (searchInput.status() != null) {
      if (searchInput.status().publicationStatus() != null) {
        queryParams.add("publicationStatus", searchInput.status().publicationStatus().toString());
      }
      queryParams.add("withError", String.valueOf(searchInput.status().withError()));
    }

    queryParams.add("myDocOfficeOnly", String.valueOf(searchInput.myDocOfficeOnly()));
    URI uri =
        new DefaultUriBuilderFactory()
            .builder()
            .path("/api/v1/caselaw/documentunits/search")
            .queryParams(queryParams)
            .build();

    List<DocumentationUnitListItem> content =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri(uri)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<SliceTestImpl<DocumentationUnitListItem>>() {})
            .returnResult()
            .getResponseBody()
            .getContent();

    return content.stream().map(DocumentationUnitListItem::documentNumber).toList();
  }

  @Test
  void
      testSearchLinkableDocumentationUnits_shouldOnlyFindPublishedOrMineOrPendingWhenCreatingDocoffice() {
    LocalDate date = LocalDate.parse("2023-02-02");

    var du1 =
        createDocumentationUnit(date, List.of("AkteZ"), "DS", PublicationStatus.UNPUBLISHED, null);
    var du2 =
        createDocumentationUnit(date, List.of("AkteZ"), "DS", PublicationStatus.PUBLISHED, null);
    var du3 =
        createDocumentationUnit(
            date, List.of("AkteZ"), "DS", PublicationStatus.EXTERNAL_HANDOVER_PENDING, "BGH");
    var du4 =
        createDocumentationUnit(
            date, List.of("AkteZ"), "CC-RIS", PublicationStatus.UNPUBLISHED, null);
    var du5 =
        createDocumentationUnit(
            date, List.of("AkteZ"), "CC-RIS", PublicationStatus.PUBLISHED, null);
    var du6 =
        createDocumentationUnit(
            date, List.of("AkteZ"), "CC-RIS", PublicationStatus.EXTERNAL_HANDOVER_PENDING, "DS");

    RisBodySpec<SliceTestImpl<RelatedDocumentationUnit>> risBody =
        risWebTestClient
            .withDefaultLogin()
            .put()
            .uri(
                "/api/v1/caselaw/documentunits/search-linkable-documentation-units?pg=0&sz=30&documentNumber=KORE000000000")
            .bodyValue(RelatedDocumentationUnit.builder().fileNumber("AkteZ").build())
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<>() {});
    List<RelatedDocumentationUnit> content = risBody.returnResult().getResponseBody().getContent();
    assertThat(content).hasSize(5);
    assertThat(content)
        .extracting(RelatedDocumentationUnit::getDocumentNumber)
        .doesNotContain(du4.getDocumentNumber())
        .containsExactlyInAnyOrder(
            du1.getDocumentNumber(),
            du2.getDocumentNumber(),
            du3.getDocumentNumber(),
            du5.getDocumentNumber(),
            du6.getDocumentNumber());
  }

  private DocumentationUnitDTO createDocumentationUnit(
      LocalDate decisionDate,
      List<String> fileNumbers,
      String documentOfficeLabel,
      PublicationStatus status,
      String creatingDocOfficeLabel) {

    DocumentationOfficeDTO documentOffice =
        documentationOfficeRepository.findByAbbreviation(documentOfficeLabel);

    DocumentationOfficeDTO creatingDocOffice = null;
    if (creatingDocOfficeLabel != null) {
      creatingDocOffice = documentationOfficeRepository.findByAbbreviation(creatingDocOfficeLabel);
    }
    assertThat(documentOffice).isNotNull();

    return EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        repository,
        DecisionDTO.builder()
            .documentationOffice(documentOffice)
            .creatingDocumentationOffice(creatingDocOffice)
            .documentNumber("XX" + RandomStringUtils.randomAlphanumeric(11))
            .date(decisionDate)
            .documentationOffice(documentOffice)
            .fileNumbers(
                fileNumbers == null
                    ? new ArrayList<>()
                    : new ArrayList<>(
                        fileNumbers.stream()
                            .map(fn -> FileNumberDTO.builder().value(fn).rank(1L).build())
                            .toList())),
        status);
  }

  @Test
  void testDeleteByUuid_withExistingReference_shouldNotRecycleDocumentNumberAfterFailedDeletion() {
    DocumentationUnitDTO referencedDTO =
        DecisionDTO.builder()
            .documentNumber("ZZRE202400001")
            .documentationOffice(documentationOffice)
            .build();
    repository.save(referencedDTO);

    EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
        repository,
        DecisionDTO.builder()
            .documentNumber("ZZRE202400002")
            .documentationOffice(documentationOffice)
            .previousDecisions(
                List.of(
                    PreviousDecisionDTO.builder()
                        .documentNumber(referencedDTO.getDocumentNumber())
                        .rank(1)
                        .build())));

    when(documentNumberPatternConfig.hasValidPattern(anyString(), anyString())).thenReturn(true);
    risWebTestClient
        .withDefaultLogin()
        .delete()
        .uri("/api/v1/caselaw/documentunits/" + referencedDTO.getId())
        .exchange()
        .expectStatus()
        .is5xxServerError();

    List<DeletedDocumentationUnitDTO> allDeletedIds = deletedDocumentationIdsRepository.findAll();
    assertThat(allDeletedIds).isEmpty();

    List<DocumentationUnitDTO> allDTOsAfterDelete = repository.findAll();
    assertThat(allDTOsAfterDelete)
        .extracting("documentNumber")
        .containsExactlyInAnyOrder("ZZRE202400001", "ZZRE202400002");
  }

  @Test
  void
      testGenerateNewDocumentationUnit_withDeletedDocumentNumberWhichExistAsDocumentationUnit_shouldRemoveDeletedDocumentsEntryAndGenerateANewDocumentNumber() {
    DocumentationUnitDTO referencedDTO =
        DecisionDTO.builder()
            .documentNumber("ZZRE202400001")
            .documentationOffice(documentationOffice)
            .build();
    repository.save(referencedDTO);

    DeletedDocumentationUnitDTO deletedDocumentationUnitDTO =
        DeletedDocumentationUnitDTO.builder()
            .abbreviation("DS")
            .documentNumber("ZZRE202400001")
            .year(DateUtil.getYear())
            .build();
    deletedDocumentationIdsRepository.save(deletedDocumentationUnitDTO);
    databaseDocumentNumberRepository.save(
        DocumentNumberDTO.builder()
            .documentationOfficeAbbreviation("DS")
            .lastNumber(1)
            .year(DateUtil.getYear())
            .build());
    when(documentNumberPatternConfig.getDocumentNumberPatterns())
        .thenReturn(Map.of("DS", "ZZREYYYY*****"));

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/new")
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(Decision.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody())
                    .extracting("documentNumber")
                    .isEqualTo("ZZRE" + LocalDate.now().getYear() + "00002"));

    List<DeletedDocumentationUnitDTO> allDeletedIds = deletedDocumentationIdsRepository.findAll();
    assertThat(allDeletedIds).isEmpty();
  }

  @Test
  void testGenerateNewDocumentationUnit_withInternalUser_shouldSucceed() {
    when(documentNumberPatternConfig.getDocumentNumberPatterns())
        .thenReturn(Map.of("DS", "ZZREYYYY*****"));
    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/new")
        .exchange()
        .expectStatus()
        .isCreated();
  }

  @Test
  void testGenerateNewDocumentationUnit_ManagementData_shouldSetCreatedBy() {
    when(documentNumberPatternConfig.getDocumentNumberPatterns())
        .thenReturn(Map.of("DS", "ZZREYYYY*****"));
    var createdDocUnit =
        risWebTestClient
            .withDefaultLogin()
            .put()
            .uri("/api/v1/caselaw/documentunits/new")
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(Decision.class)
            .returnResult()
            .getResponseBody();

    ManagementData createdManagementData = createdDocUnit.managementData();
    assertThat(createdManagementData.createdByDocOffice()).isEqualTo("DS");
    assertThat(createdManagementData.createdAtDateTime())
        .isBetween(Instant.now().minusSeconds(10), Instant.now());
    assertThat(createdManagementData.createdByName()).isEqualTo("testUser");

    var docUnitGetSameDocOffice =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/documentunits/" + createdDocUnit.documentNumber())
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .expectBody(Decision.class)
            .returnResult()
            .getResponseBody();

    ManagementData managementDataSameDocOffice = docUnitGetSameDocOffice.managementData();
    assertThat(managementDataSameDocOffice.createdByDocOffice()).isEqualTo("DS");
    assertThat(managementDataSameDocOffice.createdAtDateTime())
        .isBetween(Instant.now().minusSeconds(10), Instant.now());
    assertThat(managementDataSameDocOffice.createdByName()).isEqualTo("testUser");
  }

  @Test
  void
      testGenerateNewDocumentationUnit_ManagementData_shouldSetCreatedByAndHideNameForOtherDocOffice()
          throws DocumentationUnitNotExistsException {
    when(documentNumberPatternConfig.getDocumentNumberPatterns())
        .thenReturn(Map.of("DS", "ZZREYYYY*****"));
    var createdDocUnit =
        risWebTestClient
            .withDefaultLogin()
            .put()
            .uri("/api/v1/caselaw/documentunits/new")
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(Decision.class)
            .returnResult()
            .getResponseBody();

    ManagementData createdManagementData = createdDocUnit.managementData();
    assertThat(createdManagementData.createdByDocOffice()).isEqualTo("DS");
    assertThat(createdManagementData.createdAtDateTime())
        .isBetween(Instant.now().minusSeconds(10), Instant.now());
    assertThat(createdManagementData.createdByName()).isEqualTo("testUser");

    // Publish the doc unit so that other doc office can access it
    var status =
        Status.builder()
            .publicationStatus(PublicationStatus.PUBLISHED)
            .createdAt(Instant.now())
            .build();
    documentationUnitStatusService.update(createdDocUnit.documentNumber(), status, null);

    var docUnitForDifferentDocOffice =
        risWebTestClient
            .withLogin("/BGH")
            .get()
            .uri("/api/v1/caselaw/documentunits/" + createdDocUnit.documentNumber())
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .expectBody(Decision.class)
            .returnResult()
            .getResponseBody();

    ManagementData managementDataForOtherDocOffice = docUnitForDifferentDocOffice.managementData();
    assertThat(managementDataForOtherDocOffice.createdByDocOffice()).isEqualTo("DS");
    assertThat(managementDataForOtherDocOffice.createdAtDateTime())
        .isBetween(Instant.now().minusSeconds(10), Instant.now());
    assertThat(managementDataForOtherDocOffice.createdByName()).isNull();
  }

  @Test
  void testGenerateNewDocumentationUnit_withExternalUser_shouldBeForbidden() {
    risWebTestClient
        .withExternalLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/new")
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testGenerateNewDocumentationUnit_withReferenceInParameters_shouldSetSource() {
    LegalPeriodicalDTO legalPeriodical =
        legalPeriodicalRepository.save(
            LegalPeriodicalDTO.builder()
                .jurisId(1)
                .abbreviation("ABC")
                .primaryReference(true)
                .title("Longer title")
                .build());
    when(documentNumberPatternConfig.getDocumentNumberPatterns())
        .thenReturn(Map.of("DS", "ZZREYYYY*****"));

    DocumentationUnitCreationParameters parameters =
        DocumentationUnitCreationParameters.builder()
            .reference(
                Reference.builder()
                    .id(UUID.randomUUID())
                    .referenceType(ReferenceType.CASELAW)
                    .legalPeriodical(LegalPeriodicalTransformer.transformToDomain(legalPeriodical))
                    .legalPeriodicalRawValue(legalPeriodical.getAbbreviation())
                    .citation("test")
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/new")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(parameters)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(Decision.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody())
                    .extracting("coreData.source.value")
                    .isEqualTo(SourceValue.Z));
  }

  @Test
  void testGenerateNewDocumentationUnit_isExternalHandover_shouldWriteHistoryLog() {
    DocumentationOfficeDTO creatingDocumentationOfficeDTO =
        documentationOfficeRepository.findByAbbreviation("BGH");

    DocumentationOffice creatingDocumentationOffice =
        DocumentationOfficeTransformer.transformToDomain(creatingDocumentationOfficeDTO);
    LegalPeriodicalDTO legalPeriodical =
        legalPeriodicalRepository.save(
            LegalPeriodicalDTO.builder()
                .jurisId(2)
                .abbreviation("ABC")
                .primaryReference(true)
                .title("Longer title")
                .build());
    when(documentNumberPatternConfig.getDocumentNumberPatterns())
        .thenReturn(Map.of("BGH", "ZZREYYYY*****"));

    DocumentationUnitCreationParameters parameters =
        DocumentationUnitCreationParameters.builder()
            .documentationOffice(creatingDocumentationOffice)
            .reference(
                Reference.builder()
                    .id(UUID.randomUUID())
                    .referenceType(ReferenceType.CASELAW)
                    .legalPeriodical(LegalPeriodicalTransformer.transformToDomain(legalPeriodical))
                    .legalPeriodicalRawValue(legalPeriodical.getAbbreviation())
                    .citation("test")
                    .build())
            .build();

    Decision createdDocUnit =
        risWebTestClient
            .withDefaultLogin()
            .put()
            .uri("/api/v1/caselaw/documentunits/new")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(parameters)
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(Decision.class)
            .returnResult()
            .getResponseBody();

    User user = User.builder().documentationOffice(creatingDocumentationOffice).build();
    var logs = historyLogService.getHistoryLogs(createdDocUnit.uuid(), user);
    assertThat(logs).hasSize(2);
    assertThat(logs)
        .map(HistoryLog::eventType)
        .containsExactly(HistoryLogEventType.EXTERNAL_HANDOVER, HistoryLogEventType.CREATE);
    // As DS User I am not able to see the user from BGH docoffice, who created the docunit
    assertThat(logs).map(HistoryLog::createdBy).containsExactly(null, null);
    assertThat(logs.getFirst().description()).isEqualTo("Fremdanalage angelegt für BGH");
  }

  @Test
  void testTakeOverDocumentationUnit_setsStatusAndPermissionsCorrectlyAndCreatesHistoryLog() {
    DocumentationOfficeDTO creatingDocumentationOffice =
        documentationOfficeRepository.findByAbbreviation("BGH");
    String documentNumber = "1234567890123";

    var ds = documentationOfficeRepository.findByAbbreviation("DS");

    var pendingDocUnit =
        EntityBuilderTestUtil.createAndSavePendingDocumentationUnit(
            repository, documentationOffice, creatingDocumentationOffice, documentNumber);

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/1234567890123/takeover")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentationUnitListItem.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().status().publicationStatus())
                  .isEqualTo(UNPUBLISHED);
              assertThat(response.getResponseBody().isDeletable()).isTrue();
              assertThat(response.getResponseBody().isEditable()).isTrue();
            });

    var historyLogs =
        historyLogRepository.findByDocumentationUnitId(
            pendingDocUnit.getId(),
            User.builder()
                .documentationOffice(DocumentationOfficeTransformer.transformToDomain(ds))
                .build());
    assertThat(historyLogs).hasSize(1);
    assertThat(historyLogs.getFirst().createdAt())
        .isBetween(Instant.now().minusSeconds(10), Instant.now());
    assertThat(historyLogs.getFirst().documentationOffice()).isEqualTo("DS");
    assertThat(historyLogs.getFirst().createdBy()).isEqualTo("testUser");
    assertThat(historyLogs.getFirst().description())
        .isEqualTo("Status geändert: Fremdanlage → Unveröffentlicht");
    assertThat(historyLogs.getFirst().eventType()).isEqualTo(HistoryLogEventType.STATUS);
  }

  @Nested
  class BulkAssignProcedure {
    @Transactional
    @Test
    void shouldAssignNewProcedureToMultipleDocUnitsWithInboxStatus() {
      var decisionBuilder1 =
          DecisionDTO.builder()
              .documentNumber("DOCNUMBER_001")
              .documentationOffice(documentationOffice)
              .inboxStatus(InboxStatus.EXTERNAL_HANDOVER);
      var docUnit1 =
          EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
              repository, decisionBuilder1);

      var decisionBuilder2 =
          DecisionDTO.builder()
              .documentNumber("DOCNUMBER_002")
              .documentationOffice(documentationOffice)
              .inboxStatus(InboxStatus.EU);
      var docUnit2 =
          EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
              repository, decisionBuilder2);
      var docUnitIds = List.of(docUnit1.getId(), docUnit2.getId());
      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/bulk-assign-procedure")
          .bodyValue(new BulkAssignProcedureRequest("new_procedure", docUnitIds))
          .exchange()
          .expectStatus()
          .isOk();

      var updatedDocUnits = repository.findAll();
      assertThat(updatedDocUnits)
          .hasSize(2)
          .map(DocumentationUnitDTO::getProcedure)
          .map(ProcedureDTO::getLabel)
          .containsExactlyInAnyOrder("new_procedure", "new_procedure");

      assertThat(updatedDocUnits)
          .map(DocumentationUnitDTO::getInboxStatus)
          .containsExactly(null, null);
    }

    @Transactional
    @Test
    void shouldAssignExistingProcedureToSingleDocUnitWithoutInboxStatus() {
      var procedure =
          ProcedureDTO.builder()
              .documentationOffice(documentationOffice)
              .label("existing_procedure")
              .build();
      procedureRepository.save(procedure);
      var decisionBuilder1 =
          DecisionDTO.builder()
              .documentNumber("DOCNUMBER_001")
              .documentationOffice(documentationOffice)
              .inboxStatus(null);
      var docUnit1 =
          EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
              repository, decisionBuilder1);

      var docUnitIds = List.of(docUnit1.getId());
      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/bulk-assign-procedure")
          .bodyValue(new BulkAssignProcedureRequest("existing_procedure", docUnitIds))
          .exchange()
          .expectStatus()
          .isOk();

      var updatedDocUnits = repository.findAll();
      assertThat(updatedDocUnits).hasSize(1);

      assertThat(updatedDocUnits.getFirst().getProcedure().getLabel())
          .isEqualTo("existing_procedure");

      assertThat(updatedDocUnits.getFirst().getInboxStatus()).isNull();
    }

    @Transactional
    @Test
    void shouldRollbackIfOneUpdateFails() {
      TestTransaction.end();
      ProcedureDTO procedure =
          ProcedureDTO.builder()
              .documentationOffice(documentationOffice)
              .label("old_procedure")
              .build();
      procedureRepository.save(procedure);
      var decisionBuilder1 =
          DecisionDTO.builder()
              .documentNumber("DOCNUMBER_001")
              .documentationOffice(documentationOffice)
              .procedure(procedure)
              .inboxStatus(InboxStatus.EXTERNAL_HANDOVER);
      var docUnit1 =
          EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
              repository, decisionBuilder1);

      var pendingProceedingBuilder2 =
          PendingProceedingDTO.builder()
              .documentNumber("DOCNUMBER_002")
              .documentationOffice(documentationOffice)
              .inboxStatus(InboxStatus.EU);
      var pendingProceeding2 = repository.save(pendingProceedingBuilder2.build());

      var decisionBuilder3 =
          DecisionDTO.builder()
              .documentNumber("DOCNUMBER_003")
              .documentationOffice(documentationOffice)
              .inboxStatus(InboxStatus.EU);
      var docUnit3 =
          EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
              repository, decisionBuilder3);
      var docUnitIds = List.of(docUnit1.getId(), pendingProceeding2.getId(), docUnit3.getId());
      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/bulk-assign-procedure")
          .bodyValue(new BulkAssignProcedureRequest("new_procedure", docUnitIds))
          .exchange()
          .expectStatus()
          .isBadRequest();

      TestTransaction.start();
      var updatedDocUnits = repository.findAll();
      assertThat(updatedDocUnits)
          .hasSize(3)
          .map(DocumentationUnitDTO::getProcedure)
          .map(Optional::ofNullable)
          .map(p -> p.map(ProcedureDTO::getLabel).orElse(null))
          .containsExactlyInAnyOrder("old_procedure", null, null);

      assertThat(updatedDocUnits)
          .map(DocumentationUnitDTO::getInboxStatus)
          .containsExactlyInAnyOrder(InboxStatus.EXTERNAL_HANDOVER, InboxStatus.EU, InboxStatus.EU);
      TestTransaction.end();
    }

    @Test
    void shouldRejectRequestForDocUnitFromOtherDocOffice() {
      var bghDocOffice = documentationOfficeRepository.findByAbbreviation("BGH");

      var decisionBuilder1 =
          DecisionDTO.builder()
              .documentNumber("DOCNUMBER_001")
              .documentationOffice(bghDocOffice)
              .inboxStatus(InboxStatus.EXTERNAL_HANDOVER);
      var docUnit1 =
          EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
              repository, decisionBuilder1);

      var docUnitIds = List.of(docUnit1.getId());
      risWebTestClient
          .withDefaultLogin()
          .patch()
          .uri("/api/v1/caselaw/documentunits/bulk-assign-procedure")
          .bodyValue(new BulkAssignProcedureRequest("new_procedure", docUnitIds))
          .exchange()
          .expectStatus()
          .isForbidden();
    }
  }

  @Test
  void assignDocumentationOffice_withoutRights_shouldBeForbidden() {
    // Arrange
    var bghDocOffice = documentationOfficeRepository.findByAbbreviation("BGH");
    var decisionBuilder =
        DecisionDTO.builder()
            .documentNumber("DOCNUMBER_001")
            .documentationOffice(documentationOffice);
    var documentationUnit =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(repository, decisionBuilder);

    // Act
    risWebTestClient
        .withExternalLogin()
        .put()
        .uri(
            "/api/v1/caselaw/documentunits/"
                + documentationUnit.getId()
                + "/assign/"
                + bghDocOffice.getId())
        .exchange()
        .expectStatus()
        .isForbidden()
        .expectBody(String.class)
        .consumeWith(
            response -> {
              // Assert
              var documentationUnitDTO = repository.findById(documentationUnit.getId());
              assertThat(documentationUnitDTO.get().getDocumentationOffice())
                  .isEqualTo(documentationOffice);
            });
  }

  @Test
  void assignDocumentationOffice_withoutProcedures_shouldSucceedAndLogChanges() {
    // Arrange
    var bghDocOffice = documentationOfficeRepository.findByAbbreviation("BGH");
    var decisionBuilder =
        DecisionDTO.builder()
            .documentNumber("DOCNUMBER_001")
            .documentationOffice(documentationOffice);
    var documentationUnit =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(repository, decisionBuilder);

    // Act
    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri(
            "/api/v1/caselaw/documentunits/"
                + documentationUnit.getId()
                + "/assign/"
                + bghDocOffice.getId())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .consumeWith(
            response -> {
              // Assert
              assertThat(response.getResponseBody())
                  .isEqualTo("The documentation office [BGH] has been successfully assigned.");
              var documentationUnitDTO = repository.findById(documentationUnit.getId());
              assertThat(documentationUnitDTO.get().getDocumentationOffice().getId())
                  .isEqualTo(bghDocOffice.getId());
              var historyLogs =
                  historyLogRepository.findByDocumentationUnitId(
                      documentationUnit.getId(),
                      User.builder()
                          .documentationOffice(
                              DocumentationOfficeTransformer.transformToDomain(
                                  documentationUnit.getDocumentationOffice()))
                          .build());
              assertThat(historyLogs).hasSize(1);
              assertThat(historyLogs.get(0).eventType())
                  .isEqualTo(HistoryLogEventType.DOCUMENTATION_OFFICE);
              assertThat(historyLogs.get(0).description())
                  .isEqualTo("Dokstelle geändert: [DS] → [BGH]");
              assertThat(
                      documentationUnitDTO
                          .get()
                          .getManagementData()
                          .getLastUpdatedByDocumentationOffice()
                          .getId())
                  .isEqualTo(documentationUnit.getDocumentationOffice().getId());
              assertThat(
                      documentationUnitDTO.get().getManagementData().getLastUpdatedBySystemName())
                  .isNull();
              assertThat(documentationUnitDTO.get().getManagementData().getLastUpdatedAtDateTime())
                  .isBetween(Instant.now().minusSeconds(10), Instant.now());
              assertThat(documentationUnitDTO.get().getManagementData().getLastUpdatedByUserName())
                  .isEqualTo("testUser");
              assertThat(documentationUnitDTO.get().getManagementData().getLastUpdatedByUserId())
                  .isNotNull();
              assertThat(documentationUnitDTO.get().getInboxStatus())
                  .isEqualTo(InboxStatus.EXTERNAL_HANDOVER);
            });
  }

  @Transactional
  @Test
  void assignDocumentationOffice_withProcedures_shouldSucceedAndRemoveProcedures() {
    // Arrange
    var bghDocOffice = documentationOfficeRepository.findByAbbreviation("BGH");
    var decisionId = createDecisionWithProcedures();

    // Act
    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + decisionId + "/assign/" + bghDocOffice.getId())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .consumeWith(
            response -> {
              // Assert
              assertThat(response.getResponseBody())
                  .isEqualTo("The documentation office [BGH] has been successfully assigned.");
              var docUnitWithoutProcedure = repository.findById(decisionId);
              assertThat(docUnitWithoutProcedure.get().getProcedure()).isNull();
              assertThat(docUnitWithoutProcedure.get().getProcedureHistory()).isEmpty();
              assertThat(docUnitWithoutProcedure.get().getDocumentationOffice().getId())
                  .isEqualTo(bghDocOffice.getId());
            });
  }

  private UUID createDecisionWithProcedures() {
    List<ProcedureDTO> procedures =
        List.of(
            ProcedureDTO.builder()
                .documentationOffice(documentationOffice)
                .label("vorgang1")
                .build(),
            ProcedureDTO.builder()
                .documentationOffice(documentationOffice)
                .label("vorgang2")
                .build());
    procedureRepository.saveAll(procedures);
    String documentNumber =
        new Random().ints(13, 0, 10).mapToObj(Integer::toString).collect(Collectors.joining());
    var decision =
        repository.save(
            DecisionDTO.builder()
                .documentNumber(documentNumber)
                .procedure(procedures.get(0))
                .procedureHistory(new ArrayList<>(List.of(procedures.get(1))))
                .documentationOffice(documentationOffice)
                .build());
    assertThat(procedureRepository.count()).isEqualTo(2);
    var docUnitWithProcedure = repository.findById(decision.getId());
    assertThat(docUnitWithProcedure.get().getProcedure().getLabel()).isEqualTo("vorgang1");
    assertThat(docUnitWithProcedure.get().getProcedureHistory().get(0).getLabel())
        .isEqualTo("vorgang2");
    return decision.getId();
  }

  @Test
  void test_getDocumentationUnitImage_shouldSucceed() {
    DocumentationUnitDTO dto =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
            repository, documentationOffice, "TEST123456789");

    when(attachmentService.findByDocumentationUnitIdAndFileName(dto.getId(), "image.png"))
        .thenReturn(Optional.of(new Image(new byte[] {1, 2, 3}, "png", "image.png")));

    byte[] imageBytes =
        risWebTestClient
            .withDefaultLogin()
            .get()
            .uri("/api/v1/caselaw/documentunits/TEST123456789/image/image.png")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(byte[].class)
            .returnResult()
            .getResponseBody();
    assertThat(imageBytes).isEqualTo(new byte[] {1, 2, 3});
  }
}
