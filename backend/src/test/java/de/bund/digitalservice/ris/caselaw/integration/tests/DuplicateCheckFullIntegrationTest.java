package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDSDocOffice;
import static de.bund.digitalservice.ris.caselaw.AuthUtils.mockUserGroups;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberGeneratorService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberRecyclingService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentationUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDuplicateCheckService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseProcedureService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentNumberPatternConfig;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentationUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.DocxConverterService;
import de.bund.digitalservice.ris.caselaw.adapter.DuplicateRelationService;
import de.bund.digitalservice.ris.caselaw.adapter.FmxService;
import de.bund.digitalservice.ris.caselaw.adapter.KeycloakUserService;
import de.bund.digitalservice.ris.caselaw.adapter.OAuthService;
import de.bund.digitalservice.ris.caselaw.adapter.StagingPortalPublicationService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseCourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDeletedDocumentationIdsRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentCategoryRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentNumberRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDuplicateCheckRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseFileNumberRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseRegionRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingCourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingDateDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingFileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DuplicateRelationRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PendingProceedingDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDeltaMigrationRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitHistoryLogRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitSearchRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresHandoverReportRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import de.bund.digitalservice.ris.caselaw.adapter.eurlex.EurLexSOAPSearchService;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.CourtTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DecisionTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentTypeTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationOfficeTransformer;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentService;
import de.bund.digitalservice.ris.caselaw.domain.AuthService;
import de.bund.digitalservice.ris.caselaw.domain.ConverterService;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitDocxMetadataInitializationService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitHistoryLogService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateRelation;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateRelationStatus;
import de.bund.digitalservice.ris.caselaw.domain.FeatureToggleService;
import de.bund.digitalservice.ris.caselaw.domain.HandoverService;
import de.bund.digitalservice.ris.caselaw.domain.MailService;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.UserGroupService;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitException;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.domain.mapper.PatchMapperService;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.Builder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import software.amazon.awssdk.services.s3.S3AsyncClient;

/**
 * This is a temporary solution, as we have two different methods for the DuplicateCheck, see
 * DuplicateCheckIntegrationTest. We will look into unifying the two concepts and potentially
 * testing them together.
 */
@RISIntegrationTest(
    imports = {
      DocumentationUnitService.class,
      DuplicateRelationService.class,
      PostgresDeltaMigrationRepositoryImpl.class,
      DatabaseDocumentNumberGeneratorService.class,
      DatabaseDuplicateCheckService.class,
      DatabaseDocumentNumberRecyclingService.class,
      DatabaseDocumentationUnitStatusService.class,
      DatabaseProcedureService.class,
      PostgresHandoverReportRepositoryImpl.class,
      PostgresDocumentationUnitRepositoryImpl.class,
      PostgresJPAConfig.class,
      FlywayConfig.class,
      SecurityConfig.class,
      OAuthService.class,
      TestConfig.class,
      DocumentNumberPatternConfig.class,
      KeycloakUserService.class,
      PostgresDocumentationUnitHistoryLogRepositoryImpl.class,
      DocumentationUnitHistoryLogService.class,
      PostgresDocumentationUnitSearchRepositoryImpl.class
    },
    controllers = {DocumentationUnitController.class})
