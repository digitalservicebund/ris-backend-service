package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDSDocOffice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.EntityBuilderTestUtil;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberGeneratorService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberRecyclingService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentationUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentNumberPatternConfig;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentationUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.DocxConverterService;
import de.bund.digitalservice.ris.caselaw.adapter.FmxService;
import de.bund.digitalservice.ris.caselaw.adapter.OAuthService;
import de.bund.digitalservice.ris.caselaw.adapter.PortalPublicationJobService;
import de.bund.digitalservice.ris.caselaw.adapter.PrototypePortalBucket;
import de.bund.digitalservice.ris.caselaw.adapter.PrototypePortalPublicationService;
import de.bund.digitalservice.ris.caselaw.adapter.RiiService;
import de.bund.digitalservice.ris.caselaw.adapter.XmlUtilService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseCourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionNameDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalEffectDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PortalPublicationJobDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PortalPublicationJobRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresCourtRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentTypeRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitHistoryLogRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitSearchRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.eurlex.EurLexSOAPSearchService;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.PublicPortalTransformer;
import de.bund.digitalservice.ris.caselaw.config.ConverterConfig;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentService;
import de.bund.digitalservice.ris.caselaw.domain.ConverterService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitDocxMetadataInitializationService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitHistoryLogService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateCheckService;
import de.bund.digitalservice.ris.caselaw.domain.FeatureToggleService;
import de.bund.digitalservice.ris.caselaw.domain.HandoverService;
import de.bund.digitalservice.ris.caselaw.domain.PortalPublicationTaskStatus;
import de.bund.digitalservice.ris.caselaw.domain.PortalPublicationTaskType;
import de.bund.digitalservice.ris.caselaw.domain.ProcedureService;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.domain.mapper.PatchMapperService;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@RISIntegrationTest(
    imports = {
      PrototypePortalPublicationService.class,
      PublicPortalTransformer.class,
      XmlUtilService.class,
      ConverterConfig.class,
      PrototypePortalBucket.class,
      DocumentationUnitService.class,
      DatabaseDocumentNumberGeneratorService.class,
      DatabaseDocumentNumberRecyclingService.class,
      DatabaseDocumentationUnitStatusService.class,
      PostgresDocumentationUnitRepositoryImpl.class,
      PostgresCourtRepositoryImpl.class,
      PostgresDocumentTypeRepositoryImpl.class,
      PostgresJPAConfig.class,
      FlywayConfig.class,
      SecurityConfig.class,
      OAuthService.class,
      TestConfig.class,
      PortalPublicationJobService.class,
      DocumentNumberPatternConfig.class,
      RiiService.class,
      PostgresDocumentationUnitHistoryLogRepositoryImpl.class,
      DocumentationUnitHistoryLogService.class,
      PostgresDocumentationUnitSearchRepositoryImpl.class
    },
    controllers = {DocumentationUnitController.class})
class PrototypePortalPublicationJobIntegrationTest {
  @Container
  static PostgreSQLContainer<?> postgreSQLContainer =
      new PostgreSQLContainer<>("postgres:14").withInitScript("init_db.sql");

  @Autowired private PortalPublicationJobRepository portalPublicationJobRepository;

  @DynamicPropertySource
  static void registerDynamicProperties(DynamicPropertyRegistry registry) {
    registry.add("database.user", () -> postgreSQLContainer.getUsername());
    registry.add("database.password", () -> postgreSQLContainer.getPassword());
    registry.add("database.host", () -> postgreSQLContainer.getHost());
    registry.add("database.port", () -> postgreSQLContainer.getFirstMappedPort());
    registry.add("database.database", () -> postgreSQLContainer.getDatabaseName());
  }

  @Autowired private RisWebTestClient risWebTestClient;
  @Autowired private DatabaseDocumentationUnitRepository repository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @Autowired private DatabaseCourtRepository databaseCourtRepository;
  @Autowired private DatabaseDocumentTypeRepository databaseDocumentTypeRepository;
  @Autowired private PortalPublicationJobRepository publicationJobRepository;
  @Autowired private PortalPublicationJobService portalPublicationJobService;

  @MockitoBean(name = "prototypePortalS3Client")
  private S3Client s3Client;

  @MockitoBean private UserService userService;
  @MockitoBean private DocxConverterService docxConverterService;
  @MockitoBean private ClientRegistrationRepository clientRegistrationRepository;
  @MockitoBean private AttachmentService attachmentService;
  @MockitoBean private PatchMapperService patchMapperService;
  @MockitoBean private HandoverService handoverService;
  @MockitoBean private ProcedureService procedureService;
  @MockitoBean private DuplicateCheckService duplicateCheckService;
  @MockitoBean private FmxService fmxService;
  @MockitoBean private ConverterService converterService;
  @MockitoBean private EurLexSOAPSearchService eurLexSOAPSearchService;
  @MockitoBean private FeatureToggleService featureToggleService;

  @MockitoBean
  private DocumentationUnitDocxMetadataInitializationService
      documentationUnitDocxMetadataInitializationService;

  private final DocumentationOffice docOffice = buildDSDocOffice();
  private DocumentationOfficeDTO documentationOffice;

  @BeforeEach
  void setUp() {
    documentationOffice =
        documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation());

    doReturn(docOffice)
        .when(userService)
        .getDocumentationOffice(
            argThat(
                (OidcUser user) -> {
                  List<String> groups = user.getAttribute("groups");
                  return Objects.requireNonNull(groups).get(0).equals("/DS");
                }));

