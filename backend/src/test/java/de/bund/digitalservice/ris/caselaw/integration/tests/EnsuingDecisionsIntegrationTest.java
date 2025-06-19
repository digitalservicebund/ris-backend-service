package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.EnsuingDecision;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = {"classpath:doc_office_init.sql", "classpath:ensuing_decisions_init.sql"})
@Sql(
    scripts = {"classpath:ensuing_decisions_cleanup.sql"},
    executionPhase = AFTER_TEST_METHOD)
class EnsuingDecisionsIntegrationTest extends BaseIntegrationTest {
  @Autowired private RisWebTestClient risWebTestClient;

  @Test
  void testGetDocumentationUnit_withoutEnsuingDecisions_shouldReturnEmptyList() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr002")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> assertThat(response.getResponseBody().ensuingDecisions()).isEmpty());
  }

  @Test
  void testGetDocumentationUnit_withEnsuingDecisions_shouldReturnList() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr001")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().ensuingDecisions()).hasSize(2);
              assertThat(response.getResponseBody().ensuingDecisions().get(0).isPending())
                  .isFalse();
              assertThat(response.getResponseBody().ensuingDecisions().get(1).isPending()).isTrue();
            });
  }

  @Test
  void testUpdateDocumentationUnit_addNewEnsuingDecision() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr001")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> assertThat(response.getResponseBody().ensuingDecisions()).hasSize(2));

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3"))
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .ensuingDecisions(
                List.of(
                    EnsuingDecision.builder()
                        .uuid(UUID.fromString("f0232240-7416-11ee-b962-0242ac120002"))
                        .pending(false)
                        .note("note1")
                        .build(),
                    EnsuingDecision.builder()
                        .uuid(UUID.fromString("f0232240-7416-11ee-b962-0242ac120003"))
                        .pending(true)
                        .note("note2")
                        .build(),
                    EnsuingDecision.builder().pending(false).note("note3").build(),
                    EnsuingDecision.builder().pending(false).note("note4").build()))
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3")
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().ensuingDecisions()).hasSize(4);
              assertThat(response.getResponseBody().ensuingDecisions().get(2).isPending())
                  .isFalse();
            });
  }

  @Test
  void testUpdateDocumentationUnit_updateExistingEnsuingDecision() {
    UUID uuid = UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3");
    UUID ensuingDecisionUUID1 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120002");
    UUID ensuingDecisionUUID2 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120003");
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr001")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().ensuingDecisions()).hasSize(2);
              assertThat(response.getResponseBody().ensuingDecisions().get(0).getNote())
                  .isEqualTo("note1");
              assertThat(response.getResponseBody().ensuingDecisions().get(0).getFileNumber())
                  .isEqualTo("abc");
              assertThat(response.getResponseBody().ensuingDecisions().get(0).getUuid())
                  .isEqualTo(ensuingDecisionUUID1);
            });

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(uuid)
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .ensuingDecisions(
                List.of(
                    EnsuingDecision.builder()
                        .uuid(ensuingDecisionUUID1)
                        .pending(false)
                        .note("updatedNote1")
                        .fileNumber("cba")
                        .build(),
                    EnsuingDecision.builder()
                        .uuid(ensuingDecisionUUID2)
                        .pending(true)
                        .note("note2")
                        .build()))
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3")
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().ensuingDecisions()).hasSize(2);
              assertThat(response.getResponseBody().ensuingDecisions().get(0).getNote())
                  .isEqualTo("updatedNote1");
              assertThat(response.getResponseBody().ensuingDecisions().get(0).getFileNumber())
                  .isEqualTo("cba");
              assertThat(response.getResponseBody().ensuingDecisions().get(0).getUuid())
                  .isEqualTo(ensuingDecisionUUID1);
              assertThat(response.getResponseBody().ensuingDecisions().get(1).getUuid())
                  .isEqualTo(ensuingDecisionUUID2);
            });
  }

  @Test
  void testUpdateDocumentationUnit_tryToAddAEmptyEnsuingDecision_shouldNotSucceed() {
    UUID ensuingDecisionUUID1 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120002");
    UUID ensuingDecisionUUID2 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120003");
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr001")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> assertThat(response.getResponseBody().ensuingDecisions()).hasSize(2));

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3"))
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .ensuingDecisions(
                List.of(
                    EnsuingDecision.builder()
                        .uuid(ensuingDecisionUUID1)
                        .pending(false)
                        .note("updatedNote1")
                        .fileNumber("cba")
                        .build(),
                    EnsuingDecision.builder()
                        .uuid(ensuingDecisionUUID2)
                        .pending(true)
                        .note("note2")
                        .build(),
                    EnsuingDecision.builder().build()))
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3")
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> assertThat(response.getResponseBody().ensuingDecisions()).hasSize(2));
  }

  @Test
  void testUpdateDocumentationUnit_removeValuesDeletesEnsuingDecision() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr001")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> assertThat(response.getResponseBody().ensuingDecisions()).hasSize(2));

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3"))
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .ensuingDecisions(
                List.of(
                    EnsuingDecision.builder()
                        .uuid(UUID.fromString("f0232240-7416-11ee-b962-0242ac120002"))
                        .pending(false)
                        .note("updatedNote1")
                        .fileNumber("cba")
                        .build(),
                    EnsuingDecision.builder()
                        .uuid(UUID.fromString("f0232240-7416-11ee-b962-0242ac120003"))
                        .build(),
                    EnsuingDecision.builder().build()))
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3")
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> assertThat(response.getResponseBody().ensuingDecisions()).hasSize(1));
  }

  @Test
  void testUpdateDocumentationUnit_removeEnsuingDecision() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr001")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> assertThat(response.getResponseBody().ensuingDecisions()).hasSize(2));

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3"))
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .ensuingDecisions(
                List.of(
                    EnsuingDecision.builder()
                        .uuid(UUID.fromString("f0232240-7416-11ee-b962-0242ac120002"))
                        .pending(false)
                        .note("updatedNote1")
                        .fileNumber("cba")
                        .build()))
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3")
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> assertThat(response.getResponseBody().ensuingDecisions()).hasSize(1));
  }

  @Test
  void testUpdateDocumentationUnit_removeAllEnsuingDecisions() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr001")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> assertThat(response.getResponseBody().ensuingDecisions()).hasSize(2));

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3"))
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3")
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> assertThat(response.getResponseBody().ensuingDecisions()).isEmpty());
  }

  @Test
  void testUpdateDocumentationUnit_updateToPendingDecision() {
    UUID uuid = UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3");
    UUID ensuingDecisionUUID1 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120002");
    UUID ensuingDecisionUUID2 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120003");
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr001")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().ensuingDecisions()).hasSize(2);
              assertThat(response.getResponseBody().ensuingDecisions().get(0).isPending())
                  .isFalse();
              assertThat(response.getResponseBody().ensuingDecisions().get(1).isPending()).isTrue();
            });

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(uuid)
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .ensuingDecisions(
                List.of(
                    EnsuingDecision.builder()
                        .uuid(ensuingDecisionUUID1)
                        .pending(true)
                        .note("note1")
                        .build(),
                    EnsuingDecision.builder()
                        .uuid(ensuingDecisionUUID2)
                        .pending(true)
                        .note("note2")
                        .build()))
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3")
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().ensuingDecisions()).hasSize(2);
              assertThat(response.getResponseBody().ensuingDecisions().get(0).isPending()).isTrue();
              assertThat(response.getResponseBody().ensuingDecisions().get(1).isPending()).isTrue();
            });
  }

  @Test
  void
      testUpdateDocumentationUnit_addPendingDecisionWithDocumentNumber_shouldLinkRelatedDocumentationUnit() {
    UUID uuid = UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3");
    UUID ensuingDecisionUUID1 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120002");
    UUID ensuingDecisionUUID2 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120003");
    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(uuid)
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .ensuingDecisions(
                List.of(
                    EnsuingDecision.builder()
                        .pending(true)
                        .documentNumber("documentnr002")
                        .note("new note")
                        .build(),
                    EnsuingDecision.builder()
                        .uuid(ensuingDecisionUUID1)
                        .pending(true)
                        .note("note1")
                        .build(),
                    EnsuingDecision.builder()
                        .uuid(ensuingDecisionUUID2)
                        .pending(true)
                        .note("note2")
                        .build()))
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3")
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().ensuingDecisions()).hasSize(3);
              assertThat(response.getResponseBody().ensuingDecisions().get(0).isPending()).isTrue();
              assertThat(response.getResponseBody().ensuingDecisions().get(1).isPending()).isTrue();
            });
  }

  @Test
  void testUpdateDocumentationUnit_addEmptyPendingDecision_shouldNotSaveTheEmpty() {
    UUID uuid = UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3");
    UUID ensuingDecisionUUID1 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120002");
    UUID ensuingDecisionUUID2 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120003");
    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(uuid)
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .ensuingDecisions(
                List.of(
                    EnsuingDecision.builder().pending(true).build(),
                    EnsuingDecision.builder()
                        .uuid(ensuingDecisionUUID1)
                        .pending(true)
                        .note("note1")
                        .build(),
                    EnsuingDecision.builder()
                        .uuid(ensuingDecisionUUID2)
                        .pending(true)
                        .note("note2")
                        .build()))
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3")
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().ensuingDecisions()).hasSize(2);
              assertThat(response.getResponseBody().ensuingDecisions().get(0).isPending()).isTrue();
              assertThat(response.getResponseBody().ensuingDecisions().get(1).isPending()).isTrue();
            });
  }

  @Test
  void testUpdateDocumentationUnit_deletePendingDecision() {
    UUID uuid = UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3");
    UUID ensuingDecisionUUID1 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120002");

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr001")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().ensuingDecisions()).hasSize(2);
              assertThat(response.getResponseBody().ensuingDecisions().get(0).isPending())
                  .isFalse();
            });

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(uuid)
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .ensuingDecisions(
                List.of(
                    EnsuingDecision.builder()
                        .uuid(ensuingDecisionUUID1)
                        .pending(false)
                        .note("note1")
                        .build()))
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3")
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().ensuingDecisions()).hasSize(1);
              assertThat(response.getResponseBody().ensuingDecisions().get(0).isPending())
                  .isFalse();
            });
  }

  @Test
  void testUpdateDocumentationUnit_updateCourtInEnsuingDecision() {
    UUID uuid = UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3");
    UUID ensuingDecisionUUID1 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120002");
    UUID ensuingDecisionUUID2 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120003");
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr001")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().ensuingDecisions()).hasSize(2);
              assertThat(response.getResponseBody().ensuingDecisions().get(0).getCourt())
                  .extracting("id")
                  .isEqualTo(UUID.fromString("96301f85-9bd2-4690-a67f-f9fdfe725de3"));
              assertThat(response.getResponseBody().ensuingDecisions().get(1).getCourt())
                  .extracting("id")
                  .isEqualTo(UUID.fromString("96301f85-9bd2-4690-a67f-f9fdfe725de3"));
            });

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(uuid)
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .ensuingDecisions(
                List.of(
                    EnsuingDecision.builder()
                        .uuid(ensuingDecisionUUID1)
                        .pending(false)
                        .court(
                            Court.builder()
                                .id(UUID.fromString("f99a0003-bfa3-4baa-904c-be07e274c741"))
                                .location("Karlsruhe")
                                .type("BVerfG")
                                .build())
                        .fileNumber("abc")
                        .note("note1")
                        .build(),
                    EnsuingDecision.builder()
                        .uuid(ensuingDecisionUUID2)
                        .pending(true)
                        .fileNumber("cba")
                        .note("note2")
                        .build()))
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3")
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().ensuingDecisions()).hasSize(2);
              assertThat(response.getResponseBody().ensuingDecisions().get(0).getCourt())
                  .extracting("id")
                  .isEqualTo(UUID.fromString("f99a0003-bfa3-4baa-904c-be07e274c741"));
              assertThat(response.getResponseBody().ensuingDecisions().get(1).getCourt()).isNull();
            });
  }

  @Test
  void testUpdateDocumentationUnit_updateDecisionDateInEnsuingDecision() {
    UUID uuid = UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3");
    UUID ensuingDecisionUUID1 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120002");
    UUID ensuingDecisionUUID2 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120003");
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr001")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().ensuingDecisions()).hasSize(2);
              assertThat(response.getResponseBody().ensuingDecisions().get(0).getDecisionDate())
                  .isEqualTo("2011-01-21");
              assertThat(response.getResponseBody().ensuingDecisions().get(1).getDecisionDate())
                  .isEqualTo("2011-01-21");
            });

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(uuid)
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .ensuingDecisions(
                List.of(
                    EnsuingDecision.builder()
                        .uuid(ensuingDecisionUUID1)
                        .pending(false)
                        .decisionDate(LocalDate.parse("2000-01-21"))
                        .fileNumber("abc")
                        .note("note1")
                        .build(),
                    EnsuingDecision.builder()
                        .uuid(ensuingDecisionUUID2)
                        .pending(true)
                        .fileNumber("cba")
                        .note("note2")
                        .build()))
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3")
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().ensuingDecisions()).hasSize(2);
              assertThat(response.getResponseBody().ensuingDecisions().get(0).getDecisionDate())
                  .isEqualTo("2000-01-21");
              assertThat(response.getResponseBody().ensuingDecisions().get(1).getCourt()).isNull();
            });
  }

  @Test
  void testUpdateDocumentationUnit_updateFileNumberInEnsuingDecision() {
    UUID uuid = UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3");
    UUID ensuingDecisionUUID1 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120002");
    UUID ensuingDecisionUUID2 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120003");
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr001")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().ensuingDecisions()).hasSize(2);
              assertThat(response.getResponseBody().ensuingDecisions().get(0).getFileNumber())
                  .isEqualTo("abc");
              assertThat(response.getResponseBody().ensuingDecisions().get(1).getFileNumber())
                  .isEqualTo("cba");
            });

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(uuid)
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .ensuingDecisions(
                List.of(
                    EnsuingDecision.builder()
                        .uuid(ensuingDecisionUUID1)
                        .fileNumber("cba")
                        .note("note1")
                        .build(),
                    EnsuingDecision.builder().uuid(ensuingDecisionUUID2).note("note2").build()))
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3")
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().ensuingDecisions()).hasSize(2);
              assertThat(response.getResponseBody().ensuingDecisions().get(0).getFileNumber())
                  .isEqualTo("cba");
              assertThat(response.getResponseBody().ensuingDecisions().get(1).getFileNumber())
                  .isNull();
            });
  }

  @Test
  void testUpdateDocumentationUnit_updateDocumentTypeInEnsuingDecision() {
    UUID uuid = UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3");
    UUID ensuingDecisionUUID1 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120002");
    UUID ensuingDecisionUUID2 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120003");
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr001")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().ensuingDecisions()).hasSize(2);
              assertThat(response.getResponseBody().ensuingDecisions().get(0).getDocumentType())
                  .extracting("label")
                  .isEqualTo("Beschluss");
              assertThat(response.getResponseBody().ensuingDecisions().get(1).getDocumentType())
                  .extracting("label")
                  .isEqualTo("Anordnung");
            });

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(uuid)
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .ensuingDecisions(
                List.of(
                    EnsuingDecision.builder()
                        .uuid(ensuingDecisionUUID1)
                        .documentType(
                            DocumentType.builder()
                                .uuid(UUID.fromString("0c64fc8f-806c-4c43-a80f-dc54500b2a5a"))
                                .jurisShortcut("AO")
                                .label("Anordnung")
                                .build())
                        .note("note1")
                        .build(),
                    EnsuingDecision.builder().uuid(ensuingDecisionUUID2).note("note2").build()))
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3")
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().ensuingDecisions()).hasSize(2);
              assertThat(response.getResponseBody().ensuingDecisions().get(0).getDocumentType())
                  .extracting("label")
                  .isEqualTo("Anordnung");
              assertThat(response.getResponseBody().ensuingDecisions().get(1).getDocumentType())
                  .isNull();
            });
  }

  @Test
  void testUpdateDocumentationUnit_updateNoteInEnsuingDecision() {
    UUID uuid = UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3");
    UUID ensuingDecisionUUID1 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120002");
    UUID ensuingDecisionUUID2 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120003");
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr001")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().ensuingDecisions()).hasSize(2);
              assertThat(response.getResponseBody().ensuingDecisions().get(0).getNote())
                  .isEqualTo("note1");
              assertThat(response.getResponseBody().ensuingDecisions().get(1).getNote())
                  .isEqualTo("note2");
            });

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(uuid)
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .ensuingDecisions(
                List.of(
                    EnsuingDecision.builder()
                        .uuid(ensuingDecisionUUID1)
                        .fileNumber("abc")
                        .note("note2")
                        .build(),
                    EnsuingDecision.builder()
                        .uuid(ensuingDecisionUUID2)
                        .fileNumber("cba")
                        .pending(true)
                        .build()))
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3")
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().ensuingDecisions()).hasSize(2);
              assertThat(response.getResponseBody().ensuingDecisions().get(0).getNote())
                  .isEqualTo("note2");
              assertThat(response.getResponseBody().ensuingDecisions().get(1).getNote()).isNull();
            });
  }

  @Test
  void
      testGetDocumentationUnit_withEnsuingDecisions_shouldReturnListWithLinkedAndNotLinkedDecisions() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr001")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().ensuingDecisions()).hasSize(2);
              assertThat(response.getResponseBody().ensuingDecisions().get(0).getDocumentNumber())
                  .isNull();
              assertThat(response.getResponseBody().ensuingDecisions().get(1).getDocumentNumber())
                  .isEqualTo("documentnr002");
            });
  }

  @Test
  void testUpdateDocumentationUnit_addLinkedEnsuingDecision() {
    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3"))
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .ensuingDecisions(
                List.of(EnsuingDecision.builder().documentNumber("documentnr002").build()))
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3")
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().ensuingDecisions()).hasSize(1);
              assertThat(response.getResponseBody().ensuingDecisions().get(0).getDocumentNumber())
                  .isEqualTo("documentnr002");
            });
  }

  @Test
  void testUpdateDocumentationUnit_removeLinkedEnsuingDecision() {
    UUID uuid = UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3");
    UUID ensuingDecisionUUID1 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120002");
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr001")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> assertThat(response.getResponseBody().ensuingDecisions()).hasSize(2));

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(uuid)
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .ensuingDecisions(
                List.of(
                    EnsuingDecision.builder()
                        .uuid(ensuingDecisionUUID1)
                        .fileNumber("abc")
                        .note("note2")
                        .build()))
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3")
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              assertThat(response.getResponseBody().ensuingDecisions()).hasSize(1);
              assertThat(response.getResponseBody().ensuingDecisions().get(0).getDocumentNumber())
                  .isNull();
            });
  }
}
