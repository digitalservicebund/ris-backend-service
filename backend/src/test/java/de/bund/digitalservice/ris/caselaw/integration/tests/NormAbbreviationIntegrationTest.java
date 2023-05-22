package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import de.bund.digitalservice.ris.caselaw.adapter.NormAbbreviationController;
import de.bund.digitalservice.ris.caselaw.adapter.NormAbbreviationService;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseDocumentCategoryRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseDocumentTypeNewRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseNormAbbreviationDocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseNormAbbreviationRegionRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseNormAbbreviationRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseRegionRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DocumentCategoryDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DocumentTypeNewDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.NormAbbreviationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.NormAbbreviationDocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.NormAbbreviationRegionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.PostgresNormAbbreviationRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.RegionDTO;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresConfig;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.DocumentTypeNew;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.NormAbbreviation;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.NormAbbreviation.NormAbbreviationBuilder;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.Region;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@RISIntegrationTest(
    imports = {
      NormAbbreviationService.class,
      FlywayConfig.class,
      PostgresConfig.class,
      PostgresNormAbbreviationRepositoryImpl.class
    },
    controllers = {NormAbbreviationController.class})
class NormAbbreviationIntegrationTest {
  private static final UUID NORM_ABBREVIATION_UUID =
      UUID.fromString("AAAAAAAA-2222-3333-4444-555555555555");
  private static final UUID DOCUMENT_TYPE_UUID_1 =
      UUID.fromString("D1D1D1D1-2222-3333-4444-555555555555");
  private static final UUID DOCUMENT_TYPE_UUID_2 =
      UUID.fromString("D2D2D2D2-2222-3333-4444-555555555555");
  private static final UUID DOCUMENT_CATEGORY_UUID_1 =
      UUID.fromString("C1C1C1C1-2222-3333-4444-555555555555");
  private static final UUID DOCUMENT_CATEGORY_UUID_2 =
      UUID.fromString("C2C2C2C2-2222-3333-4444-555555555555");
  private static final UUID REGION_UUID_1 = UUID.fromString("E1E1E1E1-2222-3333-4444-555555555555");
  private static final UUID REGION_UUID_2 = UUID.fromString("E2E2E2E2-2222-3333-4444-555555555555");

  @Container
  static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:12");

  @DynamicPropertySource
  static void registerDynamicProperties(DynamicPropertyRegistry registry) {
    registry.add("database.user", () -> postgreSQLContainer.getUsername());
    registry.add("database.password", () -> postgreSQLContainer.getPassword());
    registry.add("database.host", () -> postgreSQLContainer.getHost());
    registry.add("database.port", () -> postgreSQLContainer.getFirstMappedPort());
    registry.add("database.database", () -> postgreSQLContainer.getDatabaseName());
  }

  @Autowired private WebTestClient webClient;

  @Autowired private DatabaseNormAbbreviationRepository repository;
  @Autowired private DatabaseDocumentTypeNewRepository documentTypeRepository;
  @Autowired private DatabaseDocumentCategoryRepository documentCategoryRepository;
  @Autowired private DatabaseRegionRepository regionRepository;

  @Autowired
  private DatabaseNormAbbreviationDocumentTypeRepository normAbbreviationDocumentTypeRepository;

  @Autowired private DatabaseNormAbbreviationRegionRepository normAbbreviationRegionRepository;
  @MockBean private S3AsyncClient s3AsyncClient;
  @MockBean private EmailPublishService publishService;

  @AfterEach
  void cleanUp() {
    normAbbreviationRegionRepository.deleteAll().block();
    normAbbreviationDocumentTypeRepository.deleteAll().block();
    repository.deleteAll().block();
    documentTypeRepository.deleteAll().block();
    documentCategoryRepository.deleteAll().block();
    regionRepository.deleteAll().block();
  }

