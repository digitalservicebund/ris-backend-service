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
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.Builder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
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

  @Test
  void checkDuplicates_withoutFileNumbers_shouldDoNothing()
      throws DocumentationUnitNotExistsException {
    // Arrange
    var creationParams =
        DocumentationUnitTestCreationParameters.builder()
            .documentNumber("DocumentNumb1")
            .decisionDate(LocalDate.of(2020, 12, 1))
            .build();
    var docUnitWithoutFileNumbers =
        DocumentationUnitTransformer.transformToDomain(
            generateNewDocumentationUnit(docOffice, Optional.of(creationParams)));

    assertThat(duplicateRelationRepository.findAll()).isEmpty();

    // Act
    duplicateCheckService.checkDuplicates(docUnitWithoutFileNumbers.documentNumber());

    var foundDocUnit = documentationUnitService.getByUuid(docUnitWithoutFileNumbers.uuid());

    // Assert
    assertThat(duplicateRelationRepository.findAll()).isEmpty();
    assertThat(foundDocUnit.managementData().duplicateRelations()).isEmpty();
  }


  @Test
  void checkDuplicates_withoutFoundDuplicates_shouldDoNothing()
      throws DocumentationUnitNotExistsException {
    // Arrange
    var creationParams =
        DocumentationUnitTestCreationParameters.builder()
            .documentNumber("DocumentNumb1")
            .decisionDate(LocalDate.of(2020, 12, 1))
            .fileNumber("AZ-123")
            .build();
    var docUnitToBeChecked =
        DocumentationUnitTransformer.transformToDomain(
            generateNewDocumentationUnit(docOffice, Optional.of(creationParams)));

    var creationParams2 =
        DocumentationUnitTestCreationParameters.builder()
            .documentNumber("DocumentNumb2")
            .decisionDate(LocalDate.of(2020, 12, 1))
            .fileNumber("AZ-Different")
            .build();
    // create non duplicate docUnit
    DocumentationUnitTransformer.transformToDomain(
        generateNewDocumentationUnit(docOffice, Optional.of(creationParams2)));

    assertThat(duplicateRelationRepository.findAll()).isEmpty();

    // Act
    duplicateCheckService.checkDuplicates(docUnitToBeChecked.documentNumber());

    var initialDocUnit = documentationUnitService.getByUuid(docUnitToBeChecked.uuid());

    // Assert
    assertThat(duplicateRelationRepository.findAll()).isEmpty();
    assertThat(initialDocUnit.managementData().duplicateRelations()).isEmpty();
  }

  @Test
  void checkDuplicates_withoutExistingDuplicates_shouldCreateNewDuplicateWithPendingStatus()
      throws DocumentationUnitNotExistsException {
    // Arrange
    var creationParams =
        DocumentationUnitTestCreationParameters.builder()
            .documentNumber("DocumentNumb1")
            .decisionDate(LocalDate.of(2020, 12, 1))
            .fileNumber("AZ-123")
            .build();
    var docUnitToBeChecked =
        DocumentationUnitTransformer.transformToDomain(
            generateNewDocumentationUnit(docOffice, Optional.of(creationParams)));

    var creationParams2 =
        DocumentationUnitTestCreationParameters.builder()
            .documentNumber("DocumentNumb2")
            .decisionDate(LocalDate.of(2020, 12, 1))
            .fileNumber("AZ-123")
            .build();
    var docUnitDuplicate =
        DocumentationUnitTransformer.transformToDomain(
            generateNewDocumentationUnit(docOffice, Optional.of(creationParams2)));
    assertThat(duplicateRelationRepository.findAll()).isEmpty();

    // Act
    duplicateCheckService.checkDuplicates(docUnitToBeChecked.documentNumber());

    var foundDocUnit = documentationUnitService.getByUuid(docUnitToBeChecked.uuid());

    // Assert
    assertThat(duplicateRelationRepository.findAll()).hasSize(1);
    DuplicateRelation duplicate =
        foundDocUnit.managementData().duplicateRelations().stream().findFirst().get();
    assertThat(duplicate.documentNumber()).isEqualTo(docUnitDuplicate.documentNumber());
    assertThat(duplicate.status()).isEqualTo(DuplicateRelationStatus.PENDING);
  }

  @Test
  void checkDuplicates_withoutExistingDuplicatesAndIsJdvDuplicateCheckActiveTrue_shouldCreateNewDuplicateWithPendingStatus()
      throws DocumentationUnitNotExistsException {
    // Arrange
    var creationParams =
        DocumentationUnitTestCreationParameters.builder()
            .documentNumber("DocumentNumb1")
            .decisionDate(LocalDate.of(2020, 12, 1))
            .fileNumber("AZ-123")
            .build();
    var docUnitToBeChecked =
        DocumentationUnitTransformer.transformToDomain(
            generateNewDocumentationUnit(docOffice, Optional.of(creationParams)));

    var creationParams2 =
        DocumentationUnitTestCreationParameters.builder()
            .documentNumber("DocumentNumb2")
            .decisionDate(LocalDate.of(2020, 12, 1))
            .isJdvDuplicateCheckActive(true)
            .fileNumber("AZ-123")
            .build();
    var docUnitDuplicate =
        DocumentationUnitTransformer.transformToDomain(
            generateNewDocumentationUnit(docOffice, Optional.of(creationParams2)));
    assertThat(duplicateRelationRepository.findAll()).isEmpty();

    // Act
    duplicateCheckService.checkDuplicates(docUnitToBeChecked.documentNumber());

    var foundDocUnit = documentationUnitService.getByUuid(docUnitToBeChecked.uuid());

    // Assert
    assertThat(duplicateRelationRepository.findAll()).hasSize(1);
    DuplicateRelation duplicate =
        foundDocUnit.managementData().duplicateRelations().stream().findFirst().get();
    assertThat(duplicate.documentNumber()).isEqualTo(docUnitDuplicate.documentNumber());
    assertThat(duplicate.status()).isEqualTo(DuplicateRelationStatus.PENDING);
  }

  @Test
  void checkDuplicates_withoutExistingDuplicatesAndIsJdvDuplicateCheckActiveFalse_shouldCreateNewDuplicateWithIgnoredStatus()
      throws DocumentationUnitNotExistsException {
    // Arrange
    var creationParams =
        DocumentationUnitTestCreationParameters.builder()
            .documentNumber("DocumentNumb1")
            .decisionDate(LocalDate.of(2020, 12, 1))
            .fileNumber("AZ-123")
            .build();
    var docUnitToBeChecked =
        DocumentationUnitTransformer.transformToDomain(
            generateNewDocumentationUnit(docOffice, Optional.of(creationParams)));

    var creationParams2 =
        DocumentationUnitTestCreationParameters.builder()
            .documentNumber("DocumentNumb2")
            .decisionDate(LocalDate.of(2020, 12, 1))
            .isJdvDuplicateCheckActive(false)
            .fileNumber("AZ-123")
            .build();
    var docUnitDuplicate =
        DocumentationUnitTransformer.transformToDomain(
            generateNewDocumentationUnit(docOffice, Optional.of(creationParams2)));
    assertThat(duplicateRelationRepository.findAll()).isEmpty();

    // Act
    duplicateCheckService.checkDuplicates(docUnitToBeChecked.documentNumber());

    var foundDocUnit = documentationUnitService.getByUuid(docUnitToBeChecked.uuid());

    // Assert
    assertThat(duplicateRelationRepository.findAll()).hasSize(1);
    DuplicateRelation duplicate =
        foundDocUnit.managementData().duplicateRelations().stream().findFirst().get();
    assertThat(duplicate.documentNumber()).isEqualTo(docUnitDuplicate.documentNumber());
    assertThat(duplicate.status()).isEqualTo(DuplicateRelationStatus.IGNORED);
  }

  @Test
  void checkDuplicates_withExistingDuplicatesAndIsJdvDuplicateCheckActiveFalse_shouldUpdateStatusToIgnored()
      throws DocumentationUnitNotExistsException {
    // Arrange
    var creationParams =
        DocumentationUnitTestCreationParameters.builder()
            .documentNumber("DocumentNumb1")
            .decisionDate(LocalDate.of(2020, 12, 1))
            .fileNumber("AZ-123")
            .build();
    var docUnitToBeChecked =
        DocumentationUnitTransformer.transformToDomain(
            generateNewDocumentationUnit(docOffice, Optional.of(creationParams)));

    var creationParams2 =
        DocumentationUnitTestCreationParameters.builder()
            .documentNumber("DocumentNumb2")
            .decisionDate(LocalDate.of(2020, 12, 1))
            .fileNumber("AZ-123")
            .build();
    var duplicateDTO = generateNewDocumentationUnit(docOffice, Optional.of(creationParams2));
    var docUnitDuplicate =
        DocumentationUnitTransformer.transformToDomain(
            duplicateDTO);
    assertThat(duplicateRelationRepository.findAll()).isEmpty();

    // Create duplicate with pending status
    duplicateCheckService.checkDuplicates(docUnitToBeChecked.documentNumber());
    assertThat(documentationUnitService.getByUuid(docUnitToBeChecked.uuid()).managementData().duplicateRelations().stream().findFirst().get().status()).isEqualTo(DuplicateRelationStatus.PENDING);

    // change isJdvDuplicateCheckActive from null to false
    duplicateDTO.setIsJdvDuplicateCheckActive(false);
    databaseDocumentationUnitRepository.save(duplicateDTO);

    // Act
    duplicateCheckService.checkDuplicates(docUnitToBeChecked.documentNumber());
    var foundDocUnit = documentationUnitService.getByUuid(docUnitToBeChecked.uuid());

    // Assert
    assertThat(duplicateRelationRepository.findAll()).hasSize(1);
    DuplicateRelation duplicate =
        foundDocUnit.managementData().duplicateRelations().stream().findFirst().get();
    assertThat(duplicate.documentNumber()).isEqualTo(docUnitDuplicate.documentNumber());
    assertThat(duplicate.status()).isEqualTo(DuplicateRelationStatus.IGNORED);
  }

  @Test
  void checkDuplicates_withExistingDuplicates_shouldDeleteOutdatedDuplicates()
      throws DocumentationUnitNotExistsException {
    // Arrange
    var creationParams =
        DocumentationUnitTestCreationParameters.builder()
            .documentNumber("DocumentNumb1")
            .decisionDate(LocalDate.of(2020, 12, 1))
            .fileNumber("AZ-123")
            .build();
    var docUnitToBeChecked =
        DocumentationUnitTransformer.transformToDomain(
            generateNewDocumentationUnit(docOffice, Optional.of(creationParams)));

    var creationParams2 =
        DocumentationUnitTestCreationParameters.builder()
            .documentNumber("DocumentNumb2")
            .decisionDate(LocalDate.of(2020, 12, 1))
            .fileNumber("AZ-123")
            .build();
    var duplicateDTO = generateNewDocumentationUnit(docOffice, Optional.of(creationParams2));
    var docUnitDuplicate =
        DocumentationUnitTransformer.transformToDomain(
            duplicateDTO);
    assertThat(duplicateRelationRepository.findAll()).isEmpty();

    // Create duplicate with pending status
    duplicateCheckService.checkDuplicates(docUnitToBeChecked.documentNumber());
    assertThat(documentationUnitService.getByUuid(docUnitToBeChecked.uuid()).managementData().duplicateRelations().stream().findFirst().get().status()).isEqualTo(DuplicateRelationStatus.PENDING);

    // change isJdvDuplicateCheckActive from null to false
    duplicateDTO.setIsJdvDuplicateCheckActive(null);
    databaseDocumentationUnitRepository.save(duplicateDTO);

    // Act
    duplicateCheckService.checkDuplicates(docUnitToBeChecked.documentNumber());
    var foundDocUnit = documentationUnitService.getByUuid(docUnitToBeChecked.uuid());

    // Assert
    assertThat(duplicateRelationRepository.findAll()).hasSize(1);
    DuplicateRelation duplicate =
        foundDocUnit.managementData().duplicateRelations().stream().findFirst().get();
    assertThat(duplicate.documentNumber()).isEqualTo(docUnitDuplicate.documentNumber());
    assertThat(duplicate.status()).isEqualTo(DuplicateRelationStatus.IGNORED);
  }

  @Builder(toBuilder = true)
  public record DocumentationUnitTestCreationParameters(
      String documentNumber,
      DocumentationOffice documentationOffice,
      Court court,
      List<String> deviatingCourts,
      DocumentType documentType,
      @PastOrPresent LocalDate decisionDate,
      List<LocalDate> deviatingDecisionDates,
      String fileNumber,
      List<String> deviatingFileNumbers,
      String ecli,
      List<String> deviatingEclis,
      Boolean isJdvDuplicateCheckActive
  ) {}

  private DocumentationUnitDTO generateNewDocumentationUnit(
      DocumentationOffice userDocOffice,
      Optional<DocumentationUnitTestCreationParameters> parameters)
      throws DocumentationUnitException {

    // default office is user office
    DocumentationUnitTestCreationParameters params =
        parameters.orElse(
            DocumentationUnitTestCreationParameters.builder()
                .documentationOffice(userDocOffice)
                .build());
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
                    .fileNumbers(params.fileNumber() == null ? null : List.of(params.fileNumber()))
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
