package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentNumberService;
import de.bund.digitalservice.ris.caselaw.adapter.DocumentUnitController;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDeviatingDecisionDateRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseIncorrectCourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DeviatingDecisionDateDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DeviatingEcliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DeviatingEcliRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.FileNumberRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.IncorrectCourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresDocumentUnitListEntryRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresDocumentUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.LegalEffect;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.StateDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.StateRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.proceedingdecision.DatabaseProceedingDecisionRepository;
import de.bund.digitalservice.ris.caselaw.config.FlywayConfig;
import de.bund.digitalservice.ris.caselaw.config.PostgresConfig;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitCreationInfo;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import de.bund.digitalservice.ris.caselaw.domain.Texts;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.time.Instant;
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
      DocumentUnitService.class,
      DatabaseDocumentNumberService.class,
      PostgresDocumentUnitRepositoryImpl.class,
      PostgresDocumentUnitListEntryRepositoryImpl.class,
      FlywayConfig.class,
      PostgresConfig.class
    },
    controllers = {DocumentUnitController.class})
class DocumentUnitIntegrationTest {
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
  @Autowired private DatabaseDocumentUnitRepository repository;
  @Autowired private DatabaseProceedingDecisionRepository previousDecisionRepository;
  @Autowired private FileNumberRepository fileNumberRepository;
  @Autowired private DeviatingEcliRepository deviatingEcliRepository;
  @Autowired private CourtRepository courtRepository;
  @Autowired private StateRepository stateRepository;
  @Autowired private DatabaseDeviatingDecisionDateRepository deviatingDecisionDateRepository;
  @Autowired private DocumentTypeRepository documentTypeRepository;
  @Autowired private DatabaseIncorrectCourtRepository incorrectCourtRepository;

  @MockBean private S3AsyncClient s3AsyncClient;
  @MockBean private EmailPublishService publishService;

  @AfterEach
  void cleanUp() {
    fileNumberRepository.deleteAll().block();
    deviatingEcliRepository.deleteAll().block();
    previousDecisionRepository.deleteAll().block();
    courtRepository.deleteAll().block();
    stateRepository.deleteAll().block();
    deviatingDecisionDateRepository.deleteAll().block();
    incorrectCourtRepository.deleteAll().block();
    repository.deleteAll().block();
    documentTypeRepository.deleteAll().block();
  }
  /*
  @Test
  void testGetDocumentUnit_withPreviousDecisions() {
    UUID documentUnitUuid1 = UUID.randomUUID();
    DocumentUnitDTO documentUnitDTO =
        DocumentUnitDTO.builder()
            .uuid(documentUnitUuid1)
            .documentnumber("docnr12345678")
            .creationtimestamp(Instant.now())
            .build();
    DocumentUnitDTO savedDocumentUnit = repository.save(documentUnitDTO).block();
    List<ProceedingDecisionDTO> proceedingDecisionDTOS =
        List.of(
            ProceedingDecisionDTO.builder().id(savedDocumentUnit.getId()).build(),
            ProceedingDecisionDTO.builder().id(savedDocumentUnit.getId()).build());
    previousDecisionRepository.saveAll(proceedingDecisionDTOS).collectList().block();

    UUID documentUnitUuid2 = UUID.randomUUID();
    documentUnitDTO =
        DocumentUnitDTO.builder()
            .uuid(documentUnitUuid2)
            .documentnumber("docnr23456789")
            .creationtimestamp(Instant.now())
            .build();
    savedDocumentUnit = repository.save(documentUnitDTO).block();
    proceedingDecisionDTOS =
        List.of(
            ProceedingDecisionDTO.builder().id(savedDocumentUnit.getId()).build(),
            ProceedingDecisionDTO.builder().id(savedDocumentUnit.getId()).build());
    previousDecisionRepository.saveAll(proceedingDecisionDTOS).collectList().block();

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/caselaw/documentunits/docnr12345678")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              DocumentUnit responseBody = response.getResponseBody();
              assertThat(responseBody.uuid()).isEqualTo(documentUnitUuid1);
              assertThat(responseBody.proceedingDecisions()).hasSize(2);
            });
  }*/
  /*
  @Test
  void testUpdateDocumentUnit_withPreviousDecisions() {
    UUID documentUnitUuid1 = UUID.randomUUID();
    DocumentUnitDTO documentUnitDTO =
        DocumentUnitDTO.builder()
            .uuid(documentUnitUuid1)
            .documentnumber("docnr12345678")
            .creationtimestamp(Instant.now())
            .build();
    DocumentUnitDTO savedDocumentUnit = repository.save(documentUnitDTO).block();
    List<ProceedingDecisionDTO> proceedingDecisionDTOS =
        List.of(
            ProceedingDecisionDTO.builder().id(savedDocumentUnit.getId()).build(),
            ProceedingDecisionDTO.builder().id(savedDocumentUnit.getId()).build());
    previousDecisionRepository.saveAll(proceedingDecisionDTOS).collectList().block();

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(documentUnitUuid1)
            .documentNumber("newdocnumber12")
            .creationtimestamp(Instant.now())
            .proceedingDecisions(
                List.of(
                    ProceedingDecision.builder()
                        .court(new Court("courtType", "courtPlace", "courtLabel", null))
                        .date(Instant.parse("2020-05-06T00:00:00Z"))
                        .fileNumber("prev1")
                        .build(),
                    ProceedingDecision.builder()
                        .court(new Court("courtType", "courtPlace", "courtLabel", null))
                        .date(Instant.parse("2020-05-06T00:00:00Z"))
                        .fileNumber("prev2")
                        .build()))
            .build();

    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/caselaw/documentunits/" + documentUnitUuid1)
        .bodyValue(documentUnit)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              DocumentUnit responseBody = response.getResponseBody();
              assertThat(responseBody).isNotNull();
              assertThat(responseBody.uuid()).isEqualTo(documentUnitUuid1);
              assertThat(responseBody.proceedingDecisions()).hasSize(2);
            });
  }*/

