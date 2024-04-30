package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDefaultDocOffice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

import de.bund.digitalservice.ris.caselaw.RisWebTestClient;
import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.adapter.AuthService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberGeneratorService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberRecyclingService;
import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentNumberPatternConfig;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.DocxConverterService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitSearchRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseLegalForceTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseNormAbbreviationRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseNormReferenceRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseRegionRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalForceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalForceTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NormAbbreviationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NormReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresPublicationReportRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.RegionDTO;
import de.bund.digitalservice.ris.caselaw.config.FeatureToggleConfig;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresJPAConfig;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.AttachmentService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@RISIntegrationTest(
    imports = {
      DocumentUnitService.class,
      DatabaseDocumentNumberGeneratorService.class,
      DatabaseDocumentNumberRecyclingService.class,
      DatabaseDocumentUnitStatusService.class,
      PostgresDocumentationUnitRepositoryImpl.class,
      PostgresPublicationReportRepositoryImpl.class,
      FlywayConfig.class,
      PostgresJPAConfig.class,
      SecurityConfig.class,
      AuthService.class,
      TestConfig.class,
      FeatureToggleConfig.class,
      DocumentNumberPatternConfig.class
    },
    controllers = {DocumentUnitController.class},
    timeout = "PT2M")
@Sql(scripts = {"classpath:legal_force_init.sql"})
@Sql(
    scripts = {"classpath:legal_force_cleanup.sql"},
    executionPhase = AFTER_TEST_METHOD)
class LegalForceIntegrationTest {
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
  @Autowired private DatabaseNormReferenceRepository normRepository;
  @Autowired private DatabaseNormAbbreviationRepository normAbbreviationRepository;
  @Autowired private DatabaseLegalForceTypeRepository legalForceTypeRepository;
  @Autowired private DatabaseRegionRepository regionRepository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;

  @MockBean private S3AsyncClient s3AsyncClient;
  @MockBean private EmailPublishService publishService;
  @MockBean private DocxConverterService docxConverterService;
  @MockBean private UserService userService;
  @MockBean private ReactiveClientRegistrationRepository clientRegistrationRepository;
  @MockBean private AttachmentService attachmentService;

  private final DocumentationOffice docOffice = buildDefaultDocOffice();
  private DocumentationOfficeDTO documentationOfficeDTO;

  @Autowired
  private DatabaseDocumentationUnitSearchRepository databaseDocumentationUnitSearchRepository;

  @BeforeEach
  void setUp() {
    documentationOfficeDTO =
        documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation());

    doReturn(Mono.just(docOffice))
        .when(userService)
        .getDocumentationOffice(
            argThat(
                (OidcUser user) -> {
                  List<String> groups = user.getAttribute("groups");
                  return Objects.requireNonNull(groups).get(0).equals("/DS");
                }));
  }

  @Test
  @Transactional
  void testLegalForce_withNormReference_withoutLegalForce() {
    DocumentationUnitDTO dto =
        DocumentationUnitDTO.builder()
            .documentNumber("1234567890123")
            .documentationOffice(documentationOfficeDTO)
            .build();
    NormAbbreviationDTO normAbbreviationDTO =
        normAbbreviationRepository
            .findById(UUID.fromString("33333333-2222-3333-4444-555555555555"))
            .get();
    NormReferenceDTO normReferenceDTO =
        NormReferenceDTO.builder()
            .normAbbreviation(normAbbreviationDTO)
            .singleNorm("single norm")
            .dateOfRelevance("1990")
            .dateOfVersion(LocalDate.of(2011, Month.APRIL, 7))
            .rank(1)
            .build();
    dto.getNormReferences().add(normReferenceDTO);
    DocumentationUnitDTO savedDTO = repository.save(dto);

    DocumentationUnitDTO result = repository.findById(savedDTO.getId()).get();

    assertThat(result.getNormReferences()).hasSize(1);
    assertThat(result.getNormReferences().get(0).getLegalForce()).isNull();
  }

  @Test
  @Transactional
  void testLegalForce_withNormReference_withLegalForce() {
    DocumentationUnitDTO dto =
        DocumentationUnitDTO.builder()
            .documentNumber("1234567890123")
            .documentationOffice(documentationOfficeDTO)
            .build();
    NormAbbreviationDTO normAbbreviationDTO =
        normAbbreviationRepository
            .findById(UUID.fromString("33333333-2222-3333-4444-555555555555"))
            .get();
    LegalForceTypeDTO legalForceTypeDTO =
        legalForceTypeRepository
            .findById(UUID.fromString("11111111-2222-3333-4444-555555555555"))
            .get();
    RegionDTO regionDTO =
        regionRepository.findById(UUID.fromString("55555555-2222-3333-4444-555555555555")).get();
    LegalForceDTO legalForceDTO =
        LegalForceDTO.builder().legalForceType(legalForceTypeDTO).region(regionDTO).build();
    NormReferenceDTO normReferenceDTO =
        NormReferenceDTO.builder()
            .normAbbreviation(normAbbreviationDTO)
            .singleNorm("single norm")
            .dateOfRelevance("1990")
            .dateOfVersion(LocalDate.of(2011, Month.APRIL, 7))
            .legalForce(legalForceDTO)
            .rank(1)
            .build();
    dto.getNormReferences().add(normReferenceDTO);
    DocumentationUnitDTO savedDTO = repository.save(dto);

    DocumentationUnitDTO result = repository.findById(savedDTO.getId()).get();

    assertThat(result.getNormReferences()).hasSize(1);
    assertThat(result.getNormReferences().get(0).getLegalForce()).isNotNull();
  }
}
