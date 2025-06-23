package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildBGHDocOffice;
import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDSDocOffice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

import com.fasterxml.jackson.core.type.TypeReference;
import de.bund.digitalservice.ris.caselaw.SliceTestImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseProcedureRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseUserGroupRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ProcedureDTO;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ProcedureTransformer;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitListItem;
import de.bund.digitalservice.ris.caselaw.domain.Procedure;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@Sql(
    scripts = {
      "classpath:doc_office_init.sql",
      "classpath:procedures_init.sql",
      "classpath:user_group_init.sql"
    })
@Sql(
    scripts = {"classpath:procedures_cleanup.sql"},
    executionPhase = AFTER_TEST_METHOD)
class ProcedureIntegrationTest extends BaseIntegrationTest {

  @Autowired private RisWebTestClient risWebTestClient;
  @Autowired private DatabaseDocumentationUnitRepository documentationUnitRepository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @Autowired private DatabaseProcedureRepository repository;
  @Autowired private DatabaseUserGroupRepository userGroupRepository;

  private final DocumentationOffice docOffice = buildDSDocOffice();
  private DocumentationOfficeDTO docOfficeDTO;
  private DocumentationUnitDTO docUnitDTO;

  @BeforeEach
  void setUp() {
    docOfficeDTO = documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation());
    docUnitDTO = documentationUnitRepository.findByDocumentNumber("1234567890123").get();
  }

  @AfterEach
  void cleanUp() {
    documentationUnitRepository.deleteAll();
    repository.deleteAll();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "testProcedure",
        "123456",
        "asd 123",
        "äÜö.123!",
        "thisIsALongLongLongLongLongLongLongProcedureName"
      })
  void testAddingNewProcedure(String label) {
    Procedure procedure = Procedure.builder().label(label).build();

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(docUnitDTO.getId())
            .documentNumber(docUnitDTO.getDocumentNumber())
            .coreData(
                CoreData.builder().procedure(procedure).documentationOffice(docOffice).build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + docUnitDTO.getId())
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().coreData().procedure().label())
                  .isEqualTo(procedure.label());
              assertThat(response.getResponseBody().coreData().procedure().createdAt()).isNotNull();
            });

    Optional<ProcedureDTO> resultProcedure =
        repository.findAllByLabelAndDocumentationOffice(label, docOfficeDTO);
    assertThat(resultProcedure).isPresent();
    assertThat(resultProcedure.get().getLabel()).isEqualTo(label);
    assertThat(resultProcedure.get().getDocumentationOffice().getAbbreviation())
        .isEqualTo(docOffice.abbreviation());
  }

  @Test
  void testAddSameProcedureLabel_shouldReturnTheExistingProcedure() {
    ProcedureDTO procedureDTO =
        repository.findAllByLabelAndDocumentationOffice("procedure1", docOfficeDTO).get();

    Procedure procedure = Procedure.builder().label("procedure1").build();

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(docUnitDTO.getId())
            .documentNumber(docUnitDTO.getDocumentNumber())
            .coreData(
                CoreData.builder().procedure(procedure).documentationOffice(docOffice).build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + docUnitDTO.getId())
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              CoreData coreData = response.getResponseBody().coreData();
              assertThat(coreData.previousProcedures()).isEmpty();
              assertThat(coreData.procedure())
                  .extracting("id", "label")
                  .containsExactly(procedureDTO.getId(), procedure.label());
            });

    DocumentationUnitDTO updatedDocUnitDTO =
        documentationUnitRepository.findById(docUnitDTO.getId()).get();
    ProcedureDTO currentProcedure = updatedDocUnitDTO.getProcedure();
    assertThat(currentProcedure.getLabel()).isEqualTo(procedureDTO.getLabel());
    assertThat(currentProcedure.getId()).isEqualTo(procedureDTO.getId());

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/procedure?q=" + "procedure1" + "&sz=1&pg=0")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(new TypeReference<SliceTestImpl<Procedure>>() {})
        .consumeWith(
            response ->
                assertThat(
                        Objects.requireNonNull(response.getResponseBody())
                            .getContent()
                            .getFirst()
                            .documentationUnitCount())
                    .isOne());
  }

  @Test
  void testAddSameProcedureLabelWithTrailingSpaces_shouldReturnTheExistingProcedure() {
    ProcedureDTO procedureDTO =
        repository.findAllByLabelAndDocumentationOffice("procedure1", docOfficeDTO).get();

    Procedure procedure = Procedure.builder().label("  procedure1  ").build();

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(docUnitDTO.getId())
            .documentNumber(docUnitDTO.getDocumentNumber())
            .coreData(
                CoreData.builder().procedure(procedure).documentationOffice(docOffice).build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + docUnitDTO.getId())
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(Decision.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().coreData().procedure())
                    .extracting("id", "label")
                    .containsExactly(procedureDTO.getId(), procedure.label().trim()));

    Optional<ProcedureDTO> resultProcedure =
        repository.findAllByLabelAndDocumentationOffice("procedure1", docOfficeDTO);
    assertThat(resultProcedure).isPresent();

    DocumentationUnitDTO updatedDocUnitDTO =
        documentationUnitRepository.findById(docUnitDTO.getId()).get();
    ProcedureDTO currentProcedure = updatedDocUnitDTO.getProcedure();
    assertThat(currentProcedure.getLabel()).isEqualTo(procedureDTO.getLabel());
    assertThat(currentProcedure.getId()).isEqualTo(procedureDTO.getId());
  }

  @Test
  void testProcedureLabelWithTrailingSpaces_shouldSaveProcedureWithoutTrailingSpaces() {
    Procedure procedure = Procedure.builder().label("  procedure1  ").build();

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(docUnitDTO.getId())
            .documentNumber(docUnitDTO.getDocumentNumber())
            .coreData(
                CoreData.builder().procedure(procedure).documentationOffice(docOffice).build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + docUnitDTO.getId())
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(Decision.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().coreData().procedure())
                    .extracting("label")
                    .isEqualTo("procedure1"));

    Optional<ProcedureDTO> resultProcedure =
        repository.findAllByLabelAndDocumentationOffice("procedure1", docOfficeDTO);
    assertThat(resultProcedure).isPresent();

    DocumentationUnitDTO updatedDocUnitDTO =
        documentationUnitRepository.findById(docUnitDTO.getId()).get();
    ProcedureDTO currentProcedure = updatedDocUnitDTO.getProcedure();
    assertThat(currentProcedure.getLabel()).isEqualTo("procedure1");
  }

  @Test
  @SuppressWarnings("java:S5961")
  void testAddingProcedureToPreviousProcedures() {
    Procedure procedure1 = Procedure.builder().label("foo").build();
    Procedure procedure2 = Procedure.builder().label("bar").build();
    Procedure procedure3 = Procedure.builder().label("baz").build();

    // add first procedure
    Decision decisionFromFrontend1 =
        Decision.builder()
            .uuid(docUnitDTO.getId())
            .documentNumber(docUnitDTO.getDocumentNumber())
            .coreData(
                CoreData.builder().procedure(procedure1).documentationOffice(docOffice).build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + docUnitDTO.getId())
        .bodyValue(decisionFromFrontend1)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().coreData().procedure().label())
                  .isEqualTo(procedure1.label());
              assertThat(response.getResponseBody().coreData().previousProcedures()).isEmpty();
            });

    assertThat(repository.findAllByLabelAndDocumentationOffice("foo", docOfficeDTO).get())
        .isNotNull();

    // add second procedure
    Decision decisionFromFrontend2 =
        Decision.builder()
            .uuid(docUnitDTO.getId())
            .documentNumber(docUnitDTO.getDocumentNumber())
            .coreData(
                CoreData.builder().procedure(procedure2).documentationOffice(docOffice).build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + docUnitDTO.getId())
        .bodyValue(decisionFromFrontend2)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().coreData().procedure().label())
                  .isEqualTo(procedure2.label());
              assertThat(response.getResponseBody().coreData().previousProcedures())
                  .isEqualTo(List.of("foo"));
            });

    assertThat(repository.findAllByLabelAndDocumentationOffice("bar", docOfficeDTO).get())
        .isNotNull();

    // add third procedure
    Decision decisionFromFrontend3 =
        Decision.builder()
            .uuid(docUnitDTO.getId())
            .documentNumber(docUnitDTO.getDocumentNumber())
            .coreData(
                CoreData.builder().procedure(procedure3).documentationOffice(docOffice).build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + docUnitDTO.getId())
        .bodyValue(decisionFromFrontend3)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().coreData().procedure().label())
                  .isEqualTo(procedure3.label());
              assertThat(response.getResponseBody().coreData().previousProcedures())
                  .containsExactly("foo", "bar");
            });

    assertThat(repository.findAllByLabelAndDocumentationOffice("baz", docOfficeDTO).get())
        .isNotNull();

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/procedure?q=" + procedure1.label() + "&sz=1&pg=0")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(new TypeReference<SliceTestImpl<Procedure>>() {})
        .consumeWith(
            response ->
                assertThat(
                        Objects.requireNonNull(response.getResponseBody())
                            .getContent()
                            .get(0)
                            .documentationUnitCount())
                    .isZero());

    var procedure1Id =
        repository.findAllByLabelAndDocumentationOffice("foo", docOfficeDTO).get().getId();

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/procedure/" + procedure1Id + "/documentunits")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(new TypeReference<List<DocumentationUnitListItem>>() {})
        .consumeWith(
            response -> assertThat(Objects.requireNonNull(response.getResponseBody())).isEmpty());

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/procedure?q=" + procedure3.label() + "&sz=1&pg=0")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(new TypeReference<SliceTestImpl<Procedure>>() {})
        .consumeWith(
            response ->
                assertThat(
                        Objects.requireNonNull(response.getResponseBody())
                            .getContent()
                            .get(0)
                            .documentationUnitCount())
                    .isEqualTo(1));

    var procedure3Id =
        repository.findAllByLabelAndDocumentationOffice("baz", docOfficeDTO).get().getId();

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/procedure/" + procedure3Id + "/documentunits")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(new TypeReference<List<DocumentationUnitListItem>>() {})
        .consumeWith(
            response ->
                assertThat(
                        Objects.requireNonNull(response.getResponseBody()).get(0).documentNumber())
                    .isEqualTo("1234567890123"));
  }

  @Test
  void testAddProcedureWhichIsInHistoryAgain() {
    UUID procedureId = addProcedureToDocUnit("foo", docUnitDTO);
    addProcedureToDocUnit("bar", docUnitDTO);

    Decision decisionFromFrontend1 =
        Decision.builder()
            .uuid(docUnitDTO.getId())
            .documentNumber(docUnitDTO.getDocumentNumber())
            .coreData(
                CoreData.builder()
                    .procedure(Procedure.builder().id(procedureId).build())
                    .documentationOffice(docOffice)
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + docUnitDTO.getId())
        .bodyValue(decisionFromFrontend1)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              CoreData coreData = response.getResponseBody().coreData();
              assertThat(coreData.procedure().label()).isEqualTo("foo");
              assertThat(coreData.previousProcedures()).containsExactly("foo", "bar");
            });
  }

  @Test
  void testAddProcedureWithSameNameToDifferentOffice() {
    Procedure procedure = Procedure.builder().label("testProcedure").build();

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(docUnitDTO.getId())
            .documentNumber(docUnitDTO.getDocumentNumber())
            .coreData(
                CoreData.builder().procedure(procedure).documentationOffice(docOffice).build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + docUnitDTO.getId())
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(Decision.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().coreData().procedure().label())
                    .isEqualTo(procedure.label()));

    DocumentationOfficeDTO dsOffice = documentationOfficeRepository.findByAbbreviation("DS");
    assertThat(repository.findAllByLabelAndDocumentationOffice("testProcedure", dsOffice))
        .isNotNull();
    assertThat(repository.findAllByLabelAndDocumentationOffice("testProcedure", docOfficeDTO))
        .isNotNull();
  }

  @Test
  void testSearch_withoutQuery_shouldReturnLatestProcedureUsedInDocumentationUnit() {
    addProcedureToDocUnit("procedure1", docUnitDTO);
    addProcedureToDocUnit("procedure2", docUnitDTO);
    addProcedureToDocUnit("procedure3", docUnitDTO);

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/procedure?withDocUnits=true&sz=20&pg=0")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(new TypeReference<SliceTestImpl<Procedure>>() {})
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).hasSize(1);
              assertThat(response.getResponseBody().getContent().get(0).label())
                  .isEqualTo("procedure3");
            });
  }

  @Test
  void testSearch_withQuery_shouldReturnProceduresWithDateFirst() {
    DecisionDTO documentationUnitDTO2 =
        (DecisionDTO) documentationUnitRepository.findByDocumentNumber("docNumber00002").get();
    DecisionDTO documentationUnitDTO3 =
        (DecisionDTO) documentationUnitRepository.findByDocumentNumber("docNumber00003").get();

    addProcedureToDocUnit("with date", docUnitDTO);

    ProcedureDTO procedureWithDateInPast =
        repository.findAllByLabelAndDocumentationOffice("with date in past", docOfficeDTO).get();
    ProcedureDTO procedureWithoutDate =
        repository.findAllByLabelAndDocumentationOffice("without date", docOfficeDTO).get();

    documentationUnitDTO2.setProcedureHistory(List.of(procedureWithDateInPast));
    documentationUnitDTO2.setProcedure(procedureWithDateInPast);
    documentationUnitRepository.save(documentationUnitDTO2);

    documentationUnitDTO3.setProcedureHistory(List.of(procedureWithoutDate));
    documentationUnitDTO3.setProcedure(procedureWithoutDate);
    documentationUnitRepository.save(documentationUnitDTO3);

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/procedure?withDocUnits=true&q=date&sz=20&pg=0")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(new TypeReference<SliceTestImpl<Procedure>>() {})
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).hasSize(3);
              assertThat(response.getResponseBody().getContent().get(0).label())
                  .isEqualTo("with date");
              assertThat(response.getResponseBody().getContent().get(1).label())
                  .isEqualTo("with date in past");
              assertThat(response.getResponseBody().getContent().get(2).label())
                  .isEqualTo("without date");
            });
  }

  @Test
  void testSearch_withQuery_shouldOnlyReturnLatestProcedureUsedInDocumentationUnit() {
    addProcedureToDocUnit("procedure1", docUnitDTO);
    addProcedureToDocUnit("procedure2", docUnitDTO);
    addProcedureToDocUnit("procedure3", docUnitDTO);

    assertThat(repository.findAll()).hasSize(7);

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/procedure?withDocUnits=true&q=procedure&sz=20&pg=0")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(new TypeReference<SliceTestImpl<Procedure>>() {})
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).hasSize(1);
              assertThat(response.getResponseBody().getContent().get(0).label())
                  .isEqualTo("procedure3");
            });

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/procedure?withDocUnits=true&q=procedure2&pg=0&sz=10")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(new TypeReference<SliceTestImpl<Procedure>>() {})
        .consumeWith(response -> assertThat(response.getResponseBody()).isEmpty());
  }

  @Test
  void testSearch_withQueryWithTrailingSpaces_shouldReturnResultWithoutTrailingSpaces() {
    addProcedureToDocUnit("procedure1", docUnitDTO);

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/procedure?withDocUnits=true&q= procedure1 &sz=20&pg=0")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(new TypeReference<SliceTestImpl<Procedure>>() {})
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).hasSize(1);
              assertThat(response.getResponseBody().getContent().get(0).label())
                  .isEqualTo("procedure1");
            });
  }

  @Test
  void testSearch_withDifferentCasing_shouldReturnResult() {
    addProcedureToDocUnit("procedure1", docUnitDTO);

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/procedure?withDocUnits=true&q=PROcedure1 &sz=20&pg=0")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(new TypeReference<SliceTestImpl<Procedure>>() {})
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).hasSize(1);
              assertThat(response.getResponseBody().getContent().get(0).label())
                  .isEqualTo("procedure1");
            });
  }

  @Test
  void testProcedureControllerReturnsPerDocOffice() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/procedure?withDocUnits=true&sz=10&pg=0")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(new TypeReference<SliceTestImpl<Procedure>>() {})
        .consumeWith(
            response ->
                assertThat(Objects.requireNonNull(response.getResponseBody()).getContent())
                    .isEmpty());
  }

  @Test
  // only needed for e2e test
  // TODO remove controller endpoint. check how to handle cleanup after e2e tests
  void testDeleteProcedure() {
    ProcedureDTO procedureDTO =
        repository.findAllByLabelAndDocumentationOffice("procedure1", docOfficeDTO).get();
    assertThat(repository.findAll()).hasSize(7);

    risWebTestClient
        .withDefaultLogin()
        .delete()
        .uri("/api/v1/caselaw/procedure/" + procedureDTO.getId())
        .exchange()
        .expectStatus()
        .is2xxSuccessful();

    assertThat(repository.findAll()).hasSize(6);
  }

  @Test
  void testProcedureControllerReturnsDocUnitsPerProcedure() {
    DocumentationOfficeDTO bghDocOfficeDTO =
        documentationOfficeRepository.findByAbbreviation("BGH");
    ProcedureDTO procedure =
        repository.findAllByLabelAndDocumentationOffice("testProcedure BGH", bghDocOfficeDTO).get();

    var bghDocUnit = documentationUnitRepository.findByDocumentNumber("bghDocument123").get();

    Decision decisionFromFrontend1 =
        Decision.builder()
            .uuid(bghDocUnit.getId())
            .documentNumber(bghDocUnit.getDocumentNumber())
            .coreData(
                CoreData.builder()
                    .procedure(ProcedureTransformer.transformToDomain(procedure, false))
                    .documentationOffice(buildBGHDocOffice())
                    .build())
            .build();

    risWebTestClient
        .withLogin("/BGH")
        .put()
        .uri("/api/v1/caselaw/documentunits/" + bghDocUnit.getId())
        .bodyValue(decisionFromFrontend1)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().coreData().procedure().label())
                  .isEqualTo(procedure.getLabel());
              assertThat(response.getResponseBody().coreData().previousProcedures()).isEmpty();
            });

    var docUnitList =
        risWebTestClient
            .withLogin("/BGH")
            .get()
            .uri("/api/v1/caselaw/procedure/" + procedure.getId() + "/documentunits")
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .expectBody(new TypeReference<List<DocumentationUnitListItem>>() {})
            .returnResult()
            .getResponseBody();
    assertThat(docUnitList.get(0).documentNumber()).isEqualTo(bghDocUnit.getDocumentNumber());
    assertThat(docUnitList.get(0).isDeletable()).isTrue();
    assertThat(docUnitList.get(0).isEditable()).isTrue();

    // Without being assigned, the doc unit is read-only for an external user.
    var docUnitListExternal =
        risWebTestClient
            .withLogin("/BGH/Extern", "External")
            .get()
            .uri("/api/v1/caselaw/procedure/" + procedure.getId() + "/documentunits")
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .expectBody(new TypeReference<List<DocumentationUnitListItem>>() {})
            .returnResult()
            .getResponseBody();
    assertThat(docUnitListExternal.get(0).documentNumber())
        .isEqualTo(bghDocUnit.getDocumentNumber());
    assertThat(docUnitListExternal.get(0).isDeletable()).isFalse();
    assertThat(docUnitListExternal.get(0).isEditable()).isFalse();

    risWebTestClient
        .withLogin("/BGH")
        .put()
        .uri(
            "/api/v1/caselaw/procedure/"
                + procedure.getId()
                + "/assign/3b733549-d2cc-40f0-b7f3-9bfa9f3c1b89")
        .exchange()
        .expectStatus()
        .is2xxSuccessful();

    // After external user is assigned to doc unit via procedure it is editable.
    var docUnitListExternalAfterAssign =
        risWebTestClient
            .withLogin("/BGH/Extern", "External")
            .get()
            .uri("/api/v1/caselaw/procedure/" + procedure.getId() + "/documentunits")
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .expectBody(new TypeReference<List<DocumentationUnitListItem>>() {})
            .returnResult()
            .getResponseBody();
    assertThat(docUnitListExternalAfterAssign.get(0).isDeletable()).isFalse();
    assertThat(docUnitListExternalAfterAssign.get(0).isEditable()).isTrue();
  }

  @Test
  void testSearch_withQuery_shouldReturnLatestProcedureUsedInDocumentationUnit() {
    addProcedureToDocUnit("procedure1", docUnitDTO);
    addProcedureToDocUnit("procedure2", docUnitDTO);

    assertThat(repository.findAll()).hasSize(7);

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/procedure?withDocUnits=true&q=procedure&sz=20&pg=0")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(new TypeReference<SliceTestImpl<Procedure>>() {})
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).hasSize(1);
              assertThat(response.getResponseBody().getContent().get(0).label())
                  .isEqualTo("procedure2");
            });
  }

  @Test
  void testSearch_withQueryAndWithExternalUser_shouldReturnOnlyAssignedProcedure() {
    DocumentationUnitDTO unassignedDocUnit =
        documentationUnitRepository.findByDocumentNumber("docNumber00002").get();
    addProcedureToDocUnit("procedure2", unassignedDocUnit);
    assignProcedure(docUnitDTO);

    risWebTestClient
        .withExternalLogin()
        .get()
        .uri("/api/v1/caselaw/procedure?withDocUnits=true&q=procedure&sz=20&pg=0")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(new TypeReference<SliceTestImpl<Procedure>>() {})
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).hasSize(1);
              assertThat(response.getResponseBody().getContent().get(0).label())
                  .isEqualTo("procedure1");
            });
  }

  @Test
  void testSearch_withoutQueryAndWithExternalUser_shouldReturnOnlyAssignedProcedure() {
    DocumentationUnitDTO unassignedDocUnit =
        documentationUnitRepository.findByDocumentNumber("docNumber00002").get();
    addProcedureToDocUnit("procedure2", unassignedDocUnit);
    assignProcedure(docUnitDTO);

    risWebTestClient
        .withExternalLogin()
        .get()
        .uri("/api/v1/caselaw/procedure?withDocUnits=true&sz=20&pg=0")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(new TypeReference<SliceTestImpl<Procedure>>() {})
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody()).hasSize(1);
              assertThat(response.getResponseBody().getContent().get(0).label())
                  .isEqualTo("procedure1");
            });
  }

  @Test
  void testAssign_withInternalUser_shouldReturnSuccessMessage() {
    UUID procedureId = addProcedureToDocUnit("procedure1", docUnitDTO);
    UUID userGroupId = userGroupRepository.findAll().get(0).getId();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/procedure/" + procedureId + "/assign/" + userGroupId)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(String.class)
        .consumeWith(
            response -> {
              String result = response.getResponseBody();
              assertThat(result)
                  .isEqualTo("Vorgang 'procedure1' wurde Nutzergruppe 'DS/Extern' zugewiesen.");
            });
  }

  @Test
  void testAssign_withNonExistingProcedureId_shouldResultInBadRequest() {
    addProcedureToDocUnit("procedure1", docUnitDTO);
    String nonExistingProcedureId = "non-existing procedureId";
    UUID userGroupId = userGroupRepository.findAll().get(0).getId();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/procedure/" + nonExistingProcedureId + "/assign/" + userGroupId)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody(String.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody())
                    .contains(
                        "Failed to convert 'procedureUUID' with value: "
                            + "'non-existing procedureId'\",\"instance\":\""
                            + "/api/v1/caselaw/procedure/non-existing%20procedureId/assign/"
                            + userGroupId));
  }

  @Test
  void testAssign_withNonExistingGroupId_shouldResultInBadRequest() {
    UUID procedureId = addProcedureToDocUnit("procedure1", docUnitDTO);
    String nonExistingGroupId = "non-existing groupId";

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/procedure/" + procedureId + "/assign/" + nonExistingGroupId)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody(String.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody())
                    .contains(
                        "Failed to convert 'userGroupId' with value: "
                            + "'non-existing groupId'\",\"instance\":\""
                            + "/api/v1/caselaw/procedure/"
                            + procedureId
                            + "/assign/non-existing%20groupId"));
  }

  @Test
  void testUnassign_withInternalUser_shouldReturnSuccessMessage() {
    UUID procedureId = addProcedureToDocUnit("procedure1", docUnitDTO);
    UUID userGroupId = userGroupRepository.findAll().get(0).getId();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/procedure/" + procedureId + "/assign/" + userGroupId)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(String.class)
        .consumeWith(
            response -> {
              String result = response.getResponseBody();
              assertThat(result)
                  .isEqualTo("Vorgang 'procedure1' wurde Nutzergruppe 'DS/Extern' zugewiesen.");
            });

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/procedure/" + procedureId + "/unassign")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(String.class)
        .consumeWith(
            response -> {
              String result = response.getResponseBody();
              assertThat(result)
                  .isEqualTo("Die Zuweisung aus Vorgang 'procedure1' wurde entfernt.");
            });
  }

  @Test
  void testAssign_withExternalUser_shouldBeForbidden() {
    UUID procedureId = addProcedureToDocUnit("procedure1", docUnitDTO);
    UUID userGroupId = userGroupRepository.findAll().get(0).getId();

    risWebTestClient
        .withExternalLogin()
        .put()
        .uri("/api/v1/caselaw/procedure/" + procedureId + "/assign/" + userGroupId)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testUnassign_withExternalUser_shouldBeForbidden() {
    UUID procedureId = addProcedureToDocUnit("procedure1", docUnitDTO);

    risWebTestClient
        .withExternalLogin()
        .put()
        .uri("/api/v1/caselaw/procedure/" + procedureId + "/unassign")
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  private void assignProcedure(DocumentationUnitDTO docUnitDTO) {
    UUID procedureId = addProcedureToDocUnit("procedure1", docUnitDTO);
    UUID userGroupId = userGroupRepository.findAll().get(0).getId();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/procedure/" + procedureId + "/assign/" + userGroupId)
        .exchange()
        .expectStatus()
        .is2xxSuccessful();
  }

  private UUID addProcedureToDocUnit(
      String procedureValue, DocumentationUnitDTO documentationUnitDTO) {
    Decision decisionFromFrontend1 =
        Decision.builder()
            .uuid(documentationUnitDTO.getId())
            .documentNumber(documentationUnitDTO.getDocumentNumber())
            .coreData(
                CoreData.builder()
                    .procedure(Procedure.builder().label(procedureValue).build())
                    .documentationOffice(docOffice)
                    .build())
            .build();

    AtomicReference<UUID> procedureId = new AtomicReference<>();
    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + documentationUnitDTO.getId())
        .bodyValue(decisionFromFrontend1)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              CoreData coreData = response.getResponseBody().coreData();
              assertThat(coreData.procedure().label()).isEqualTo(procedureValue);
              procedureId.set(coreData.procedure().id());
            });

    return procedureId.get();
  }
}
