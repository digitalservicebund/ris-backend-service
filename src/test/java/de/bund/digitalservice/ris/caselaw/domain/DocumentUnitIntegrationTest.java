package de.bund.digitalservice.ris.caselaw.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitController;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresConfig;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.CourtDTO;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.CourtRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.state.StateDTO;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.state.StateRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.AutoConfigureDataR2dbc;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@WebFluxTest(controllers = {DocumentUnitController.class})
@Import({
  DocumentUnitService.class,
  DocumentUnitRepositoryImpl.class,
  FlywayConfig.class,
  PostgresConfig.class
})
@Tag("integration")
@Testcontainers(disabledWithoutDocker = true)
@WithMockUser
@AutoConfigureDataR2dbc
// This is because retryWhen(Retry.backoff... in DocumentUnitService can take up
// to ~127 seconds
@AutoConfigureWebTestClient(timeout = "150000")
public class DocumentUnitIntegrationTest {
  @Container
  static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer<>("postgres:12");

  @DynamicPropertySource
  static void registerDynamicProperties(DynamicPropertyRegistry registry) {
    registry.add("database.user", () -> postgreSQLContainer.getUsername());
    registry.add("database.password", () -> postgreSQLContainer.getPassword());
    registry.add("database.host", () -> postgreSQLContainer.getHost());
    registry.add("database.port", () -> postgreSQLContainer.getFirstMappedPort());
    registry.add("database.database", () -> postgreSQLContainer.getDatabaseName());
  }

  @MockBean S3AsyncClient s3AsyncClient;
  @MockBean EmailPublishService publishService;
  @MockBean DocumentUnitListEntryRepository listEntryRepository;

  @Autowired private WebTestClient webClient;
  @Autowired private DatabaseDocumentUnitRepository repository;
  @Autowired private FileNumberRepository fileNumberRepository;
  @Autowired private DeviatingEcliRepository deviatingEcliRepository;
  @Autowired private CourtRepository courtRepository;
  @Autowired private StateRepository stateRepository;
  @Autowired private DeviatingDecisionDateRepository deviatingDecisionDateRepository;

  @BeforeEach
  void setUp() {
    repository.deleteAll().block();
    fileNumberRepository.deleteAll().block();
    deviatingEcliRepository.deleteAll().block();
    courtRepository.deleteAll().block();
    stateRepository.deleteAll().block();
    deviatingDecisionDateRepository.deleteAll().block();
  }

