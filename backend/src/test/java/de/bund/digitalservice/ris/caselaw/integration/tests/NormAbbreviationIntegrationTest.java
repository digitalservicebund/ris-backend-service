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
  private static final UUID NORM_ABBREVIATION_UUID_1 =
      UUID.fromString("A1A1A1A1-2222-3333-4444-555555555555");
  private static final UUID NORM_ABBREVIATION_UUID_2 =
      UUID.fromString("A2A2A2A2-2222-3333-4444-555555555555");
  private static final UUID NORM_ABBREVIATION_UUID_3 =
      UUID.fromString("A3A3A3A3-2222-3333-4444-555555555555");
  private static final UUID NORM_ABBREVIATION_UUID_4 =
      UUID.fromString("A4A4A4A4-2222-3333-4444-555555555555");
  private static final UUID NORM_ABBREVIATION_UUID_5 =
      UUID.fromString("A5A5A5A5-2222-3333-4444-555555555555");
  private static final UUID NORM_ABBREVIATION_UUID_6 =
      UUID.fromString("A6A6A6A6-2222-3333-4444-555555555555");
  private static final UUID NORM_ABBREVIATION_UUID_7 =
      UUID.fromString("A7A7A7A7-2222-3333-4444-555555555555");
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
        .uri("/api/v1/caselaw/normabbreviation/" + NORM_ABBREVIATION_UUID_1)
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
        .uri("/api/v1/caselaw/normabbreviation/" + NORM_ABBREVIATION_UUID_1)
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
        .uri("/api/v1/caselaw/normabbreviation/" + NORM_ABBREVIATION_UUID_1)
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
        .uri("/api/v1/caselaw/normabbreviation/" + NORM_ABBREVIATION_UUID_1)
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
        .uri("/api/v1/caselaw/normabbreviation/" + NORM_ABBREVIATION_UUID_1)
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
  void testGetNormAbbreviationBySearchQuery() {
    generateLookupValues();

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/caselaw/normabbreviation?q=search query")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(NormAbbreviation[].class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody())
                  .extracting("id")
                  .containsExactly(NORM_ABBREVIATION_UUID_2);
            });
  }

  @Test
  void testGetNormAbbreviationBySearchQuery_returnInTheRightOrder() {
    generateLookupValues();

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/caselaw/normabbreviation?q=Search")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(NormAbbreviation[].class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody())
                  .extracting("id")
                  .containsExactly(
                      NORM_ABBREVIATION_UUID_6, NORM_ABBREVIATION_UUID_5, NORM_ABBREVIATION_UUID_7);
            });
  }

  private void generateLookupValues() {
    NormAbbreviationDTO normAbbreviationDTO =
        NormAbbreviationDTO.builder()
            .id(NORM_ABBREVIATION_UUID_1)
            .newEntity(true)
            .abbreviation("norm abbreviation 1")
            .decisionDate(LocalDate.of(2023, Month.MAY, 19))
            .documentId(1234)
            .documentNumber("document number 1")
            .officialLetterAbbreviation("official letter abbreviation 1")
            .officialLongTitle("official long title 1")
            .officialShortTitle("official short title 1")
            .source('S')
            .build();
    repository.save(normAbbreviationDTO).block();

    normAbbreviationDTO =
        NormAbbreviationDTO.builder()
            .id(NORM_ABBREVIATION_UUID_2)
            .newEntity(true)
            .abbreviation("search query at the beginning")
            .decisionDate(LocalDate.of(2023, Month.MAY, 20))
            .documentId(2345)
            .documentNumber("document number 2")
            .officialLetterAbbreviation("official letter abbreviation 2")
            .officialLongTitle("official long title 2")
            .officialShortTitle("official short title 2")
            .source('T')
            .build();
    repository.save(normAbbreviationDTO).block();

    normAbbreviationDTO =
        NormAbbreviationDTO.builder()
            .id(NORM_ABBREVIATION_UUID_3)
            .newEntity(true)
            .abbreviation("in the middle the search query is located")
            .decisionDate(LocalDate.of(2023, Month.MAY, 21))
            .documentId(3456)
            .documentNumber("document number 3")
            .officialLetterAbbreviation("official letter abbreviation 3")
            .officialLongTitle("official long title 3")
            .officialShortTitle("official short title 3")
            .source('U')
            .build();
    repository.save(normAbbreviationDTO).block();

    normAbbreviationDTO =
        NormAbbreviationDTO.builder()
            .id(NORM_ABBREVIATION_UUID_4)
            .newEntity(true)
            .abbreviation("SeaRch QueRY not in the right case")
            .decisionDate(LocalDate.of(2023, Month.MAY, 22))
            .documentId(4567)
            .documentNumber("document number 4")
            .officialLetterAbbreviation("official letter abbreviation 4")
            .officialLongTitle("official long title 4")
            .officialShortTitle("official short title 4")
            .source('V')
            .build();
    repository.save(normAbbreviationDTO).block();

    normAbbreviationDTO =
        NormAbbreviationDTO.builder()
            .id(NORM_ABBREVIATION_UUID_5)
            .newEntity(true)
            .abbreviation("Search B")
            .decisionDate(LocalDate.of(2023, Month.MAY, 23))
            .documentId(5678)
            .documentNumber("document number 5")
            .officialLetterAbbreviation("official letter abbreviation 5")
            .officialLongTitle("official long title 5")
            .officialShortTitle("official short title 5")
            .source('W')
            .build();
    repository.save(normAbbreviationDTO).block();

    normAbbreviationDTO =
        NormAbbreviationDTO.builder()
            .id(NORM_ABBREVIATION_UUID_6)
            .newEntity(true)
            .abbreviation("Search A")
            .decisionDate(LocalDate.of(2023, Month.MAY, 24))
            .documentId(6789)
            .documentNumber("document number 6")
            .officialLetterAbbreviation("official letter abbreviation 6")
            .officialLongTitle("official long title 6")
            .officialShortTitle("official short title 6")
            .source('X')
            .build();
    repository.save(normAbbreviationDTO).block();

    normAbbreviationDTO =
        NormAbbreviationDTO.builder()
            .id(NORM_ABBREVIATION_UUID_7)
            .newEntity(true)
            .abbreviation("Search C")
            .decisionDate(LocalDate.of(2023, Month.MAY, 25))
            .documentId(7890)
            .documentNumber("document number 7")
            .officialLetterAbbreviation("official letter abbreviation 7")
            .officialLongTitle("official long title 7")
            .officialShortTitle("official short title 7")
            .source('Y')
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
            .normAbbreviationId(NORM_ABBREVIATION_UUID_1)
            .documentTypeId(documentTypeUUID)
            .build();
    normAbbreviationDocumentTypeRepository.save(normAbbreviationDocumentTypeDTO).block();
  }

  private void linkRegion(UUID regionUUID) {
    NormAbbreviationRegionDTO normAbbreviationRegionDTO =
        NormAbbreviationRegionDTO.builder()
            .newEntity(true)
            .normAbbreviationId(NORM_ABBREVIATION_UUID_1)
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
          .id(NORM_ABBREVIATION_UUID_1)
          .abbreviation("norm abbreviation 1")
          .documentId(1234)
          .documentNumber("document number 1")
          .decisionDate(
              LocalDate.of(2023, Month.MAY, 19)
                  .atStartOfDay()
                  .atZone(ZoneId.of("Europe/Berlin"))
                  .toInstant())
          .officialLetterAbbreviation("official letter abbreviation 1")
          .officialShortTitle("official short title 1")
          .officialLongTitle("official long title 1")
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