  /*
  @Test
  void testUpdateDocumentUnit_withPreviousDecisionToInsertToDeleteAndToUpdate() {
    UUID documentUnitUuid1 = UUID.randomUUID();
    DocumentUnitDTO documentUnitDTO =
        DocumentUnitDTO.builder()
            .uuid(documentUnitUuid1)
            .documentnumber("docnr12345678")
            .creationtimestamp(Instant.now())
            .build();
    DocumentUnitDTO savedDocumentUnit = repository.save(documentUnitDTO).block();
    List<ProceedingDecisionDTO> proceedingDecisionDTOS =
        List.of(
            ProceedingDecisionDTO.builder().id(savedDocumentUnit.getId()).build(),
            ProceedingDecisionDTO.builder().id(savedDocumentUnit.getId()).build());
    previousDecisionRepository.saveAll(proceedingDecisionDTOS).collectList().block();

    DocumentUnit documentUnit =
        DocumentUnit.builder()
            .uuid(documentUnitUuid1)
            .documentNumber("docnr12345678")
            .creationtimestamp(Instant.now())
            .proceedingDecisions(
                List.of(
                    ProceedingDecision.builder()
                        .uuid(UUID.randomUUID())
                        .court(new Court("courtType", "courtPlace", "courtLabel", null))
                        .date(Instant.parse("2020-05-06T00:00:00Z"))
                        .fileNumber("prev1")
                        .documentType(
                            DocumentType.builder()
                                .jurisShortcut("category")
                                .label("category123")
                                .build())
                        .build()))
            .build();

    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/caselaw/documentunits/" + documentUnitUuid1)
        .bodyValue(documentUnit)
        .exchange()
        .expectStatus()
        .isOk();

    List<DocumentUnitDTO> documentUnitDTOs = repository.findAll().collectList().block();
    assertThat(documentUnitDTOs).hasSize(1);

    proceedingDecisionDTOS = previousDecisionRepository.findAll().collectList().block();
    assertThat(proceedingDecisionDTOS).hasSize(1);
    assertThat(proceedingDecisionDTOS)
        .extracting("id", "fileNumber")
        .containsExactly(tuple(1L, "prev1"));
  }*/