  @Test
  void testForCorrectDbEntryAfterNewDocumentUnitCreation() {
    DocumentUnitCreationInfo info = new DocumentUnitCreationInfo("ABC", "D");

    webClient
        .mutateWith(csrf())
        .post()
        .uri("/api/v1/caselaw/documentunits/")
        .bodyValue(info)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().documentNumber()).startsWith("ABCD");
            });

    List<DocumentUnitDTO> list = repository.findAll().collectList().block();
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getDocumentnumber()).startsWith("ABCD");
  }

  @Test
  void testForFileNumbersDbEntryAfterUpdateByUuid() {
    UUID uuid = UUID.randomUUID();

    DocumentUnitDTO dto =
        DocumentUnitDTO.builder()
            .uuid(uuid)
            .creationtimestamp(Instant.now())
            .documentnumber("1234567890123")
            .build();

    DocumentUnitDTO savedDto = repository.save(dto).block();

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .id(savedDto.id)
            .uuid(dto.getUuid())
            .creationtimestamp(dto.getCreationtimestamp())
            .documentNumber(dto.getDocumentnumber())
            .coreData(CoreData.builder().fileNumbers(List.of("AkteX")).build())
            .texts(Texts.builder().decisionName("decisionName").build())
            .build();

    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/caselaw/documentunits/" + uuid + "/docx")
        .bodyValue(documentUnitFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().documentNumber()).isEqualTo("1234567890123");
              assertThat(response.getResponseBody().coreData().fileNumbers().get(0))
                  .isEqualTo("AkteX");
            });

    List<DocumentUnitDTO> list = repository.findAll().collectList().block();
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getDocumentnumber()).isEqualTo("1234567890123");

    List<FileNumberDTO> fileNumberEntries =
        fileNumberRepository.findAllByDocumentUnitId(list.get(0).id).collectList().block();
    assertThat(fileNumberEntries).hasSize(1);
    assertThat(fileNumberEntries.get(0).getFileNumber()).isEqualTo("AkteX");
  }

  @Test
  void testForDeviatingEcliDbEntryAfterUpdateByUuid() {
    UUID uuid = UUID.randomUUID();

    DocumentUnitDTO dto =
        DocumentUnitDTO.builder()
            .uuid(uuid)
            .creationtimestamp(Instant.now())
            .documentnumber("1234567890123")
            .build();

    DocumentUnitDTO savedDto = repository.save(dto).block();

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .id(savedDto.id)
            .uuid(dto.getUuid())
            .creationtimestamp(dto.getCreationtimestamp())
            .documentNumber(dto.getDocumentnumber())
            .coreData(CoreData.builder().deviatingEclis(List.of("ecli123", "ecli456")).build())
            .texts(Texts.builder().decisionName("decisionName").build()) // TODO why is this needed?
            .build();

    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/caselaw/documentunits/" + uuid + "/docx")
        .bodyValue(documentUnitFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().documentNumber()).isEqualTo("1234567890123");
              assertThat(response.getResponseBody().coreData().deviatingEclis().get(0))
                  .isEqualTo("ecli123");
              assertThat(response.getResponseBody().coreData().deviatingEclis().get(1))
                  .isEqualTo("ecli456");
            });

    List<DocumentUnitDTO> list = repository.findAll().collectList().block();
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getDocumentnumber()).isEqualTo("1234567890123");

    List<DeviatingEcliDTO> deviatingEclis =
        deviatingEcliRepository.findAllByDocumentUnitId(list.get(0).id).collectList().block();

    assertThat(deviatingEclis).hasSize(2);
    assertThat(deviatingEclis.get(0).getEcli()).isEqualTo("ecli123");
    assertThat(deviatingEclis.get(1).getEcli()).isEqualTo("ecli456");
  }

  @Test
  void testForDeviatingDecisionDateDbEntryAfterUpdateByUuid() {
    UUID uuid = UUID.randomUUID();

    DocumentUnitDTO dto =
        DocumentUnitDTO.builder()
            .uuid(uuid)
            .creationtimestamp(Instant.now())
            .documentnumber("1234567890123")
            .build();

    DocumentUnitDTO savedDto = repository.save(dto).block();

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .id(savedDto.id)
            .uuid(dto.getUuid())
            .creationtimestamp(dto.getCreationtimestamp())
            .documentNumber(dto.getDocumentnumber())
            .coreData(
                CoreData.builder()
                    .deviatingDecisionDates(
                        (List.of(
                            Instant.parse("2022-01-31T23:00:00Z"),
                            Instant.parse("2022-01-31T23:00:00Z"))))
                    .build())
            .texts(Texts.builder().decisionName("decisionName").build()) // TODO why is this needed?
            .build();

    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/caselaw/documentunits/" + uuid + "/docx")
        .bodyValue(documentUnitFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().documentNumber()).isEqualTo("1234567890123");
              assertThat(response.getResponseBody().coreData().deviatingDecisionDates().get(0))
                  .isEqualTo("2022-01-31T23:00:00Z");
              assertThat(response.getResponseBody().coreData().deviatingDecisionDates().get(1))
                  .isEqualTo("2022-01-31T23:00:00Z");
            });

    List<DocumentUnitDTO> list = repository.findAll().collectList().block();
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getDocumentnumber()).isEqualTo("1234567890123");

    List<DeviatingDecisionDateDTO> deviatingDecisionDates =
        deviatingDecisionDateRepository
            .findAllByDocumentUnitId(list.get(0).id)
            .collectList()
            .block();

    assertThat(deviatingDecisionDates).hasSize(2);
    assertThat(deviatingDecisionDates.get(0).getDecisiondate()).isEqualTo("2022-01-31T23:00:00Z");
    assertThat(deviatingDecisionDates.get(1).getDecisiondate()).isEqualTo("2022-01-31T23:00:00Z");
  }

  @Test
  void testRegionFilledBasedOnCourt_courtHasStateShortcut_shouldUseStateName() {
    DocumentUnit documentUnitFromFrontend =
        testRegionFilledBasedOnCourt("BE", "Berlin", "region123");

    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/caselaw/documentunits/" + documentUnitFromFrontend.uuid() + "/docx")
        .bodyValue(documentUnitFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().coreData().court().label())
                  .isEqualTo(documentUnitFromFrontend.coreData().court().label());
              assertThat(response.getResponseBody().coreData().region()).isEqualTo("Berlin");
            });
  }

  @Test
  void testRegionFilledBasedOnCourt_courtHasNoStateShortcut_shouldUseCourtRegion() {
    DocumentUnit documentUnitFromFrontend = testRegionFilledBasedOnCourt(null, null, "region123");

    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/caselaw/documentunits/" + documentUnitFromFrontend.uuid() + "/docx")
        .bodyValue(documentUnitFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().coreData().court().label())
                  .isEqualTo(documentUnitFromFrontend.coreData().court().label());
              assertThat(response.getResponseBody().coreData().region()).isEqualTo("region123");
            });
  }

  @Test
  void testRegionFilledBasedOnCourt_courtHasNoStateShortcutAndNoRegion_shouldLeaveEmpty() {
    DocumentUnit documentUnitFromFrontend = testRegionFilledBasedOnCourt(null, null, null);

    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/caselaw/documentunits/" + documentUnitFromFrontend.uuid() + "/docx")
        .bodyValue(documentUnitFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().coreData().court().label())
                  .isEqualTo(documentUnitFromFrontend.coreData().court().label());
              assertThat(response.getResponseBody().coreData().region()).isNull();
            });
  }

  private DocumentUnit testRegionFilledBasedOnCourt(
      String stateShortcutInCourtAndState, String stateNameInState, String regionInCourt) {
    CourtDTO courtDTO =
        CourtDTO.builder()
            .courttype("ABC")
            .courtlocation("location123")
            .federalstate(stateShortcutInCourtAndState)
            .region(regionInCourt)
            .build();
    courtRepository.save(courtDTO).block();
    stateRepository
        .save(
            StateDTO.builder()
                .jurisshortcut(stateShortcutInCourtAndState)
                .label(stateNameInState)
                .build())
        .block();

    DocumentUnitDTO dto =
        DocumentUnitDTO.builder()
            .uuid(UUID.randomUUID())
            .creationtimestamp(Instant.now())
            .documentnumber("1234567890123")
            .courtType(courtDTO.getCourttype())
            .courtLocation(courtDTO.getCourtlocation())
            .build();

    DocumentUnitDTO savedDto = repository.save(dto).block();
    assert savedDto != null;

    return DocumentUnit.builder()
        .id(savedDto.id)
        .uuid(dto.getUuid())
        .creationtimestamp(dto.getCreationtimestamp())
        .documentNumber(dto.getDocumentnumber())
        .coreData(
            CoreData.builder()
                .court(
                    new Court(
                        savedDto.getCourtType(),
                        savedDto.getCourtLocation(),
                        savedDto.getCourtType() + " " + savedDto.getCourtLocation(),
                        ""))
                .build())
        .texts(Texts.builder().decisionName("decisionName").build())
        .build();
  }
}
