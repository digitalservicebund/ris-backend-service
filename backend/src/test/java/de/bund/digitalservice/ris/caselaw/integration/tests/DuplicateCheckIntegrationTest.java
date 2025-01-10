package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDSDocOffice;
import static de.bund.digitalservice.ris.caselaw.AuthUtils.mockUserGroups;
import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberGeneratorService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberRecyclingService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentationUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseProcedureService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentNumberPatternConfig;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentationUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.DocxConverterService;
import de.bund.digitalservice.ris.caselaw.adapter.DuplicateCheckService;
import de.bund.digitalservice.ris.caselaw.adapter.DuplicateRelationService;
import de.bund.digitalservice.ris.caselaw.adapter.KeycloakUserService;
import de.bund.digitalservice.ris.caselaw.adapter.LdmlExporterService;
import de.bund.digitalservice.ris.caselaw.adapter.OAuthService;
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
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DuplicateRelationRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDeltaMigrationRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresHandoverReportRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.CourtTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentTypeTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationOfficeTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationUnitTransformer;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentService;
import de.bund.digitalservice.ris.caselaw.domain.AuthService;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitDocxMetadataInitializationService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateRelation;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateRelationStatus;
import de.bund.digitalservice.ris.caselaw.domain.HandoverService;
import de.bund.digitalservice.ris.caselaw.domain.MailService;
import de.bund.digitalservice.ris.caselaw.domain.UserGroupService;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitException;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.domain.mapper.PatchMapperService;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@RISIntegrationTest(
    imports = {
      DocumentationUnitService.class,
      DuplicateRelationService.class,
      PostgresDeltaMigrationRepositoryImpl.class,
      DatabaseDocumentNumberGeneratorService.class,
      DuplicateCheckService.class,
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
      KeycloakUserService.class
    },
    controllers = {DocumentationUnitController.class})