  @Test
  void testForCorrectDbEntryAfterNewDocumentUnitCreation() {
    DocumentUnitCreationInfo info = new DocumentUnitCreationInfo("ABC", "D");

    webClient
        .mutateWith(csrf())
        .post()
        .uri("/api/v1/caselaw/documentunits")
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
            .uuid(dto.getUuid())
            .creationtimestamp(dto.getCreationtimestamp())
            .documentNumber(dto.getDocumentnumber())
            .coreData(CoreData.builder().fileNumbers(List.of("AkteX")).build())
            .texts(Texts.builder().decisionName("decisionName").build())
            .build();

    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/caselaw/documentunits/" + uuid)
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
        fileNumberRepository.findAllByDocumentUnitId(list.get(0).getId()).collectList().block();
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
            .uuid(dto.getUuid())
            .creationtimestamp(dto.getCreationtimestamp())
            .documentNumber(dto.getDocumentnumber())
            .coreData(CoreData.builder().deviatingEclis(List.of("ecli123", "ecli456")).build())
            .texts(Texts.builder().decisionName("decisionName").build()) // TODO why is this needed?
            .build();

    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/caselaw/documentunits/" + uuid)
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
        deviatingEcliRepository.findAllByDocumentUnitId(list.get(0).getId()).collectList().block();

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

    repository.save(dto).block();

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
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
        .uri("/api/v1/caselaw/documentunits/" + uuid)
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
            .findAllByDocumentUnitId(list.get(0).getId())
            .collectList()
            .block();

    assertThat(deviatingDecisionDates).hasSize(2);
    assertThat(deviatingDecisionDates.get(0).decisionDate()).isEqualTo("2022-01-31T23:00:00Z");
    assertThat(deviatingDecisionDates.get(1).decisionDate()).isEqualTo("2022-01-31T23:00:00Z");
  }