    portalPublicationJobRepository.deleteAll();
  }

  @AfterEach
  void cleanUp() {
    repository.deleteAll();
  }

  @Test
  void shouldPublishWithOnlyAllowedPrototypeData() throws IOException {
    DocumentationUnitDTO dto =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
            repository, buildValidDocumentationUnit("1"));
    ArgumentCaptor<PutObjectRequest> putCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
    ArgumentCaptor<RequestBody> bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);

    portalPublicationJobRepository.saveAll(
        List.of(createPublicationJob(dto, PortalPublicationTaskType.PUBLISH)));

    portalPublicationJobService.executePendingJobs();

    verify(s3Client, times(1)).putObject(putCaptor.capture(), bodyCaptor.capture());

    var putRequest = putCaptor.getValue();
    var ldmlContent =
        new String(
            bodyCaptor.getValue().contentStreamProvider().newStream().readAllBytes(),
            StandardCharsets.UTF_8);

    assertThat(putRequest.key()).contains(dto.getDocumentNumber());
    assertThat(ldmlContent)
        .contains("gruende test")
        .doesNotContain("entscheidungsname test")
        .doesNotContain("orientierungssatz test");
  }

  @Test
  void shouldContinueExecutionOnError() {
    DocumentationUnitDTO dto =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
            repository, buildValidDocumentationUnit("1"));
    DocumentationUnitDTO dto2 =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
            repository, buildValidDocumentationUnit("2"));

    // PUBLISH job and upload changelog will fail
    when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
        .thenThrow(new RuntimeException("error"));

    portalPublicationJobRepository.saveAll(
        List.of(
            createPublicationJob(dto, PortalPublicationTaskType.PUBLISH),
            createPublicationJob(dto2, PortalPublicationTaskType.DELETE)));

    portalPublicationJobService.executePendingJobs();

    // DELETE is called even after fail
    verify(s3Client, times(1)).deleteObject(any(Consumer.class));
    // PUT 1.xml (fails) ((+ PUT changelog)) //currently disabled
    verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));

    assertThat(
            portalPublicationJobRepository.findAll().stream()
                .map(PortalPublicationJobDTO::getPublicationStatus)
                .toList())
        .isEqualTo(List.of(PortalPublicationTaskStatus.ERROR, PortalPublicationTaskStatus.SUCCESS));
  }

  @Test
  // In Migration there is no filter for "pure"-deletion jobs (Rsp_Loeschungen.csv), hence we will
  // receive deletion jobs for non-existing documents -> we ignore them by marking them as success
  void executePendingJobs_withFailedDeletionJob_shouldMarkJobAsSuccess() {
    DocumentationUnitDTO dto =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
            repository, buildValidDocumentationUnit("1"));

    // DELETE job will fail as the file is missing
    doThrow(NoSuchKeyException.class).when(s3Client).deleteObject(any(Consumer.class));

    portalPublicationJobRepository.saveAll(
        List.of(createPublicationJob(dto, PortalPublicationTaskType.DELETE)));

    portalPublicationJobService.executePendingJobs();

    assertThat(
            portalPublicationJobRepository.findAll().stream()
                .map(PortalPublicationJobDTO::getPublicationStatus)
                .toList())
        .hasSize(1)
        .isEqualTo(List.of(PortalPublicationTaskStatus.SUCCESS));
  }

  @Test
  void publishNightlyChangelog_shouldUploadChangelogWithChangeAllTrue() throws IOException {
    ArgumentCaptor<PutObjectRequest> putCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
    ArgumentCaptor<RequestBody> bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);

    portalPublicationJobService.publishNightlyChangelog();

    verify(s3Client).putObject(putCaptor.capture(), bodyCaptor.capture());

    var putRequest = putCaptor.getValue();
    var changelogContent =
        new String(
            bodyCaptor.getValue().contentStreamProvider().newStream().readAllBytes(),
            StandardCharsets.UTF_8);

    assertThat(putRequest.key()).contains("changelogs/");
    assertThat(changelogContent)
        .isEqualTo(
            """
                {"change_all":true}""");
  }

  private PortalPublicationJobDTO createPublicationJob(
      DocumentationUnitDTO dto, PortalPublicationTaskType publicationType) {
    return PortalPublicationJobDTO.builder()
        .documentNumber(dto.getDocumentNumber())
        .createdAt(Instant.now())
        .publicationStatus(PortalPublicationTaskStatus.PENDING)
        .publicationType(publicationType)
        .build();
  }

  private DecisionDTO.DecisionDTOBuilder<?, ?> buildValidDocumentationUnit(String docNumber) {
    CourtDTO court =
        databaseCourtRepository.saveAndFlush(
            CourtDTO.builder()
                .type("AG")
                .location("Aachen")
                .isSuperiorCourt(false)
                .isForeignCourt(false)
                .jurisId(new Random().nextInt())
                .build());

    var docType =
        databaseDocumentTypeRepository.saveAndFlush(
            DocumentTypeDTO.builder().abbreviation("test").multiple(true).build());

    return DecisionDTO.builder()
        .documentNumber(docNumber)
        .documentType(docType)
        .documentationOffice(documentationOffice)
        .court(court)
        .date(LocalDate.now())
        .legalEffect(LegalEffectDTO.JA)
        .fileNumbers(List.of(FileNumberDTO.builder().value("123").rank(0L).build()))
        .grounds("gruende test")
        .headnote("orientierungssatz test")
        .decisionNames(List.of(DecisionNameDTO.builder().value("entscheidungsname test").build()));
  }
}