  @Test
  void testGetNormAbbreviationById_allValuesFilled() {
    generateLookupValues();
    linkDocumentType(DOCUMENT_TYPE_UUID_1);
    linkRegion(REGION_UUID_1);

    NormAbbreviation expectedNormAbbreviation =
        new NormAbbreviationTestBuilder()
            .getExpectedNormAbbreviation()
            .addDocumentType(DOCUMENT_TYPE_UUID_1, 'L')
            .addRegion(REGION_UUID_1)
            .build();

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/caselaw/normabbreviation/" + NORM_ABBREVIATION_UUID)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(NormAbbreviation.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isEqualTo(expectedNormAbbreviation);
            });
  }

  @Test
  void testGetNormAbbreviationById_withoutLinkedDocumentType() {
    generateLookupValues();
    linkRegion(REGION_UUID_1);

    NormAbbreviation expectedNormAbbreviation =
        new NormAbbreviationTestBuilder()
            .getExpectedNormAbbreviation()
            .addRegion(REGION_UUID_1)
            .build();

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/caselaw/normabbreviation/" + NORM_ABBREVIATION_UUID)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(NormAbbreviation.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isEqualTo(expectedNormAbbreviation);
            });
  }

  @Test
  void testGetNormAbbreviationById_withoutLinkedRegion() {
    generateLookupValues();
    linkDocumentType(DOCUMENT_TYPE_UUID_1);

    NormAbbreviation expectedNormAbbreviation =
        new NormAbbreviationTestBuilder()
            .getExpectedNormAbbreviation()
            .addDocumentType(DOCUMENT_TYPE_UUID_1, 'L')
            .build();

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/caselaw/normabbreviation/" + NORM_ABBREVIATION_UUID)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(NormAbbreviation.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isEqualTo(expectedNormAbbreviation);
            });
  }

  @Test
  void testGetNormAbbreviationById_withoutLinkedDocumentTypeAndRegion() {
    generateLookupValues();

    NormAbbreviation expectedNormAbbreviation =
        new NormAbbreviationTestBuilder().getExpectedNormAbbreviation().build();

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/caselaw/normabbreviation/" + NORM_ABBREVIATION_UUID)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(NormAbbreviation.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isEqualTo(expectedNormAbbreviation);
            });
  }

  @Test
  void testGetNormAbbreviationById_withTwoLinkedDocumentTypesAndTwoRegions() {
    generateLookupValues();
    linkDocumentType(DOCUMENT_TYPE_UUID_1);
    linkDocumentType(DOCUMENT_TYPE_UUID_2);
    linkRegion(REGION_UUID_1);
    linkRegion(REGION_UUID_2);

    NormAbbreviation expectedNormAbbreviation =
        new NormAbbreviationTestBuilder()
            .getExpectedNormAbbreviation()
            .addDocumentType(DOCUMENT_TYPE_UUID_1, 'L')
            .addDocumentType(DOCUMENT_TYPE_UUID_2, 'M')
            .addRegion(REGION_UUID_1)
            .addRegion(REGION_UUID_2)
            .build();

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/caselaw/normabbreviation/" + NORM_ABBREVIATION_UUID)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(NormAbbreviation.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isEqualTo(expectedNormAbbreviation);
            });
  }

  private void generateLookupValues() {
    NormAbbreviationDTO normAbbreviationDTO =
        NormAbbreviationDTO.builder()
            .id(NORM_ABBREVIATION_UUID)
            .newEntity(true)
            .abbreviation("norm abbreviation")
            .decisionDate(LocalDate.of(2023, Month.MAY, 19))
            .documentId(1234)
            .documentNumber("document number")
            .officialLetterAbbreviation("official letter abbreviation")
            .officialLongTitle("official long title")
            .officialShortTitle("official short title")
            .source('S')
            .build();
    repository.save(normAbbreviationDTO).block();

    DocumentCategoryDTO documentCategoryDTO =
        DocumentCategoryDTO.builder()
            .id(DOCUMENT_CATEGORY_UUID_1)
            .newEntity(true)
            .label('L')
            .build();
    documentCategoryRepository.save(documentCategoryDTO).block();

    documentCategoryDTO =
        DocumentCategoryDTO.builder()
            .id(DOCUMENT_CATEGORY_UUID_2)
            .newEntity(true)
            .label('M')
            .build();
    documentCategoryRepository.save(documentCategoryDTO).block();

    DocumentTypeNewDTO documentTypeNewDTO =
        DocumentTypeNewDTO.builder()
            .id(DOCUMENT_TYPE_UUID_1)
            .newEntity(true)
            .abbreviation("document type abbreviation 1")
            .label("document type label 1")
            .multiple(false)
            .superLabel1("super label 11")
            .superLabel2("super label 21")
            .documentCategoryId(DOCUMENT_CATEGORY_UUID_1)
            .build();
    documentTypeRepository.save(documentTypeNewDTO).block();

    documentTypeNewDTO =
        DocumentTypeNewDTO.builder()
            .id(DOCUMENT_TYPE_UUID_2)
            .newEntity(true)
            .abbreviation("document type abbreviation 2")
            .label("document type label 2")
            .multiple(false)
            .superLabel1("super label 12")
            .superLabel2("super label 22")
            .documentCategoryId(DOCUMENT_CATEGORY_UUID_2)
            .build();
    documentTypeRepository.save(documentTypeNewDTO).block();

    RegionDTO regionDTO =
        RegionDTO.builder()
            .id(REGION_UUID_1)
            .newEntity(true)
            .code("region code 1")
            .label("region label 1")
            .build();
    regionRepository.save(regionDTO).block();

    regionDTO =
        RegionDTO.builder()
            .id(REGION_UUID_2)
            .newEntity(true)
            .code("region code 2")
            .label("region label 2")
            .build();
    regionRepository.save(regionDTO).block();
  }

  private void linkDocumentType(UUID documentTypeUUID) {
    NormAbbreviationDocumentTypeDTO normAbbreviationDocumentTypeDTO =
        NormAbbreviationDocumentTypeDTO.builder()
            .newEntity(true)
            .normAbbreviationId(NORM_ABBREVIATION_UUID)
            .documentTypeId(documentTypeUUID)
            .build();
    normAbbreviationDocumentTypeRepository.save(normAbbreviationDocumentTypeDTO).block();
  }

  private void linkRegion(UUID regionUUID) {
    NormAbbreviationRegionDTO normAbbreviationRegionDTO =
        NormAbbreviationRegionDTO.builder()
            .newEntity(true)
            .normAbbreviationId(NORM_ABBREVIATION_UUID)
            .regionId(regionUUID)
            .build();
    normAbbreviationRegionRepository.save(normAbbreviationRegionDTO).block();
  }

  private class NormAbbreviationTestBuilder {

    private final NormAbbreviationBuilder builder;
    private final List<Region> regions;
    private final List<DocumentTypeNew> documentTypes;

    private NormAbbreviationTestBuilder() {
      this.builder = NormAbbreviation.builder();
      regions = new ArrayList<>();
      documentTypes = new ArrayList<>();
    }

    private NormAbbreviationTestBuilder getExpectedNormAbbreviation() {
      builder
          .id(NORM_ABBREVIATION_UUID)
          .abbreviation("norm abbreviation")
          .documentId(1234)
          .documentNumber("document number")
          .decisionDate(
              LocalDate.of(2023, Month.MAY, 19)
                  .atStartOfDay()
                  .atZone(ZoneId.of("Europe/Berlin"))
                  .toInstant())
          .officialLetterAbbreviation("official letter abbreviation")
          .officialShortTitle("official short title")
          .officialLongTitle("official long title")
          .source('S');
      return this;
    }

    private NormAbbreviationTestBuilder addRegion(UUID regionUuid) {
      RegionDTO regionDTO = regionRepository.findById(regionUuid).block();
      if (regionDTO == null) {
        return this;
      }

      regions.add(Region.builder().code(regionDTO.getCode()).label(regionDTO.getLabel()).build());

      return this;
    }

    private NormAbbreviationTestBuilder addDocumentType(
        UUID documentTypeUuid, Character categoryLabel) {
      DocumentTypeNewDTO documentTypeNewDTO =
          documentTypeRepository.findById(documentTypeUuid).block();
      if (documentTypeNewDTO == null) {
        return this;
      }

      documentTypes.add(
          DocumentTypeNew.builder()
              .abbreviation(documentTypeNewDTO.getAbbreviation())
              .label(documentTypeNewDTO.getLabel())
              .multiple(documentTypeNewDTO.isMultiple())
              .superLabel1(documentTypeNewDTO.getSuperLabel1())
              .superLabel2(documentTypeNewDTO.getSuperLabel2())
              .categoryLabel(categoryLabel)
              .build());
      return this;
    }

    private NormAbbreviation build() {
      return builder.regions(regions).documentTypes(documentTypes).build();
    }
  }
}