  @Test
  void testUpdate_withIncorrectCourts_shouldHaveIncorrectCourtsSavedInDB() {
    UUID uuid = UUID.randomUUID();

    DocumentUnitDTO dto =
        DocumentUnitDTO.builder()
            .uuid(uuid)
            .creationtimestamp(Instant.now())
            .documentnumber("1234567890123")
            .build();

    DocumentUnitDTO savedDto = repository.save(dto).block();

    IncorrectCourtDTO incorrectCourtDTO =
        IncorrectCourtDTO.builder()
            .documentUnitId(savedDto.getId())
            .court("incorrectCourt1")
            .build();
    incorrectCourtRepository.save(incorrectCourtDTO).block();
    incorrectCourtDTO =
        IncorrectCourtDTO.builder()
            .documentUnitId(savedDto.getId())
            .court("incorrectCourt2")
            .build();
    incorrectCourtRepository.save(incorrectCourtDTO).block();

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(dto.getUuid())
            .creationtimestamp(dto.getCreationtimestamp())
            .documentNumber(dto.getDocumentnumber())
            .coreData(
                CoreData.builder()
                    .incorrectCourts(
                        List.of("incorrectCourt1", "incorrectCourt3", "incorrectCourt4"))
                    .build())
            .texts(Texts.builder().decisionName("decisionName").build()) // TODO why is this needed?
            .build();

    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/caselaw/documentunits/" + uuid)
        .bodyValue(documentUnitFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().documentNumber()).isEqualTo("1234567890123");
              assertThat(response.getResponseBody().coreData()).isNotNull();
              assertThat(response.getResponseBody().coreData().incorrectCourts()).hasSize(3);
              assertThat(response.getResponseBody().coreData().incorrectCourts())
                  .containsExactly("incorrectCourt1", "incorrectCourt3", "incorrectCourt4");
            });

    List<DocumentUnitDTO> list = repository.findAll().collectList().block();
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getDocumentnumber()).isEqualTo("1234567890123");

    List<IncorrectCourtDTO> incorrectCourtDTOs =
        incorrectCourtRepository.findAllByDocumentUnitId(list.get(0).getId()).collectList().block();

    assertThat(incorrectCourtDTOs).hasSize(3);
    assertThat(incorrectCourtDTOs)
        .extracting("court")
        .containsExactly("incorrectCourt1", "incorrectCourt3", "incorrectCourt4");
  }

  @Test
  void testRegionFilledBasedOnCourt_courtHasStateShortcut_shouldUseStateName() {
    DocumentUnit documentUnitFromFrontend =
        testRegionFilledBasedOnCourt("BE", "Berlin", "region123", false);

    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/caselaw/documentunits/" + documentUnitFromFrontend.uuid())
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
    DocumentUnit documentUnitFromFrontend =
        testRegionFilledBasedOnCourt(null, null, "region123", false);

    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/caselaw/documentunits/" + documentUnitFromFrontend.uuid())
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
    DocumentUnit documentUnitFromFrontend = testRegionFilledBasedOnCourt(null, null, null, false);

    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/caselaw/documentunits/" + documentUnitFromFrontend.uuid())
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

  @Test
  void testDontSetRegionIfCourtHasNotChanged() {
    DocumentUnit documentUnitFromFrontend =
        testRegionFilledBasedOnCourt("BY", "Bayern", "region123", true);

    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/caselaw/documentunits/" + documentUnitFromFrontend.uuid())
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
      String stateShortcutInCourtAndState,
      String stateNameInState,
      String regionInCourt,
      boolean sameCourt) {

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

    DocumentUnitDTO.DocumentUnitDTOBuilder builder =
        DocumentUnitDTO.builder()
            .uuid(UUID.randomUUID())
            .creationtimestamp(Instant.now())
            .documentnumber("1234567890123");
    if (sameCourt) {
      builder.courtType(courtDTO.getCourttype()).courtLocation(courtDTO.getCourtlocation());
    } else {
      builder.courtType("courttype").courtLocation("courtlocation");
    }
    DocumentUnitDTO dto = builder.build();

    DocumentUnitDTO savedDto = repository.save(dto).block();
    assert savedDto != null;

    Court court = null;
    if (sameCourt) {
      court =
          new Court(
              savedDto.getCourtType(),
              savedDto.getCourtLocation(),
              savedDto.getCourtType() + " " + savedDto.getCourtLocation(),
              "");
    } else {
      court =
          new Court(
              courtDTO.getCourttype(),
              courtDTO.getCourtlocation(),
              courtDTO.getCourttype() + " " + courtDTO.getCourtlocation(),
              "");
    }

    return DocumentUnit.builder()
        .uuid(dto.getUuid())
        .creationtimestamp(dto.getCreationtimestamp())
        .documentNumber(dto.getDocumentnumber())
        .coreData(CoreData.builder().court(court).build())
        .texts(Texts.builder().decisionName("decisionName").build())
        .build();
  }

  @Test
  void testDocumentTypeToSetIdFromLookuptable() {
    DocumentTypeDTO documentTypeDTO =
        DocumentTypeDTO.builder()
            .changeIndicator('c')
            .jurisShortcut("ABC")
            .documentType('R')
            .label("ABC123")
            .build();
    documentTypeRepository.save(documentTypeDTO).block();

    DocumentUnitDTO dto =
        DocumentUnitDTO.builder()
            .uuid(UUID.randomUUID())
            .creationtimestamp(Instant.now())
            .documentnumber("1234567890123")
            .build();
    repository.save(dto).block();

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(dto.getUuid())
            .creationtimestamp(dto.getCreationtimestamp())
            .documentNumber(dto.getDocumentnumber())
            .coreData(
                CoreData.builder()
                    .documentType(
                        DocumentType.builder()
                            .jurisShortcut(documentTypeDTO.getJurisShortcut())
                            .label(documentTypeDTO.getLabel())
                            .build())
                    .build())
            .build();

    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/caselaw/documentunits/" + documentUnitFromFrontend.uuid())
        .bodyValue(documentUnitFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().coreData().documentType().label())
                  .isEqualTo(documentTypeDTO.getLabel());
              assertThat(response.getResponseBody().coreData().documentType().jurisShortcut())
                  .isEqualTo(documentTypeDTO.getJurisShortcut());
            });

    List<DocumentUnitDTO> list = repository.findAll().collectList().block();
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getDocumentTypeId()).isEqualTo(1L);
    assertThat(list.get(0).getDocumentTypeDTO()).isNull();
  }

  @Test
  void testUndoSettingDocumentType() {
    DocumentUnitDTO dto =
        DocumentUnitDTO.builder()
            .uuid(UUID.randomUUID())
            .creationtimestamp(Instant.now())
            .documentnumber("1234567890123")
            .documentTypeId(123L)
            .build();
    repository.save(dto).block();

    DocumentUnit documentUnitFromFrontend =
        DocumentUnit.builder()
            .uuid(dto.getUuid())
            .creationtimestamp(dto.getCreationtimestamp())
            .documentNumber(dto.getDocumentnumber())
            .build();

    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/caselaw/documentunits/" + documentUnitFromFrontend.uuid())
        .bodyValue(documentUnitFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().coreData().documentType()).isNull();
            });

    List<DocumentUnitDTO> list = repository.findAll().collectList().block();
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getDocumentTypeId()).isNull();
    assertThat(list.get(0).getDocumentTypeDTO()).isNull();
  }

  @Test
  void testLegalEffectToBeSetFromNotSpecifiedToYesBySpecialCourtChangeButBeChangeableAfterwards() {
    testLegalEffectChanges(LegalEffect.NOT_SPECIFIED, "BGH", LegalEffect.YES);
  }

  @Test
  void testLegalEffectToBeSetFromNoToYesBySpecialCourtChangeButBeChangeableAfterwards() {
    testLegalEffectChanges(LegalEffect.NO, "BVerfG", LegalEffect.YES);
  }

  @Test
  void testLegalEffectToBeKeptAtYesBySpecialCourtChangeAndBeChangeableAfterwards() {
    testLegalEffectChanges(LegalEffect.YES, "BSG", LegalEffect.YES);
  }

  @Test
  void testLegalEffectToBeKeptByNonSpecialCourtChangeAndBeChangeableAfterwards() {
    testLegalEffectChanges(LegalEffect.NO, "ABC", LegalEffect.NO);
  }

  private void testLegalEffectChanges(
      LegalEffect valueBefore, String courtType, LegalEffect expectedValueAfter) {
    // outsource and reuse this default way of building a new DocumentUnitDTO? TODO
    DocumentUnitDTO dto =
        DocumentUnitDTO.builder()
            .uuid(UUID.randomUUID())
            .creationtimestamp(Instant.now())
            .documentnumber("1234567890123")
            .legalEffect(valueBefore.getLabel())
            .build();

    repository.save(dto).block();

    DocumentUnit documentUnitFromFrontend =
        buildDocumentUnitFromFrontendWithLegalEffect(dto, courtType, valueBefore);

    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/caselaw/documentunits/" + documentUnitFromFrontend.uuid())
        .bodyValue(documentUnitFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              assertThat(response.getResponseBody().coreData().court().type()).isEqualTo(courtType);
              assertThat(response.getResponseBody().coreData().legalEffect())
                  .isEqualTo(expectedValueAfter.getLabel());
            });

    List<DocumentUnitDTO> list = repository.findAll().collectList().block();
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getLegalEffect()).isEqualTo(expectedValueAfter.getLabel());

    // Change to NO, should stay NO
    testCorrectFEtoBECallBehaviourWithLegalEffect(
        buildDocumentUnitFromFrontendWithLegalEffect(dto, courtType, LegalEffect.NO));

    // Change to NOT_SPECIFIED, should stay NOT_SPECIFIED
    testCorrectFEtoBECallBehaviourWithLegalEffect(
        buildDocumentUnitFromFrontendWithLegalEffect(dto, courtType, LegalEffect.NOT_SPECIFIED));

    // Remove court, should stay NOT_SPECIFIED
    testCorrectFEtoBECallBehaviourWithLegalEffect(
        buildDocumentUnitFromFrontendWithLegalEffect(dto, null, LegalEffect.NOT_SPECIFIED));
  }

  private DocumentUnit buildDocumentUnitFromFrontendWithLegalEffect(
      DocumentUnitDTO dto, String courtType, LegalEffect legalEffect) {
    CoreData coreData;
    if (courtType == null) {
      coreData = CoreData.builder().legalEffect(legalEffect.getLabel()).build();
    } else {
      coreData =
          CoreData.builder()
              .court(Court.builder().type(courtType).build())
              .legalEffect(legalEffect.getLabel())
              .build();
    }
    return DocumentUnit.builder()
        .uuid(dto.getUuid())
        .creationtimestamp(dto.getCreationtimestamp())
        .documentNumber(dto.getDocumentnumber())
        .coreData(coreData)
        .build();
  }

  private void testCorrectFEtoBECallBehaviourWithLegalEffect(
      DocumentUnit documentUnitFromFrontend) {
    webClient
        .mutateWith(csrf())
        .put()
        .uri("/api/v1/caselaw/documentunits/" + documentUnitFromFrontend.uuid())
        .bodyValue(documentUnitFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(DocumentUnit.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).isNotNull();
              if (documentUnitFromFrontend.coreData().court() != null) {
                assertThat(response.getResponseBody().coreData().court().type())
                    .isEqualTo(documentUnitFromFrontend.coreData().court().type());
              }
              assertThat(response.getResponseBody().coreData().legalEffect())
                  .isEqualTo(documentUnitFromFrontend.coreData().legalEffect());
            });

    List<DocumentUnitDTO> list = repository.findAll().collectList().block();
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getLegalEffect())
        .isEqualTo(documentUnitFromFrontend.coreData().legalEffect());
  }
}
