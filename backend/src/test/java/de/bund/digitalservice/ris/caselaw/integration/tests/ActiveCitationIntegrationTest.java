package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.domain.ActiveCitation;
import de.bund.digitalservice.ris.caselaw.domain.ContentRelatedIndexing;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationType;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

@Sql(scripts = {"classpath:doc_office_init.sql", "classpath:active_citations_init.sql"})
@Sql(
    scripts = {"classpath:active_citations_cleanup.sql"},
    executionPhase = AFTER_TEST_METHOD)
class ActiveCitationIntegrationTest extends BaseIntegrationTest {

  @Autowired private RisWebTestClient risWebTestClient;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @Autowired private DatabaseDocumentationUnitRepository databaseDocumentationUnitRepository;

  DocumentationOfficeDTO docOfficeDTO;

  @BeforeEach
  void setUp() {
    docOfficeDTO = documentationOfficeRepository.findByAbbreviation("DS");
  }

  @AfterEach
  void cleanUp() {
    databaseDocumentationUnitRepository.deleteAll();
  }

  @Test
  void testGetDocumentationUnit_withoutActiveCitation_shouldReturnEmptyList() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr002")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().contentRelatedIndexing().activeCitations())
                    .isEmpty());
  }

  @Test
  void testGetDocumentationUnit_withActiveCitation_shouldReturnList() {
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
              assertThat(response.getResponseBody().contentRelatedIndexing().activeCitations())
                  .hasSize(2);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getCitationType())
                  .extracting("label")
                  .isEqualTo("Anwendung");
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(1)
                          .getCitationType())
                  .extracting("label")
                  .isEqualTo("Nichtanwendung");
            });
  }

  @Test
  void testUpdateDocumentationUnit_addNewActiveCitation() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr001")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().contentRelatedIndexing().activeCitations())
                    .hasSize(2));

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3"))
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .activeCitations(
                        List.of(
                            ActiveCitation.builder()
                                .uuid(UUID.fromString("f0232240-7416-11ee-b962-0242ac120002"))
                                .citationType(
                                    CitationType.builder()
                                        .uuid(
                                            UUID.fromString("4e768071-1a19-43a1-8ab9-c185adec94bf"))
                                        .jurisShortcut("Anwendung")
                                        .label("Anwendung")
                                        .build())
                                .build(),
                            ActiveCitation.builder()
                                .uuid(UUID.fromString("f0232240-7416-11ee-b962-0242ac120003"))
                                .citationType(
                                    CitationType.builder()
                                        .uuid(
                                            UUID.fromString("6b4bd747-fce9-4e49-8af4-3fb4f1d3663c"))
                                        .build())
                                .build(),
                            ActiveCitation.builder()
                                .citationType(
                                    CitationType.builder()
                                        .uuid(
                                            UUID.fromString("6b4bd747-fce9-4e49-8af4-3fb4f1d3663c"))
                                        .build())
                                .build(),
                            ActiveCitation.builder()
                                .citationType(
                                    CitationType.builder()
                                        .uuid(
                                            UUID.fromString("6b4bd747-fce9-4e49-8af4-3fb4f1d3663c"))
                                        .build())
                                .build()))
                    .build())
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
              assertThat(response.getResponseBody().contentRelatedIndexing().activeCitations())
                  .hasSize(4);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(2)
                          .getCitationType())
                  .extracting("label")
                  .isEqualTo("Nichtanwendung");
            });
  }

  @Test
  void testUpdateDocumentationUnit_updateExistingActiveCitation() {
    UUID uuid = UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3");
    UUID activeCitationUUID1 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120002");
    UUID activeCitationUUID2 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120003");

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
              assertThat(response.getResponseBody().contentRelatedIndexing().activeCitations())
                  .hasSize(2);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getCitationType())
                  .extracting("label")
                  .isEqualTo("Anwendung");
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getFileNumber())
                  .isEqualTo("abc");
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getUuid())
                  .isEqualTo(activeCitationUUID1);
            });

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(uuid)
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .activeCitations(
                        List.of(
                            ActiveCitation.builder()
                                .uuid(activeCitationUUID1)
                                .citationType(
                                    CitationType.builder()
                                        .uuid(
                                            UUID.fromString("6b4bd747-fce9-4e49-8af4-3fb4f1d3663c"))
                                        .build())
                                .fileNumber("cba")
                                .build(),
                            ActiveCitation.builder()
                                .uuid(activeCitationUUID2)
                                .citationType(
                                    CitationType.builder()
                                        .uuid(
                                            UUID.fromString("6b4bd747-fce9-4e49-8af4-3fb4f1d3663c"))
                                        .build())
                                .build()))
                    .build())
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
              assertThat(response.getResponseBody().contentRelatedIndexing().activeCitations())
                  .hasSize(2);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getCitationType())
                  .extracting("label")
                  .isEqualTo("Nichtanwendung");
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getFileNumber())
                  .isEqualTo("cba");
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getUuid())
                  .isEqualTo(activeCitationUUID1);
            });
  }

  @Test
  void testUpdateDocumentationUnit_tryToAddAEmptyActiveCitation_shouldNotSucceed() {
    UUID activeCitationUUID1 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120002");
    UUID activeCitationUUID2 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120003");
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr001")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().contentRelatedIndexing().activeCitations())
                    .hasSize(2));

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3"))
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .activeCitations(
                        List.of(
                            ActiveCitation.builder()
                                .uuid(activeCitationUUID1)
                                .citationType(
                                    CitationType.builder()
                                        .uuid(
                                            UUID.fromString("6b4bd747-fce9-4e49-8af4-3fb4f1d3663c"))
                                        .build())
                                .fileNumber("abc")
                                .build(),
                            ActiveCitation.builder()
                                .uuid(activeCitationUUID2)
                                .citationType(
                                    CitationType.builder()
                                        .uuid(
                                            UUID.fromString("6b4bd747-fce9-4e49-8af4-3fb4f1d3663c"))
                                        .build())
                                .build(),
                            ActiveCitation.builder().build()))
                    .build())
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
            response ->
                assertThat(response.getResponseBody().contentRelatedIndexing().activeCitations())
                    .hasSize(2));
  }

  @Test
  void testUpdateDocumentationUnit_removeValuesDeletesActiveCitation() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr001")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().contentRelatedIndexing().activeCitations())
                    .hasSize(2));

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3"))
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .activeCitations(
                        List.of(
                            ActiveCitation.builder()
                                .uuid(UUID.fromString("f0232240-7416-11ee-b962-0242ac120002"))
                                .citationType(
                                    CitationType.builder()
                                        .uuid(
                                            UUID.fromString("4e768071-1a19-43a1-8ab9-c185adec94bf"))
                                        .build())
                                .build(),
                            ActiveCitation.builder()
                                .uuid(UUID.fromString("7da39a1e-78a9-11ee-b962-0242ac120002"))
                                .build()))
                    .build())
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
            response ->
                assertThat(response.getResponseBody().contentRelatedIndexing().activeCitations())
                    .hasSize(1));
  }

  @Test
  void testUpdateDocumentationUnit_removeActiveCitation() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr001")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().contentRelatedIndexing().activeCitations())
                    .hasSize(2));

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3"))
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .activeCitations(
                        List.of(
                            ActiveCitation.builder()
                                .uuid(UUID.fromString("f0232240-7416-11ee-b962-0242ac120003"))
                                .citationType(
                                    CitationType.builder()
                                        .uuid(
                                            UUID.fromString("6b4bd747-fce9-4e49-8af4-3fb4f1d3663c"))
                                        .build())
                                .build()))
                    .build())
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
            response ->
                assertThat(response.getResponseBody().contentRelatedIndexing().activeCitations())
                    .hasSize(1));
  }

  @Test
  void testUpdateDocumentationUnit_removeAllActiveCitation() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr001")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().contentRelatedIndexing().activeCitations())
                    .hasSize(2));

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3"))
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .contentRelatedIndexing(
                // Todo is this a realistic scenario to delete all active citations?
                ContentRelatedIndexing.builder()
                    .activeCitations(List.of(ActiveCitation.builder().build()))
                    .build())
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
            response ->
                assertThat(response.getResponseBody().contentRelatedIndexing().activeCitations())
                    .isEmpty());
  }

  @Test
  void testUpdateDocumentationUnit_withListOfMixedVariations() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr001")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().contentRelatedIndexing().activeCitations())
                    .hasSize(2));
    UUID uuid = UUID.fromString("f0232240-7416-11ee-b962-0242ac120002");

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3"))
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .activeCitations(
                        List.of(
                            ActiveCitation.builder()
                                .uuid(uuid)
                                .citationType(
                                    CitationType.builder()
                                        .uuid(
                                            UUID.fromString("6b4bd747-fce9-4e49-8af4-3fb4f1d3663c"))
                                        .build())
                                .fileNumber("abc")
                                .build(),
                            ActiveCitation.builder()
                                .uuid(UUID.fromString("f0232240-7416-11ee-b962-0242ac120003"))
                                .build(),
                            ActiveCitation.builder()
                                .citationType(
                                    CitationType.builder()
                                        .uuid(
                                            UUID.fromString("4e768071-1a19-43a1-8ab9-c185adec94bf"))
                                        .build())
                                .build(),
                            ActiveCitation.builder().build()))
                    .build())
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
              assertThat(response.getResponseBody().contentRelatedIndexing().activeCitations())
                  .hasSize(2);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getCitationType())
                  .extracting("label")
                  .isEqualTo("Nichtanwendung");
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getUuid())
                  .isEqualTo(uuid);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(1)
                          .getCitationType())
                  .extracting("label")
                  .isEqualTo("Anwendung");
            });
  }

  @Test
  void testUpdateDocumentationUnit_removeCourtInActiveCitation() {
    UUID activeCitationUUID1 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120002");
    UUID activeCitationUUID2 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120003");
    UUID uuid = UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3");
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
              assertThat(response.getResponseBody().contentRelatedIndexing().activeCitations())
                  .hasSize(2);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getCourt())
                  .isNotNull();
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getCourt())
                  .extracting("id")
                  .isEqualTo(UUID.fromString("96301f85-9bd2-4690-a67f-f9fdfe725de3"));
            });

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(uuid)
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .activeCitations(
                        List.of(
                            ActiveCitation.builder()
                                .uuid(activeCitationUUID1)
                                .citationType(
                                    CitationType.builder()
                                        .uuid(
                                            UUID.fromString("6b4bd747-fce9-4e49-8af4-3fb4f1d3663c"))
                                        .build())
                                .build(),
                            ActiveCitation.builder()
                                .uuid(activeCitationUUID2)
                                .citationType(
                                    CitationType.builder()
                                        .uuid(
                                            UUID.fromString("6b4bd747-fce9-4e49-8af4-3fb4f1d3663c"))
                                        .build())
                                .build()))
                    .build())
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
              assertThat(response.getResponseBody().contentRelatedIndexing().activeCitations())
                  .hasSize(2);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getCourt())
                  .isNull();
            });
  }

  @Test
  void testUpdateDocumentationUnit_removeCitationStyleInActiveCitation() {
    UUID activeCitationUUID1 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120002");
    UUID activeCitationUUID2 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120003");
    UUID uuid = UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3");
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
              assertThat(response.getResponseBody().contentRelatedIndexing().activeCitations())
                  .hasSize(2);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getCitationType())
                  .extracting("uuid")
                  .isEqualTo(UUID.fromString("4e768071-1a19-43a1-8ab9-c185adec94bf"));
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getCitationType())
                  .extracting("label")
                  .isEqualTo("Anwendung");
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(1)
                          .getCitationType())
                  .extracting("uuid")
                  .isEqualTo(UUID.fromString("6b4bd747-fce9-4e49-8af4-3fb4f1d3663c"));
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(1)
                          .getCitationType())
                  .extracting("label")
                  .isEqualTo("Nichtanwendung");
            });

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(uuid)
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .activeCitations(
                        List.of(
                            ActiveCitation.builder()
                                .uuid(activeCitationUUID1)
                                .court(
                                    Court.builder()
                                        .id(UUID.fromString("96301f85-9bd2-4690-a67f-f9fdfe725de3"))
                                        .location("Karlsruhe")
                                        .type("BGH")
                                        .build())
                                .build(),
                            ActiveCitation.builder().uuid(activeCitationUUID2).build()))
                    .build())
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
              assertThat(response.getResponseBody().contentRelatedIndexing().activeCitations())
                  .hasSize(1);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getCitationType())
                  .isNull();
            });
  }

  @Test
  void testUpdateDocumentationUnit_removeDocumentTypeInActiveCitation() {
    UUID activeCitationUUID1 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120002");
    UUID activeCitationUUID2 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120003");
    UUID uuid = UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3");
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
              assertThat(response.getResponseBody().contentRelatedIndexing().activeCitations())
                  .hasSize(2);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getDocumentType())
                  .extracting("label")
                  .isEqualTo("Beschluss");
            });

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(uuid)
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .activeCitations(
                        List.of(
                            ActiveCitation.builder()
                                .uuid(activeCitationUUID1)
                                .court(
                                    Court.builder()
                                        .id(UUID.fromString("96301f85-9bd2-4690-a67f-f9fdfe725de3"))
                                        .location("Karlsruhe")
                                        .type("BGH")
                                        .build())
                                .build(),
                            ActiveCitation.builder().uuid(activeCitationUUID2).build()))
                    .build())
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
              assertThat(response.getResponseBody().contentRelatedIndexing().activeCitations())
                  .hasSize(1);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getDocumentType())
                  .isNull();
            });
  }

  @Test
  void testUpdateDocumentationUnit_removeFileNumberInActiveCitation() {
    UUID activeCitationUUID1 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120002");
    UUID activeCitationUUID2 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120003");
    UUID uuid = UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3");
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
              assertThat(response.getResponseBody().contentRelatedIndexing().activeCitations())
                  .hasSize(2);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getFileNumber())
                  .isEqualTo("abc");
            });

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(uuid)
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .activeCitations(
                        List.of(
                            ActiveCitation.builder()
                                .uuid(activeCitationUUID1)
                                .court(
                                    Court.builder()
                                        .id(UUID.fromString("96301f85-9bd2-4690-a67f-f9fdfe725de3"))
                                        .location("Karlsruhe")
                                        .type("BGH")
                                        .build())
                                .build(),
                            ActiveCitation.builder().uuid(activeCitationUUID2).build()))
                    .build())
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
              assertThat(response.getResponseBody().contentRelatedIndexing().activeCitations())
                  .hasSize(1);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getFileNumber())
                  .isNull();
            });
  }

  @Test
  void testUpdateDocumentationUnit_removeDecisionDateInActiveCitation() {
    UUID activeCitationUUID1 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120002");
    UUID activeCitationUUID2 = UUID.fromString("f0232240-7416-11ee-b962-0242ac120003");
    UUID uuid = UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3");
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
              assertThat(response.getResponseBody().contentRelatedIndexing().activeCitations())
                  .hasSize(2);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getDecisionDate())
                  .isEqualTo("2011-01-21");
            });

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(uuid)
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .activeCitations(
                        List.of(
                            ActiveCitation.builder()
                                .uuid(activeCitationUUID1)
                                .court(
                                    Court.builder()
                                        .id(UUID.fromString("96301f85-9bd2-4690-a67f-f9fdfe725de3"))
                                        .location("Karlsruhe")
                                        .type("BGH")
                                        .build())
                                .build(),
                            ActiveCitation.builder().uuid(activeCitationUUID2).build()))
                    .build())
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
              assertThat(response.getResponseBody().contentRelatedIndexing().activeCitations())
                  .hasSize(1);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getDecisionDate())
                  .isNull();
            });
  }

  @Test
  void
      testGetDocumentationUnit_withActiveCitations_shouldReturnListWithLinkedAndNotLinkedActiveCitations() {
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
              assertThat(response.getResponseBody().contentRelatedIndexing().activeCitations())
                  .hasSize(2);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getDocumentNumber())
                  .isNull();
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(1)
                          .getDocumentNumber())
                  .isEqualTo("documentnr002");
            });
  }

  @Test
  void testUpdateDocumentationUnit_addLinkedActiveCitation() {
    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3"))
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .activeCitations(
                        List.of(ActiveCitation.builder().documentNumber("documentnr002").build()))
                    .build())
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
              assertThat(response.getResponseBody().contentRelatedIndexing().activeCitations())
                  .hasSize(1);
              assertThat(
                      response
                          .getResponseBody()
                          .contentRelatedIndexing()
                          .activeCitations()
                          .get(0)
                          .getDocumentNumber())
                  .isEqualTo("documentnr002");
            });
  }

  @Test
  void testUpdateDocumentationUnit_removeLinkedActiveCitation() {
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr001")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().contentRelatedIndexing().activeCitations())
                    .hasSize(2));

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3"))
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .activeCitations(
                        List.of(
                            ActiveCitation.builder()
                                .uuid(UUID.fromString("f0232240-7416-11ee-b962-0242ac120002"))
                                .citationType(
                                    CitationType.builder()
                                        .uuid(
                                            UUID.fromString("4e768071-1a19-43a1-8ab9-c185adec94bf"))
                                        .build())
                                .build()))
                    .build())
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
            response ->
                assertThat(response.getResponseBody().contentRelatedIndexing().activeCitations())
                    .hasSize(1));
  }

  // TODO: (Malte Laukötter, 2026-02-10) remove once the refactoring of the citation tables is done
  @Transactional
  @Test
  void testUpdateDocumentationUnit_alsoUpdatesNewTables() {
    TestTransaction.end();
    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/documentnr001")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().contentRelatedIndexing().activeCitations())
                    .hasSize(2));

    Decision decisionFromFrontend =
        Decision.builder()
            .uuid(UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3"))
            .documentNumber("documentnr001")
            .coreData(CoreData.builder().build())
            .contentRelatedIndexing(
                ContentRelatedIndexing.builder()
                    .activeCitations(
                        List.of(
                            ActiveCitation.builder()
                                .uuid(UUID.fromString("f0232240-7416-11ee-b962-0242ac120002"))
                                .documentNumber("documentnr002")
                                .citationType(
                                    CitationType.builder()
                                        .uuid(
                                            UUID.fromString("4e768071-1a19-43a1-8ab9-c185adec94bf"))
                                        .jurisShortcut("Anwendung")
                                        .label("Anwendung")
                                        .build())
                                .build(),
                            ActiveCitation.builder()
                                .uuid(UUID.fromString("f0232240-7416-11ee-b962-0242ac120003"))
                                .documentNumber("documentnr512") // does not exist
                                .citationType(
                                    CitationType.builder()
                                        .uuid(
                                            UUID.fromString("6b4bd747-fce9-4e49-8af4-3fb4f1d3663c"))
                                        .build())
                                .build(),
                            ActiveCitation.builder()
                                .citationType(
                                    CitationType.builder()
                                        .uuid(
                                            UUID.fromString("6b4bd747-fce9-4e49-8af4-3fb4f1d3663c"))
                                        .build())
                                .build(),
                            ActiveCitation.builder()
                                .citationType(
                                    CitationType.builder()
                                        .uuid(
                                            UUID.fromString("6b4bd747-fce9-4e49-8af4-3fb4f1d3663c"))
                                        .build())
                                .build()))
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3")
        .bodyValue(decisionFromFrontend)
        .exchange()
        .expectStatus()
        .isOk();

    TestTransaction.start();
    var documentationUnitDTO =
        databaseDocumentationUnitRepository.findById(
            UUID.fromString("46f9ae5c-ea72-46d8-864c-ce9dd7cee4a3"));
    assertThat(documentationUnitDTO).isNotEmpty();
    var decisionDTO = (DecisionDTO) documentationUnitDTO.get();
    assertThat(decisionDTO.getDocumentNumber()).isEqualTo("documentnr001");
    assertThat(decisionDTO.getActiveCaselawCitations()).hasSize(4);
    assertThat(decisionDTO.getActiveCaselawCitations().getFirst().getSource().getDocumentNumber())
        .isEqualTo("documentnr001");
    assertThat(decisionDTO.getActiveCaselawCitations().getFirst().getTargetDocumentNumber())
        .isEqualTo("documentnr002");
    assertThat(decisionDTO.getActiveCaselawCitations().getFirst().getRank()).isEqualTo(1);
    assertThat(decisionDTO.getActiveCaselawCitations().get(1).getRank()).isEqualTo(2);
    assertThat(decisionDTO.getActiveCaselawCitations().get(2).getRank()).isEqualTo(3);
    assertThat(decisionDTO.getActiveCaselawCitations().get(3).getRank()).isEqualTo(4);
    TestTransaction.end();
  }
}