@Sql(scripts = {"classpath:courts_init.sql", "classpath:document_types.sql"})
@Sql(
    scripts = {"classpath:courts_cleanup.sql"},
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class DuplicateCheckFullIntegrationTest {
  @Container
  static PostgreSQLContainer<?> postgreSQLContainer =
      new PostgreSQLContainer<>("postgres:14").withInitScript("init_db.sql");

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
  @Autowired private DocumentationUnitRepository documentationUnitRepository;
  @Autowired private DatabaseFileNumberRepository fileNumberRepository;
  @Autowired private DatabaseDocumentTypeRepository databaseDocumentTypeRepository;
  @Autowired private DatabaseDocumentCategoryRepository databaseDocumentCategoryRepository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @Autowired private DatabaseCourtRepository databaseCourtRepository;
  @Autowired private DatabaseDocumentationUnitRepository databaseDocumentationUnitRepository;
  @Autowired private DatabaseRegionRepository regionRepository;
  @Autowired private DatabaseDeletedDocumentationIdsRepository deletedDocumentationIdsRepository;
  @Autowired private DatabaseDuplicateCheckRepository duplicateCheckRepository;
  @Autowired private DuplicateRelationRepository duplicateRelationRepository;
  @Autowired private AuthService authService;
  @Autowired private DocumentationUnitService documentationUnitService;
  @Autowired private DatabaseDuplicateCheckService duplicateCheckService;

  @MockitoBean private S3AsyncClient s3AsyncClient;
  @MockitoBean private MailService mailService;
  @MockitoBean private DocxConverterService docxConverterService;
  @MockitoBean private UserGroupService userGroupService;
  @MockitoBean private ClientRegistrationRepository clientRegistrationRepository;
  @MockitoBean private AttachmentService attachmentService;
  @MockitoBean private PatchMapperService patchMapperService;
  @MockitoBean private HandoverService handoverService;
  @MockitoBean private StagingPortalPublicationService stagingPortalPublicationService;
  @MockitoBean private DatabaseDocumentNumberRepository databaseDocumentNumberRepository;
  @MockitoBean private FmxService fmxService;
  @MockitoBean private ConverterService converterService;
  @MockitoBean private EurLexSOAPSearchService eurLexSOAPSearchService;
  @MockitoBean private FeatureToggleService featureToggleService;

  @MockitoBean
  private DocumentationUnitDocxMetadataInitializationService
      documentationUnitDocxMetadataInitializationService;

  private DocumentationOffice docOffice;
  private DocumentationOfficeDTO documentationOffice;
  private static final DocumentType documentType1 =
      DocumentType.builder().uuid(UUID.fromString("0f382996-497f-4c2f-9a30-1c73d8ac0a87")).build();
  private static final DocumentType documentType2 =
      DocumentType.builder().uuid(UUID.fromString("11defe05-cd4d-43e5-a07e-06c611b81a26")).build();
  private static final Court courtAgAachen =
      Court.builder().id(UUID.fromString("46301f85-9bd2-4690-a67f-f9fdfe725de3")).build();
  private static final Court courtOlgDresden =
      Court.builder().id(UUID.fromString("12e9f671-6a5c-4ec7-9b57-3fafdefd7a49")).build();

  @BeforeEach
  void setUp() {
    documentationOffice =
        documentationOfficeRepository.findByAbbreviation(buildDSDocOffice().abbreviation());

    docOffice = DocumentationOfficeTransformer.transformToDomain(documentationOffice);

    mockUserGroups(userGroupService);
  }

  @AfterEach
  void cleanUp() {
    fileNumberRepository.deleteAll();
    repository.deleteAll();
    databaseDocumentTypeRepository.deleteAll();
    duplicateRelationRepository.deleteAll();
  }

  @Nested
  class DuplicateCheckServiceTest {
    @Test
    void checkDuplicates_withoutFileNumbersOrEclis_shouldDoNothing()
        throws DocumentationUnitNotExistsException {
      // Arrange
      var docUnitWithoutFileNumbers =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb1")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .build()));

      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Act
      duplicateCheckService.checkAllDuplicates();

      var foundDocUnit = documentationUnitService.getByUuid(docUnitWithoutFileNumbers.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).isEmpty();
      assertThat(((DocumentationUnit) foundDocUnit).managementData().duplicateRelations())
          .isEmpty();
    }

    @Test
    void checkDuplicates_withEmptyDocUnit_shouldDoNothing()
        throws DocumentationUnitNotExistsException {
      // Arrange
      var docUnitWithoutFileNumbers =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(CreationParameters.builder().documentNumber("DocumentNumb1").build()));

      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Act
      duplicateCheckService.checkAllDuplicates();

      var foundDocUnit = documentationUnitService.getByUuid(docUnitWithoutFileNumbers.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).isEmpty();
      assertThat(((DocumentationUnit) foundDocUnit).managementData().duplicateRelations())
          .isEmpty();
    }

    @Test
    void checkDuplicates_withoutFoundDuplicates_shouldDoNothing()
        throws DocumentationUnitNotExistsException {
      // Arrange
      var docUnitToBeChecked =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb1")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .fileNumbers(List.of("AZ-123"))
                      .build()));

      // create non duplicate docUnit
      generateNewDocumentationUnit(
          docOffice,
          Optional.of(
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .fileNumbers(List.of("AZ-Different"))
                  .build()));

      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Act
      duplicateCheckService.checkAllDuplicates();

      var initialDocUnit = documentationUnitService.getByUuid(docUnitToBeChecked.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).isEmpty();
      assertThat(((DocumentationUnit) initialDocUnit).managementData().duplicateRelations())
          .isEmpty();
    }

    @Test
    void checkDuplicates_withoutExistingDuplicates_shouldCreateNewDuplicateWithPendingStatus()
        throws DocumentationUnitNotExistsException {
      // Arrange
      var docUnitToBeChecked =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb1")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .documentType(documentType1)
                      .fileNumbers(List.of("AZ-123"))
                      .build()));

      var docUnitDuplicateDTO =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb2")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .documentType(documentType1)
                      .fileNumbers(List.of("AZ-123"))
                      .build()));

      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Act
      duplicateCheckService.checkAllDuplicates();

      var foundDocUnit = documentationUnitService.getByUuid(docUnitToBeChecked.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).hasSize(1);
      DuplicateRelation duplicate =
          ((DocumentationUnit) foundDocUnit)
              .managementData().duplicateRelations().stream().findFirst().get();
      assertThat(duplicate.documentNumber()).isEqualTo(docUnitDuplicateDTO.getDocumentNumber());
      assertThat(duplicate.status()).isEqualTo(DuplicateRelationStatus.PENDING);
    }

    @Test
    void checkDuplicates_withMatchingDeviatingFileNumber_shouldCreateNewDuplicateWithPendingStatus()
        throws DocumentationUnitNotExistsException {
      // Arrange
      var docUnitToBeChecked =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb1")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .documentType(documentType1)
                      .deviatingFileNumbers(List.of("AZ-123-match", "AZ-888", "AZ-999"))
                      .build()));

      var docUnitDuplicateDTO =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb2")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .documentType(documentType1)
                      .fileNumbers(List.of("AZ-123-match", "AZ-777", "AZ-555"))
                      .build()));

      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Act
      duplicateCheckService.checkAllDuplicates();

      var foundDocUnit = documentationUnitService.getByUuid(docUnitToBeChecked.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).hasSize(1);
      DuplicateRelation duplicate =
          ((DocumentationUnit) foundDocUnit)
              .managementData().duplicateRelations().stream().findFirst().get();
      assertThat(duplicate.documentNumber()).isEqualTo(docUnitDuplicateDTO.getDocumentNumber());
      assertThat(duplicate.status()).isEqualTo(DuplicateRelationStatus.PENDING);
    }

    @Test
    void
        checkDuplicates_withoutExistingDuplicatesAndIsJdvDuplicateCheckActiveTrue_shouldCreateNewDuplicateWithPendingStatus()
            throws DocumentationUnitNotExistsException {
      // Arrange
      var docUnitToBeChecked =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb1")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .documentType(documentType1)
                      .fileNumbers(List.of("AZ-123"))
                      .build()));

      var docUnitDuplicateDTO =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb2")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .isJdvDuplicateCheckActive(true)
                      .documentType(documentType1)
                      .fileNumbers(List.of("AZ-123"))
                      .build()));

      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Act
      duplicateCheckService.checkAllDuplicates();

      var foundDocUnit = documentationUnitService.getByUuid(docUnitToBeChecked.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).hasSize(1);
      DuplicateRelation duplicate =
          ((DocumentationUnit) foundDocUnit)
              .managementData().duplicateRelations().stream().findFirst().get();
      assertThat(duplicate.documentNumber()).isEqualTo(docUnitDuplicateDTO.getDocumentNumber());
      assertThat(duplicate.status()).isEqualTo(DuplicateRelationStatus.PENDING);
    }

    @Test
    void
        checkDuplicates_withoutExistingDuplicatesAndIsJdvDuplicateCheckActiveTrueAndLockedStatus_shouldCreateNewDuplicateWithIgnoredStatus()
            throws DocumentationUnitNotExistsException {
      // Arrange
      var docUnitToBeChecked =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb1")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .fileNumbers(List.of("AZ-123"))
                      .documentType(documentType1)
                      .publicationStatus(PublicationStatus.LOCKED)
                      .build()));

      var docUnitDuplicateDTO =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb2")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .isJdvDuplicateCheckActive(true)
                      .documentType(documentType1)
                      .fileNumbers(List.of("AZ-123"))
                      .build()));

      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Act
      duplicateCheckService.checkDuplicates(docUnitToBeChecked.getDocumentNumber());

      var foundDocUnit =
          (DocumentationUnit) documentationUnitService.getByUuid(docUnitToBeChecked.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).hasSize(1);
      DuplicateRelation duplicate =
          foundDocUnit.managementData().duplicateRelations().stream().findFirst().get();
      assertThat(duplicate.documentNumber()).isEqualTo(docUnitDuplicateDTO.getDocumentNumber());
      assertThat(duplicate.status()).isEqualTo(DuplicateRelationStatus.IGNORED);
    }

    @Test
    void
        checkDuplicates_withoutExistingDuplicatesAndIsJdvDuplicateCheckActiveTrueAndDuplicatedStatus_shouldCreateNewDuplicateWithIgnoredStatus()
            throws DocumentationUnitNotExistsException {
      // Arrange
      var docUnitToBeChecked =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb1")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .isJdvDuplicateCheckActive(true)
                      .documentType(documentType1)
                      .fileNumbers(List.of("AZ-123"))
                      .build()));

      var docUnitDuplicateDTO =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb2")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .publicationStatus(PublicationStatus.DUPLICATED)
                      .documentType(documentType1)
                      .fileNumbers(List.of("AZ-123"))
                      .build()));

      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Act
      duplicateCheckService.checkDuplicates(docUnitToBeChecked.getDocumentNumber());

      var foundDocUnit =
          (DocumentationUnit) documentationUnitService.getByUuid(docUnitToBeChecked.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).hasSize(1);
      DuplicateRelation duplicate =
          foundDocUnit.managementData().duplicateRelations().stream().findFirst().get();
      assertThat(duplicate.documentNumber()).isEqualTo(docUnitDuplicateDTO.getDocumentNumber());
      assertThat(duplicate.status()).isEqualTo(DuplicateRelationStatus.IGNORED);
    }

    @Test
    void
        checkDuplicates_withoutExistingDuplicatesAndIsJdvDuplicateCheckActiveFalse_shouldCreateNewDuplicateWithIgnoredStatus()
            throws DocumentationUnitNotExistsException {
      // Arrange
      var docUnitToBeChecked =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb1")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .documentType(documentType1)
                      .fileNumbers(List.of("AZ-123"))
                      .build()));

      var docUnitDuplicateDTO =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb2")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .isJdvDuplicateCheckActive(false)
                      .documentType(documentType1)
                      .fileNumbers(List.of("AZ-123"))
                      .build()));

      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Act
      duplicateCheckService.checkAllDuplicates();

      var foundDocUnit = documentationUnitService.getByUuid(docUnitToBeChecked.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).hasSize(1);
      DuplicateRelation duplicate =
          ((DocumentationUnit) foundDocUnit)
              .managementData().duplicateRelations().stream().findFirst().get();
      assertThat(duplicate.documentNumber()).isEqualTo(docUnitDuplicateDTO.getDocumentNumber());
      assertThat(duplicate.status()).isEqualTo(DuplicateRelationStatus.IGNORED);
    }

    @Test
    void
        checkDuplicates_withExistingDuplicatesAndIsJdvDuplicateCheckActiveFalse_shouldUpdateStatusToIgnored()
            throws DocumentationUnitNotExistsException {
      // Arrange
      var docUnitToBeChecked =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb1")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .documentType(documentType1)
                      .fileNumbers(List.of("AZ-123"))
                      .build()));

      var duplicateDTO =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb2")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .documentType(documentType1)
                      .fileNumbers(List.of("AZ-123"))
                      .build()));

      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Create duplicate with pending status
      duplicateCheckService.checkAllDuplicates();
      assertThat(
              ((DocumentationUnit) documentationUnitService.getByUuid(docUnitToBeChecked.getId()))
                  .managementData().duplicateRelations().stream().findFirst().get().status())
          .isEqualTo(DuplicateRelationStatus.PENDING);

      // change isJdvDuplicateCheckActive from null to false
      duplicateDTO.setIsJdvDuplicateCheckActive(false);
      databaseDocumentationUnitRepository.save(duplicateDTO);

      // Act
      duplicateCheckService.checkAllDuplicates();
      var foundDocUnit = documentationUnitService.getByUuid(docUnitToBeChecked.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).hasSize(1);
      DuplicateRelation duplicate =
          ((DocumentationUnit) foundDocUnit)
              .managementData().duplicateRelations().stream().findFirst().get();
      assertThat(duplicate.documentNumber()).isEqualTo(duplicateDTO.getDocumentNumber());
      assertThat(duplicate.status()).isEqualTo(DuplicateRelationStatus.IGNORED);
    }

    @Test
    void checkDuplicates_withExistingDuplicates_shouldDeleteOutdatedDuplicate()
        throws DocumentationUnitNotExistsException {
      // Arrange
      var docUnitToBeChecked =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb1")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .documentType(documentType1)
                      .fileNumbers(List.of("AZ-123"))
                      .build()));

      generateNewDocumentationUnit(
          docOffice,
          Optional.of(
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .documentType(documentType1)
                  .fileNumbers(List.of("AZ-123"))
                  .build()));
      var duplicateDTO =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb3")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .documentType(documentType1)
                      .fileNumbers(List.of("AZ-123"))
                      .build()));
      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Create duplicates
      duplicateCheckService.checkAllDuplicates();
      assertThat(
              ((DocumentationUnit) documentationUnitService.getByUuid(docUnitToBeChecked.getId()))
                  .managementData().duplicateRelations().stream().findFirst().get().status())
          .isEqualTo(DuplicateRelationStatus.PENDING);
      assertThat(duplicateRelationRepository.findAll()).hasSize(3);

      // change decisionDate in second duplicate
      duplicateDTO.setDate(LocalDate.of(2022, 2, 22));
      databaseDocumentationUnitRepository.save(duplicateDTO);

      // Act
      duplicateCheckService.checkAllDuplicates();
      var foundDocUnit = documentationUnitService.getByUuid(docUnitToBeChecked.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).hasSize(1);
      assertThat(
              ((DocumentationUnit) foundDocUnit)
                  .managementData().duplicateRelations().stream()
                      .findFirst()
                      .get()
                      .documentNumber())
          .isEqualTo("DocumentNumb2");
    }

    @Test
    void deleteDocUnit_withDuplicateRelation_shouldDeleteDuplicateRelation()
        throws DocumentationUnitNotExistsException {
      // Arrange
      var docUnitToBeChecked =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb1")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .documentType(documentType1)
                      .fileNumbers(List.of("AZ-123"))
                      .build()));

      var duplicateDTO =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb2")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .documentType(documentType1)
                      .fileNumbers(List.of("AZ-123"))
                      .build()));
      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Create duplicates
      duplicateCheckService.checkAllDuplicates();
      assertThat(
              ((DocumentationUnit) documentationUnitService.getByUuid(docUnitToBeChecked.getId()))
                  .managementData()
                  .duplicateRelations())
          .hasSize(1);
      assertThat(duplicateRelationRepository.findAll()).hasSize(1);

      // Act
      documentationUnitService.deleteByUuid(duplicateDTO.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).isEmpty();
      assertThat(
              ((DocumentationUnit) documentationUnitService.getByUuid(docUnitToBeChecked.getId()))
                  .managementData()
                  .duplicateRelations())
          .isEmpty();
    }

    @Test
    void
        checkDuplicates_withUnpublishedDocUnitFromOtherDocOffice_shouldBeFilterOutDuplicateWarnings()
            throws DocumentationUnitNotExistsException {

      var bghDocumentationOffice = documentationOfficeRepository.findByAbbreviation("BGH");

      var bghDocOffice = DocumentationOfficeTransformer.transformToDomain(bghDocumentationOffice);

      // Arrange
      var docUnitToBeChecked =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb1")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .documentType(documentType1)
                      .fileNumbers(List.of("AZ-123"))
                      .build()));

      generateNewDocumentationUnit(
          bghDocOffice,
          Optional.of(
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .documentType(documentType1)
                  .fileNumbers(List.of("AZ-123"))
                  .publicationStatus(PublicationStatus.UNPUBLISHED)
                  .build()));

      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Create duplicates
      duplicateCheckService.checkAllDuplicates();
      // Although a duplicate relation is created ...
      assertThat(duplicateRelationRepository.findAll()).hasSize(1);
      // ... it won't be sent to the frontend
      assertThat(
              ((DocumentationUnit) documentationUnitService.getByUuid(docUnitToBeChecked.getId()))
                  .managementData()
                  .duplicateRelations())
          .isEmpty();
    }

    @Test
    void setStatus_withExistingPendingDuplicateRelation_shouldUpdateStatusToIgnored()
        throws DocumentationUnitNotExistsException {
      // Arrange
      var original =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb1")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .documentType(documentType1)
                      .fileNumbers(List.of("AZ-123"))
                      .build()));

      var duplicate =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb2")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .documentType(documentType1)
                      .fileNumbers(List.of("AZ-123"))
                      .build()));

      duplicateCheckService.checkAllDuplicates();
      assertThat(duplicateRelationRepository.findAll()).hasSize(1);
      var pendingDuplicateRelation = duplicateRelationRepository.findAll().getFirst();
      assertThat(pendingDuplicateRelation.getRelationStatus())
          .isEqualTo(DuplicateRelationStatus.PENDING);

      // Act
      duplicateCheckService.updateDuplicateStatus(
          original.getDocumentNumber(),
          duplicate.getDocumentNumber(),
          DuplicateRelationStatus.IGNORED);

      // Assert
      assertThat(
              ((DocumentationUnit) documentationUnitService.getByUuid(original.getId()))
                  .managementData().duplicateRelations().stream().findFirst().get().status())
          .isEqualTo(DuplicateRelationStatus.IGNORED);
    }

    @Test
    void setStatus_withoutExistingDuplicateRelation_shouldThrow() {
      // Arrange
      generateNewDocumentationUnit(
          docOffice,
          Optional.of(
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .fileNumbers(List.of("AZ-123"))
                  .build()));

      generateNewDocumentationUnit(
          docOffice,
          Optional.of(
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .fileNumbers(List.of("DIFFERENT"))
                  .build()));

      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Act + Assert
      assertThatThrownBy(
              () ->
                  duplicateCheckService.updateDuplicateStatus(
                      "DocumentNumb1", "DocumentNumb2", DuplicateRelationStatus.IGNORED))
          .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void
        checkDuplicates_pendingProceedingWithFullyDuplicatedAttributes_shouldProduceNoDuplicateRelations()
            throws DocumentationUnitNotExistsException {
      // Arrange
      var dto1 =
          repository.save(
              PendingProceedingDTO.builder()
                  .documentationOffice(documentationOffice)
                  .date(LocalDate.of(2023, 12, 11))
                  .deviatingDates(
                      List.of(
                          DeviatingDateDTO.builder()
                              .rank(0L)
                              .value(LocalDate.of(2023, 12, 11))
                              .build()))
                  .documentNumber("DocumentNumb1")
                  .court(CourtTransformer.transformToDTO(courtAgAachen))
                  .deviatingCourts(
                      List.of(DeviatingCourtDTO.builder().rank(0L).value("AG Aachen").build()))
                  .documentType(DocumentTypeTransformer.transformToDTO(documentType1))
                  .build());
      var pendingProceedingDTO1 =
          dto1.toBuilder()
              .fileNumbers(
                  List.of(
                      FileNumberDTO.builder()
                          .documentationUnit(dto1)
                          .rank(0L)
                          .value("123")
                          .build()))
              .deviatingFileNumbers(
                  List.of(
                      DeviatingFileNumberDTO.builder()
                          .documentationUnit(dto1)
                          .rank(0L)
                          .value("123")
                          .build()))
              .build();
      var dto2 =
          repository.save(
              PendingProceedingDTO.builder()
                  .documentationOffice(documentationOffice)
                  .documentationOffice(documentationOffice)
                  .date(LocalDate.of(2023, 12, 11))
                  .deviatingDates(
                      List.of(
                          DeviatingDateDTO.builder()
                              .rank(0L)
                              .value(LocalDate.of(2023, 12, 11))
                              .build()))
                  .documentNumber("DocumentNumb2")
                  .court(CourtTransformer.transformToDTO(courtAgAachen))
                  .deviatingCourts(
                      List.of(DeviatingCourtDTO.builder().rank(0L).value("AG Aachen").build()))
                  .documentType(DocumentTypeTransformer.transformToDTO(documentType1))
                  .build());
      var pendingProceedingDTO2 =
          dto2.toBuilder()
              .fileNumbers(
                  List.of(
                      FileNumberDTO.builder()
                          .documentationUnit(dto2)
                          .rank(0L)
                          .value("123")
                          .build()))
              .deviatingFileNumbers(
                  List.of(
                      DeviatingFileNumberDTO.builder()
                          .documentationUnit(dto2)
                          .rank(0L)
                          .value("123")
                          .build()))
              .build();

      var dto3 =
          repository.save(
              DecisionDTO.builder()
                  .documentationOffice(documentationOffice)
                  .documentationOffice(documentationOffice)
                  .date(LocalDate.of(2023, 12, 11))
                  .deviatingDates(
                      List.of(
                          DeviatingDateDTO.builder()
                              .rank(0L)
                              .value(LocalDate.of(2023, 12, 11))
                              .build()))
                  .documentNumber("DocumentNumb3")
                  .court(CourtTransformer.transformToDTO(courtAgAachen))
                  .deviatingCourts(
                      List.of(DeviatingCourtDTO.builder().rank(0L).value("AG Aachen").build()))
                  .documentType(DocumentTypeTransformer.transformToDTO(documentType1))
                  .build());
      var decisionDTO =
          dto3.toBuilder()
              .deviatingFileNumbers(
                  List.of(
                      DeviatingFileNumberDTO.builder()
                          .rank(0L)
                          .value("123")
                          .documentationUnit(dto3)
                          .build()))
              .build();

      repository.save(pendingProceedingDTO1);
      repository.save(pendingProceedingDTO2);
      var decision = repository.save(decisionDTO);

      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Act
      duplicateCheckService.checkAllDuplicates();

      var foundDocUnit = documentationUnitService.getByUuid(decision.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).isEmpty();
      assertThat(((DocumentationUnit) foundDocUnit).managementData().duplicateRelations())
          .isEmpty();
    }
  }

  @Nested
  class FindDuplicatesQueryTest {
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideTestCasesWithDuplicates")
    void findDuplicateTests(
        String testName, CreationParameters firstParams, CreationParameters secondParams)
        throws DocumentationUnitNotExistsException {
      // Arrange
      var docUnitToBeChecked = generateNewDocumentationUnit(docOffice, Optional.of(firstParams));
      generateNewDocumentationUnit(docOffice, Optional.of(secondParams));

      // Act
      duplicateCheckService.checkAllDuplicates();
      var foundDocUnit =
          (DocumentationUnit) documentationUnitService.getByUuid(docUnitToBeChecked.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).hasSize(1);
      assertThat(
              foundDocUnit.managementData().duplicateRelations().stream()
                  .findFirst()
                  .get()
                  .documentNumber())
          .isEqualTo(secondParams.documentNumber);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideTestCasesWithoutDuplicates")
    void findNoDuplicateTests(
        String testName, CreationParameters firstParams, CreationParameters secondParams)
        throws DocumentationUnitNotExistsException {
      // Arrange
      var docUnitToBeChecked = generateNewDocumentationUnit(docOffice, Optional.of(firstParams));
      generateNewDocumentationUnit(docOffice, Optional.of(secondParams));

      // Act
      duplicateCheckService.checkAllDuplicates();
      var foundDocUnit =
          (DocumentationUnit) documentationUnitService.getByUuid(docUnitToBeChecked.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).isEmpty();
      assertThat(foundDocUnit.managementData().duplicateRelations()).isEmpty();
    }

    static Stream<Arguments> provideTestCasesWithDuplicates() {
      return Stream.of(
          Arguments.of(
              "findDuplicates_withFileNumberAndDecisionDateAndDocType",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .fileNumbers(List.of("AZ-123"))
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .documentType(documentType1)
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .fileNumbers(List.of("AZ-123"))
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .documentType(documentType1)
                  .build()),
          Arguments.of(
              "findDuplicates_withFileNumberAndDeviatingDecisionDateAndDocType",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .fileNumbers(List.of("AZ-123"))
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .documentType(documentType2)
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .fileNumbers(List.of("AZ-123"))
                  .deviatingDecisionDates(List.of(LocalDate.of(2020, 12, 1)))
                  .documentType(documentType2)
                  .build()),
          Arguments.of(
              "findDuplicates_withDeviatingFileNumberAndDecisionDateAndDocType",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .fileNumbers(List.of("AZ-123"))
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .documentType(documentType1)
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .deviatingFileNumbers(List.of("AZ-123"))
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .documentType(documentType1)
                  .build()),
          Arguments.of(
              "findDuplicates_withDeviatingFileNumberAndDeviatingDecisionDateAndDocType",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .fileNumbers(List.of("AZ-123"))
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .documentType(documentType1)
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .deviatingFileNumbers(List.of("AZ-123"))
                  .deviatingDecisionDates(List.of(LocalDate.of(2020, 12, 1)))
                  .documentType(documentType1)
                  .build()),
          Arguments.of(
              "findDuplicates_withFileNumberAndCourtAndDocType",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .fileNumbers(List.of("AZ-123"))
                  .court(courtAgAachen)
                  .documentType(documentType1)
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .fileNumbers(List.of("AZ-123"))
                  .court(courtAgAachen)
                  .documentType(documentType1)
                  .build()),
          Arguments.of(
              "findDuplicates_withOneDeviatingFileNumberAndCourtAndDocType",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .deviatingFileNumbers(List.of("AZ-123"))
                  .court(courtAgAachen)
                  .documentType(documentType1)
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .fileNumbers(List.of("AZ-123"))
                  .court(courtAgAachen)
                  .documentType(documentType1)
                  .build()),
          Arguments.of(
              "findDuplicates_withTwoDeviatingFileNumbersAndCourtAndDocType",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .deviatingFileNumbers(List.of("abc", "AZ-123"))
                  .court(courtAgAachen)
                  .documentType(documentType1)
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .deviatingFileNumbers(List.of("def", "AZ-123"))
                  .court(courtAgAachen)
                  .documentType(documentType1)
                  .build()),
          Arguments.of(
              "findDuplicates_withFileNumberAndOneDeviatingCourtAndDocType",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .fileNumbers(List.of("AZ-123"))
                  .court(courtAgAachen)
                  .documentType(documentType1)
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .fileNumbers(List.of("AZ-123"))
                  .deviatingCourts(List.of("Other", "aG aachen"))
                  .documentType(documentType1)
                  .build()),
          Arguments.of(
              "findDuplicates_withFileNumberAndTwoDeviatingCourtsAndDocType",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .fileNumbers(List.of("AZ-123"))
                  .deviatingCourts(List.of("OLG Dresden"))
                  .documentType(documentType1)
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .fileNumbers(List.of("AZ-123"))
                  .deviatingCourts(List.of("OLG Dresden"))
                  .documentType(documentType1)
                  .build()),
          Arguments.of(
              "findDuplicates_withTwoDeviatingFileNumbersAndTwoDeviatingCourtsAndDocType",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .deviatingFileNumbers(List.of("AZ-123"))
                  .deviatingCourts(List.of("OLG Dresden"))
                  .documentType(documentType1)
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .deviatingFileNumbers(List.of("AZ-123"))
                  .deviatingCourts(List.of("OLG Dresden"))
                  .documentType(documentType1)
                  .build()),
          Arguments.of(
              "findDuplicates_withEcli",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .ecli("ECLI:DE:BFH:2024:B.080980.TEST.00.0")
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .ecli("ECLI:DE:BFH:2024:B.080980.TEST.00.0")
                  .build()),
          Arguments.of(
              "findDuplicates_withDeviatingEcli",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .ecli("ECLI:DE:BFH:2024:B.080980.TEST.00.0")
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .deviatingEclis(List.of("ECLI:DE:BFH:2024:B.080980.TEST.00.0"))
                  .build()));
    }

    static Stream<Arguments> provideTestCasesWithoutDuplicates() {
      return Stream.of(
          Arguments.of(
              "findNoDuplicates_withFileNumberAndDecisionDateAndDifferentDocType",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .fileNumbers(List.of("AZ-123"))
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .documentType(documentType1)
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .fileNumbers(List.of("AZ-123"))
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .documentType(documentType2)
                  .build()),
          Arguments.of(
              "findNoDuplicates_withFileNumberAndDifferentDecisionDateAndDocType",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .fileNumbers(List.of("AZ-123"))
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .documentType(documentType1)
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .fileNumbers(List.of("AZ-123"))
                  .decisionDate(LocalDate.of(2020, 12, 2))
                  .documentType(documentType1)
                  .build()),
          Arguments.of(
              "findNoDuplicates_withDifferentFileNumberAndDecisionDateAndDocType",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .fileNumbers(List.of("Diff_AZ-123"))
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .documentType(documentType1)
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .fileNumbers(List.of("AZ-123"))
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .documentType(documentType1)
                  .build()),
          Arguments.of(
              "findDuplicates_withDifferentFileNumberAndCourtAndDocType",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .fileNumbers(List.of("AZ-123"))
                  .court(courtAgAachen)
                  .documentType(documentType1)
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .fileNumbers(List.of("AZ-456"))
                  .court(courtAgAachen)
                  .documentType(documentType1)
                  .build()),
          Arguments.of(
              "findDuplicates_withFileNumberAndDifferentCourtAndDocType",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .fileNumbers(List.of("AZ-123"))
                  .court(courtAgAachen)
                  .documentType(documentType1)
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .fileNumbers(List.of("AZ-123"))
                  .court(courtOlgDresden)
                  .documentType(documentType1)
                  .build()),
          Arguments.of(
              "findDuplicates_withFileNumberAndCourtAndDifferentDocType",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .fileNumbers(List.of("AZ-123"))
                  .court(courtAgAachen)
                  .documentType(documentType1)
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .fileNumbers(List.of("AZ-123"))
                  .court(courtAgAachen)
                  .documentType(documentType2)
                  .build()),
          Arguments.of(
              "findDuplicates_withDifferentEcli",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .ecli("ECLI:DE:BFH:2024:B.080980.TEST.00.0")
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .ecli("ECLI:DE:BFH:2025:B.080980.TEST.00.0")
                  .build()));
    }
  }

  @Nested
  class FindAllDuplicatesQueryTest {
    @Test
    void findAllDuplicates_withCourtAndFileNumberAndDocType()
        throws DocumentationUnitNotExistsException {
      // Arrange
      generateNewDocumentationUnit(
          docOffice,
          Optional.of(
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .court(courtAgAachen)
                  .fileNumbers(List.of("AZ-123"))
                  .documentType(documentType1)
                  .ecli("ECLI:DE:BFH:2024")
                  .build()));

      // Gericht + AZ + DocTyp
      generateNewDocumentationUnit(
          docOffice,
          Optional.of(
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .court(courtAgAachen)
                  .documentType(documentType1)
                  .fileNumbers(List.of("AZ-123"))
                  .build()));
      // Gericht + abw. AZ + DocTyp
      generateNewDocumentationUnit(
          docOffice,
          Optional.of(
              CreationParameters.builder()
                  .documentNumber("DocumentNumb3")
                  .court(courtAgAachen)
                  .documentType(documentType1)
                  .deviatingFileNumbers(List.of("AZ-123"))
                  .build()));
      // abw. Gericht + AZ + DocTyp
      generateNewDocumentationUnit(
          docOffice,
          Optional.of(
              CreationParameters.builder()
                  .documentNumber("DocumentNumb4")
                  .deviatingCourts(List.of("AG Aachen"))
                  .documentType(documentType1)
                  .fileNumbers(List.of("AZ-123"))
                  .build()));
      // abw. Gericht + abw. AZ + DocTyp
      generateNewDocumentationUnit(
          docOffice,
          Optional.of(
              CreationParameters.builder()
                  .documentNumber("DocumentNumb5")
                  .deviatingCourts(List.of("AG Aachen"))
                  .documentType(documentType1)
                  .deviatingFileNumbers(List.of("AZ-123"))
                  .build()));

      // Act
      duplicateCheckService.checkAllDuplicates();

      // Assert: 5 doc units, all related with each other: 4 + 3 + 2 + 1 = 10 relations.
      var allDuplicates = duplicateRelationRepository.findAll();
      assertThat(allDuplicates).hasSize(10);
    }
  }

  @Builder(toBuilder = true)
  public record CreationParameters(
      String documentNumber,
      DocumentationOffice documentationOffice,
      Boolean isJdvDuplicateCheckActive,
      List<String> fileNumbers,
      List<String> deviatingFileNumbers,
      LocalDate decisionDate,
      List<LocalDate> deviatingDecisionDates,
      Court court,
      List<String> deviatingCourts,
      DocumentType documentType,
      String ecli,
      List<String> deviatingEclis,
      PublicationStatus publicationStatus) {}

  private DecisionDTO generateNewDocumentationUnit(
      DocumentationOffice userDocOffice, Optional<CreationParameters> parameters)
      throws DocumentationUnitException {

    // default office is user office
    CreationParameters params =
        parameters.orElse(CreationParameters.builder().documentationOffice(userDocOffice).build());
    if (params.documentationOffice() == null) {
      params = params.toBuilder().documentationOffice(userDocOffice).build();
    }

    DocumentationUnit docUnit =
        DocumentationUnit.builder()
            .version(0L)
            .documentNumber(params.documentNumber())
            .coreData(
                CoreData.builder()
                    .documentationOffice(params.documentationOffice())
                    .documentType(params.documentType())
                    .decisionDate(params.decisionDate())
                    .deviatingDecisionDates(params.deviatingDecisionDates())
                    .court(params.court())
                    .deviatingCourts(params.deviatingCourts())
                    .ecli(params.ecli())
                    .deviatingEclis(params.deviatingEclis())
                    .build())
            .status(Status.builder().publicationStatus(params.publicationStatus()).build())
            .build();

    var documentationUnitDTO =
        repository.save(
            DecisionTransformer.transformToDTO(
                DecisionDTO.builder()
                    .documentationOffice(
                        DocumentationOfficeTransformer.transformToDTO(
                            docUnit.coreData().documentationOffice()))
                    .creatingDocumentationOffice(
                        DocumentationOfficeTransformer.transformToDTO(
                            docUnit.coreData().creatingDocOffice()))
                    .isJdvDuplicateCheckActive(params.isJdvDuplicateCheckActive())
                    .build(),
                docUnit));

    if (params.fileNumbers() != null) {
      DecisionDTO finalDocumentationUnitDTO = documentationUnitDTO;
      var fileNumbers =
          params.fileNumbers.stream()
              .map(
                  fileNumber ->
                      FileNumberDTO.builder()
                          .documentationUnit(finalDocumentationUnitDTO)
                          .value(fileNumber)
                          .rank(0L)
                          .build())
              .toList();

      documentationUnitDTO = documentationUnitDTO.toBuilder().fileNumbers(fileNumbers).build();
    }

    if (params.deviatingFileNumbers() != null) {
      DecisionDTO finalDocumentationUnitDTO = documentationUnitDTO;
      var devfileNumbers =
          params.deviatingFileNumbers.stream()
              .map(
                  fileNumber ->
                      DeviatingFileNumberDTO.builder()
                          .documentationUnit(finalDocumentationUnitDTO)
                          .value(fileNumber)
                          .rank(0L)
                          .build())
              .toList();

      documentationUnitDTO =
          documentationUnitDTO.toBuilder().deviatingFileNumbers(devfileNumbers).build();
    }

    if (params.publicationStatus != null) {
      documentationUnitDTO =
          documentationUnitDTO.toBuilder()
              .status(
                  StatusDTO.builder()
                      .publicationStatus(params.publicationStatus())
                      .documentationUnit(documentationUnitDTO)
                      .build())
              .build();
    }

    return repository.save(documentationUnitDTO);
  }
}