@Sql(scripts = {"classpath:courts_init.sql", "classpath:document_types.sql"})
@Sql(
    scripts = {"classpath:courts_cleanup.sql"},
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class DuplicateCheckIntegrationTest {
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
  @Autowired private DuplicateCheckService duplicateCheckService;

  @MockBean private S3AsyncClient s3AsyncClient;
  @MockBean private MailService mailService;
  @MockBean private DocxConverterService docxConverterService;
  @MockBean private UserGroupService userGroupService;
  @MockBean private ClientRegistrationRepository clientRegistrationRepository;
  @MockBean private AttachmentService attachmentService;
  @MockBean private PatchMapperService patchMapperService;
  @MockBean private HandoverService handoverService;
  @MockBean private LdmlExporterService ldmlExporterService;
  @MockBean private DatabaseDocumentNumberRepository databaseDocumentNumberRepository;

  @MockBean
  private DocumentationUnitDocxMetadataInitializationService
      documentationUnitDocxMetadataInitializationService;

  private DocumentationOffice docOffice;
  private DocumentationOfficeDTO documentationOffice;

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
      duplicateCheckService.checkDuplicates(docUnitWithoutFileNumbers.getDocumentNumber());

      var foundDocUnit = documentationUnitService.getByUuid(docUnitWithoutFileNumbers.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).isEmpty();
      assertThat(foundDocUnit.managementData().duplicateRelations()).isEmpty();
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
      duplicateCheckService.checkDuplicates(docUnitWithoutFileNumbers.getDocumentNumber());

      var foundDocUnit = documentationUnitService.getByUuid(docUnitWithoutFileNumbers.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).isEmpty();
      assertThat(foundDocUnit.managementData().duplicateRelations()).isEmpty();
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
      duplicateCheckService.checkDuplicates(docUnitToBeChecked.getDocumentNumber());

      var initialDocUnit = documentationUnitService.getByUuid(docUnitToBeChecked.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).isEmpty();
      assertThat(initialDocUnit.managementData().duplicateRelations()).isEmpty();
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
                      .fileNumbers(List.of("AZ-123"))
                      .build()));

      var docUnitDuplicateDTO =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb2")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .fileNumbers(List.of("AZ-123"))
                      .build()));

      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Act
      duplicateCheckService.checkDuplicates(docUnitToBeChecked.getDocumentNumber());

      var foundDocUnit = documentationUnitService.getByUuid(docUnitToBeChecked.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).hasSize(1);
      DuplicateRelation duplicate =
          foundDocUnit.managementData().duplicateRelations().stream().findFirst().get();
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
                      .deviatingFileNumbers(List.of("AZ-123-match", "AZ-888", "AZ-999"))
                      .build()));

      var docUnitDuplicateDTO =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb2")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .fileNumbers(List.of("AZ-123-match", "AZ-777", "AZ-555"))
                      .build()));

      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Act
      duplicateCheckService.checkDuplicates(docUnitToBeChecked.getDocumentNumber());

      var foundDocUnit = documentationUnitService.getByUuid(docUnitToBeChecked.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).hasSize(1);
      DuplicateRelation duplicate =
          foundDocUnit.managementData().duplicateRelations().stream().findFirst().get();
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
                      .fileNumbers(List.of("AZ-123"))
                      .build()));

      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Act
      duplicateCheckService.checkDuplicates(docUnitToBeChecked.getDocumentNumber());

      var foundDocUnit = documentationUnitService.getByUuid(docUnitToBeChecked.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).hasSize(1);
      DuplicateRelation duplicate =
          foundDocUnit.managementData().duplicateRelations().stream().findFirst().get();
      assertThat(duplicate.documentNumber()).isEqualTo(docUnitDuplicateDTO.getDocumentNumber());
      assertThat(duplicate.status()).isEqualTo(DuplicateRelationStatus.PENDING);
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
                      .fileNumbers(List.of("AZ-123"))
                      .build()));

      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Act
      duplicateCheckService.checkDuplicates(docUnitToBeChecked.getDocumentNumber());

      var foundDocUnit = documentationUnitService.getByUuid(docUnitToBeChecked.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).hasSize(1);
      DuplicateRelation duplicate =
          foundDocUnit.managementData().duplicateRelations().stream().findFirst().get();
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
                      .fileNumbers(List.of("AZ-123"))
                      .build()));

      var duplicateDTO =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb2")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .fileNumbers(List.of("AZ-123"))
                      .build()));

      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Create duplicate with pending status
      duplicateCheckService.checkDuplicates(docUnitToBeChecked.getDocumentNumber());
      assertThat(
              documentationUnitService
                  .getByUuid(docUnitToBeChecked.getId())
                  .managementData()
                  .duplicateRelations()
                  .stream()
                  .findFirst()
                  .get()
                  .status())
          .isEqualTo(DuplicateRelationStatus.PENDING);

      // change isJdvDuplicateCheckActive from null to false
      duplicateDTO.setIsJdvDuplicateCheckActive(false);
      databaseDocumentationUnitRepository.save(duplicateDTO);

      // Act
      duplicateCheckService.checkDuplicates(docUnitToBeChecked.getDocumentNumber());
      var foundDocUnit = documentationUnitService.getByUuid(docUnitToBeChecked.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).hasSize(1);
      DuplicateRelation duplicate =
          foundDocUnit.managementData().duplicateRelations().stream().findFirst().get();
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
                      .fileNumbers(List.of("AZ-123"))
                      .build()));

      generateNewDocumentationUnit(
          docOffice,
          Optional.of(
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .fileNumbers(List.of("AZ-123"))
                  .build()));
      var duplicateDTO =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb3")
                      .decisionDate(LocalDate.of(2020, 12, 1))
                      .fileNumbers(List.of("AZ-123"))
                      .build()));
      assertThat(duplicateRelationRepository.findAll()).isEmpty();

      // Create duplicates
      duplicateCheckService.checkDuplicates(docUnitToBeChecked.getDocumentNumber());
      assertThat(
              documentationUnitService
                  .getByUuid(docUnitToBeChecked.getId())
                  .managementData()
                  .duplicateRelations()
                  .stream()
                  .findFirst()
                  .get()
                  .status())
          .isEqualTo(DuplicateRelationStatus.PENDING);
      assertThat(duplicateRelationRepository.findAll()).hasSize(2);

      // change decisionDate in second duplicate
      duplicateDTO.setDecisionDate(LocalDate.of(2022, 2, 22));
      databaseDocumentationUnitRepository.save(duplicateDTO);

      // Act
      duplicateCheckService.checkDuplicates(docUnitToBeChecked.getDocumentNumber());
      var foundDocUnit = documentationUnitService.getByUuid(docUnitToBeChecked.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).hasSize(1);
      assertThat(
              foundDocUnit.managementData().duplicateRelations().stream()
                  .findFirst()
                  .get()
                  .documentNumber())
          .isEqualTo("DocumentNumb2");
    }
  }

  @Nested
  class FindDuplicatesQueryTest {
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideTestCases")
    void findDuplicateTests(
        String testName, CreationParameters firstParams, CreationParameters secondParams)
        throws DocumentationUnitNotExistsException {
      // Arrange
      var docUnitToBeChecked = generateNewDocumentationUnit(docOffice, Optional.of(firstParams));
      generateNewDocumentationUnit(docOffice, Optional.of(secondParams));

      // Act
      duplicateCheckService.checkDuplicates(docUnitToBeChecked.getDocumentNumber());
      var foundDocUnit = documentationUnitService.getByUuid(docUnitToBeChecked.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).hasSize(1);
      assertThat(
              foundDocUnit.managementData().duplicateRelations().stream()
                  .findFirst()
                  .get()
                  .documentNumber())
          .isEqualTo(secondParams.documentNumber);
    }

    @Test
    void findDuplicates_withCourtAndFileNumber() throws DocumentationUnitNotExistsException {
      // Arrange
      var courtDTO = databaseCourtRepository.findBySearchStr("AG Aachen", 100).getFirst();
      var court = CourtTransformer.transformToDomain(courtDTO);
      var docUnitToBeChecked =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb1")
                      .court(court)
                      .fileNumbers(List.of("AZ-123"))
                      .build()));

      var duplicateDTO =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb2")
                      .court(court)
                      .fileNumbers(List.of("AZ-123"))
                      .build()));

      // Act
      duplicateCheckService.checkDuplicates(docUnitToBeChecked.getDocumentNumber());
      var foundDocUnit = documentationUnitService.getByUuid(docUnitToBeChecked.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).hasSize(1);
      assertThat(
              foundDocUnit.managementData().duplicateRelations().stream()
                  .findFirst()
                  .get()
                  .documentNumber())
          .isEqualTo(duplicateDTO.getDocumentNumber());
    }

    @Test
    void findDuplicates_withCourtAndDeviatingFileNumber()
        throws DocumentationUnitNotExistsException {
      // Arrange
      var courtDTO = databaseCourtRepository.findBySearchStr("AG Aachen", 100).getFirst();
      var court = CourtTransformer.transformToDomain(courtDTO);
      var docUnitToBeChecked =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb1")
                      .court(court)
                      .fileNumbers(List.of("AZ-123"))
                      .build()));

      var duplicateDTO =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb2")
                      .court(court)
                      .deviatingFileNumbers(List.of("AZ-123"))
                      .build()));

      // Act
      duplicateCheckService.checkDuplicates(docUnitToBeChecked.getDocumentNumber());
      var foundDocUnit = documentationUnitService.getByUuid(docUnitToBeChecked.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).hasSize(1);
      assertThat(
              foundDocUnit.managementData().duplicateRelations().stream()
                  .findFirst()
                  .get()
                  .documentNumber())
          .isEqualTo(duplicateDTO.getDocumentNumber());
    }

    @Test
    void findDuplicates_withDeviatingCourtsAndFileNumber()
        throws DocumentationUnitNotExistsException {
      // Arrange
      var docUnitToBeChecked =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb1")
                      .deviatingCourts(List.of("BGH", "BVerfG"))
                      .fileNumbers(List.of("AZ-123"))
                      .build()));

      var duplicateDTO =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb2")
                      .deviatingCourts(List.of("BGH"))
                      .fileNumbers(List.of("AZ-123"))
                      .build()));

      // Act
      duplicateCheckService.checkDuplicates(docUnitToBeChecked.getDocumentNumber());
      var foundDocUnit = documentationUnitService.getByUuid(docUnitToBeChecked.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).hasSize(1);
      assertThat(
              foundDocUnit.managementData().duplicateRelations().stream()
                  .findFirst()
                  .get()
                  .documentNumber())
          .isEqualTo(duplicateDTO.getDocumentNumber());
    }

    @Test
    void findDuplicates_withDeviatingCourtsAndDeviatingFileNumber()
        throws DocumentationUnitNotExistsException {
      // Arrange
      var docUnitToBeChecked =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb1")
                      .deviatingCourts(List.of("BGH", "BVerfG"))
                      .fileNumbers(List.of("AZ-123"))
                      .build()));

      var duplicateDTO =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb2")
                      .deviatingCourts(List.of("BGH"))
                      .deviatingFileNumbers(List.of("AZ-123"))
                      .build()));

      // Act
      duplicateCheckService.checkDuplicates(docUnitToBeChecked.getDocumentNumber());
      var foundDocUnit = documentationUnitService.getByUuid(docUnitToBeChecked.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).hasSize(1);
      assertThat(
              foundDocUnit.managementData().duplicateRelations().stream()
                  .findFirst()
                  .get()
                  .documentNumber())
          .isEqualTo(duplicateDTO.getDocumentNumber());
    }

    @Test
    void findDuplicates_withDocumentTypeAndFileNumber() throws DocumentationUnitNotExistsException {
      // Arrange
      var documentType =
          DocumentTypeTransformer.transformToDomain(
              databaseDocumentTypeRepository.findAll().get(0));
      var docUnitToBeChecked =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb1")
                      .documentType(documentType)
                      .fileNumbers(List.of("AZ-123"))
                      .build()));

      var duplicateDTO =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb2")
                      .documentType(documentType)
                      .fileNumbers(List.of("AZ-123"))
                      .build()));

      // Act
      duplicateCheckService.checkDuplicates(docUnitToBeChecked.getDocumentNumber());
      var foundDocUnit = documentationUnitService.getByUuid(docUnitToBeChecked.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).hasSize(1);
      assertThat(
              foundDocUnit.managementData().duplicateRelations().stream()
                  .findFirst()
                  .get()
                  .documentNumber())
          .isEqualTo(duplicateDTO.getDocumentNumber());
    }

    @Test
    void findDuplicates_withDocumentTypeAndDeviatingFileNumber()
        throws DocumentationUnitNotExistsException {
      // Arrange
      var documentType =
          DocumentTypeTransformer.transformToDomain(
              databaseDocumentTypeRepository.findAll().get(0));
      var docUnitToBeChecked =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb1")
                      .documentType(documentType)
                      .fileNumbers(List.of("AZ-123"))
                      .build()));

      var duplicateDTO =
          generateNewDocumentationUnit(
              docOffice,
              Optional.of(
                  CreationParameters.builder()
                      .documentNumber("DocumentNumb2")
                      .documentType(documentType)
                      .deviatingFileNumbers(List.of("AZ-123"))
                      .build()));

      // Act
      duplicateCheckService.checkDuplicates(docUnitToBeChecked.getDocumentNumber());
      var foundDocUnit = documentationUnitService.getByUuid(docUnitToBeChecked.getId());

      // Assert
      assertThat(duplicateRelationRepository.findAll()).hasSize(1);
      assertThat(
              foundDocUnit.managementData().duplicateRelations().stream()
                  .findFirst()
                  .get()
                  .documentNumber())
          .isEqualTo(duplicateDTO.getDocumentNumber());
    }

    static Stream<Arguments> provideTestCases() {
      return Stream.of(
          Arguments.of(
              "findDuplicates_withFileNumberAndDecisionDate",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .fileNumbers(List.of("AZ-123"))
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .fileNumbers(List.of("AZ-123"))
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .build()),
          Arguments.of(
              "findDuplicates_withFileNumberAndDeviatingDecisionDate",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .fileNumbers(List.of("AZ-123"))
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .fileNumbers(List.of("AZ-123"))
                  .deviatingDecisionDates(List.of(LocalDate.of(2020, 12, 1)))
                  .build()),
          Arguments.of(
              "findDuplicates_withDeviatingFileNumberAndDecisionDate",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .fileNumbers(List.of("AZ-123"))
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .deviatingFileNumbers(List.of("AZ-123"))
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .build()),
          Arguments.of(
              "findDuplicates_withDeviatingFileNumberAndDeviatingDecisionDate",
              CreationParameters.builder()
                  .documentNumber("DocumentNumb1")
                  .fileNumbers(List.of("AZ-123"))
                  .decisionDate(LocalDate.of(2020, 12, 1))
                  .build(),
              CreationParameters.builder()
                  .documentNumber("DocumentNumb2")
                  .deviatingFileNumbers(List.of("AZ-123"))
                  .deviatingDecisionDates(List.of(LocalDate.of(2020, 12, 1)))
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
      List<String> deviatingEclis) {}

  private DocumentationUnitDTO generateNewDocumentationUnit(
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
                    .fileNumbers(params.fileNumbers())
                    .deviatingFileNumbers(params.deviatingFileNumbers())
                    .documentType(params.documentType())
                    .decisionDate(params.decisionDate())
                    .deviatingDecisionDates(params.deviatingDecisionDates())
                    .court(params.court())
                    .deviatingCourts(params.deviatingCourts())
                    .ecli(params.ecli())
                    .deviatingEclis(params.deviatingEclis())
                    .build())
            .build();

    var documentationUnitDTO =
        repository.save(
            DocumentationUnitTransformer.transformToDTO(
                DocumentationUnitDTO.builder()
                    .documentationOffice(
                        DocumentationOfficeTransformer.transformToDTO(
                            docUnit.coreData().documentationOffice()))
                    .creatingDocumentationOffice(
                        DocumentationOfficeTransformer.transformToDTO(
                            docUnit.coreData().creatingDocOffice()))
                    .isJdvDuplicateCheckActive(params.isJdvDuplicateCheckActive())
                    .build(),
                docUnit));
    return repository.save(documentationUnitDTO);
  }
}
