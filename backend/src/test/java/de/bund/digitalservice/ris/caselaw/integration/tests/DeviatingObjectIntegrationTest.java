package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDSDocOffice;
import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.EntityBuilderTestUtil;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingCourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingDateDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingEcliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingFileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class DeviatingObjectIntegrationTest extends BaseIntegrationTest {

  @Autowired private RisWebTestClient risWebTestClient;
  @Autowired private DatabaseDocumentationUnitRepository repository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;

  private final DocumentationOffice docOffice = buildDSDocOffice();

  private DocumentationUnitDTO prepareDocumentationUnitDTOWithDeviatingFileNumbers() {
    return EntityBuilderTestUtil.createAndSaveDecision(
        repository,
        DecisionDTO.builder()
            .documentNumber("1234567890123")
            .documentationOffice(
                documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
            .deviatingFileNumbers(
                List.of(
                    DeviatingFileNumberDTO.builder().rank(1L).value("dfn1").build(),
                    DeviatingFileNumberDTO.builder().rank(2L).value("dfn2").build())));
  }

  @AfterEach
  void cleanUp() {
    repository.deleteAll();
  }

  // Deviating File Number
  @Test
  void testReadOfDeviatingFileNumbers() {
    prepareDocumentationUnitDTOWithDeviatingFileNumbers();

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/1234567890123")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              List<String> deviatingFileNumbers =
                  response.getResponseBody().coreData().deviatingFileNumbers();
              assertThat(deviatingFileNumbers).hasSize(2);
              assertThat(deviatingFileNumbers).containsExactly("dfn1", "dfn2");
            });
  }

  @Test
  void testAddANewDeviatingFileNumberToAnExistingList() {
    DocumentationUnitDTO savedDTO = prepareDocumentationUnitDTOWithDeviatingFileNumbers();

    Decision decision =
        Decision.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .deviatingFileNumbers(List.of("dfn1", "dfn2", "dfn3"))
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(decision)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              List<String> deviatingFileNumbers =
                  response.getResponseBody().coreData().deviatingFileNumbers();
              assertThat(deviatingFileNumbers).hasSize(3);
              assertThat(deviatingFileNumbers).containsExactly("dfn1", "dfn2", "dfn3");
            });
  }

  @Test
  void testAddADeviatingFileNumberTwiceToAnExistingList() {
    DocumentationUnitDTO savedDTO = prepareDocumentationUnitDTOWithDeviatingFileNumbers();

    Decision decision =
        Decision.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .deviatingFileNumbers(List.of("dfn1", "dfn2", "dfn2"))
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(decision)
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  void testRemoveOneDeviatingFileNumberFromExistingList() {
    DocumentationUnitDTO savedDTO = prepareDocumentationUnitDTOWithDeviatingFileNumbers();

    Decision decision =
        Decision.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .deviatingFileNumbers(List.of("dfn2"))
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(decision)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              List<String> deviatingFileNumbers =
                  response.getResponseBody().coreData().deviatingFileNumbers();
              assertThat(deviatingFileNumbers).hasSize(1);
              assertThat(deviatingFileNumbers).containsExactly("dfn2");
            });
  }

  @Test
  void testRemoveAllDeviatingFileNumberWithAEmplyListFromExistingList() {
    DocumentationUnitDTO savedDTO = prepareDocumentationUnitDTOWithDeviatingFileNumbers();

    Decision decision =
        Decision.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .deviatingFileNumbers(Collections.emptyList())
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(decision)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              List<String> deviatingFileNumbers =
                  response.getResponseBody().coreData().deviatingFileNumbers();
              assertThat(deviatingFileNumbers).isEmpty();
            });
  }

  @Test
  void testWithNullDontChangeTheExistingDeviatingFileNumbers() {
    DocumentationUnitDTO savedDTO = prepareDocumentationUnitDTOWithDeviatingFileNumbers();

    Decision decision =
        Decision.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(decision)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              List<String> deviatingFileNumbers =
                  response.getResponseBody().coreData().deviatingFileNumbers();
              assertThat(deviatingFileNumbers).hasSize(2);
              assertThat(deviatingFileNumbers).containsExactly("dfn1", "dfn2");
            });
  }

  // Deviating ECLI
  @Test
  void testReadOfDeviatingECLI() {
    EntityBuilderTestUtil.createAndSaveDecision(
        repository,
        DecisionDTO.builder()
            .documentNumber("1234567890123")
            .documentationOffice(
                documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
            .deviatingEclis(
                List.of(
                    DeviatingEcliDTO.builder().rank(1L).value("decli1").build(),
                    DeviatingEcliDTO.builder().rank(2L).value("decli2").build())));

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/1234567890123")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              List<String> deviatingEclis = response.getResponseBody().coreData().deviatingEclis();
              assertThat(deviatingEclis).hasSize(2);
              assertThat(deviatingEclis).containsExactly("decli1", "decli2");
            });
  }

  @Test
  void testAddANewDeviatingEcliToAnExistingList() {
    var savedDTO =
        EntityBuilderTestUtil.createAndSaveDecision(
            repository,
            DecisionDTO.builder()
                .documentNumber("1234567890123")
                .documentationOffice(
                    documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
                .deviatingEclis(
                    List.of(
                        DeviatingEcliDTO.builder().rank(1L).value("decli1").build(),
                        DeviatingEcliDTO.builder().rank(2L).value("decli2").build())));

    Decision decision =
        Decision.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    //
                    // .documentationOffice(documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
                    .deviatingEclis(List.of("decli1", "decli2", "decli3"))
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(decision)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              List<String> deviatingEclis = response.getResponseBody().coreData().deviatingEclis();
              assertThat(deviatingEclis).hasSize(3);
              assertThat(deviatingEclis).containsExactly("decli1", "decli2", "decli3");
            });
  }

  @Test
  void testAddADeviatingEcliTwiceToAnExistingList() {
    var savedDTO =
        EntityBuilderTestUtil.createAndSaveDecision(
            repository,
            DecisionDTO.builder()
                .documentNumber("1234567890123")
                .documentationOffice(
                    documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
                .deviatingEclis(
                    List.of(
                        DeviatingEcliDTO.builder().rank(1L).value("decli1").build(),
                        DeviatingEcliDTO.builder().rank(2L).value("decli2").build())));

    Decision decision =
        Decision.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .deviatingEclis(List.of("decli1", "decli2", "decli2"))
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(decision)
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  void testRemoveOneDeviatingEcliFromExistingList() {
    var savedDTO =
        EntityBuilderTestUtil.createAndSaveDecision(
            repository,
            DecisionDTO.builder()
                .documentNumber("1234567890123")
                .documentationOffice(
                    documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
                .deviatingEclis(
                    List.of(
                        DeviatingEcliDTO.builder().rank(1L).value("decli1").build(),
                        DeviatingEcliDTO.builder().rank(2L).value("decli2").build())));

    Decision decision =
        Decision.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .deviatingEclis(List.of("decli2"))
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(decision)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              List<String> deviatingEclis = response.getResponseBody().coreData().deviatingEclis();
              assertThat(deviatingEclis).hasSize(1);
              assertThat(deviatingEclis).containsExactly("decli2");
            });
  }

  @Test
  void testRemoveAllDeviatingEcliWithAEmplyListFromExistingList() {
    var savedDTO =
        EntityBuilderTestUtil.createAndSaveDecision(
            repository,
            DecisionDTO.builder()
                .documentNumber("1234567890123")
                .documentationOffice(
                    documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
                .deviatingEclis(
                    List.of(
                        DeviatingEcliDTO.builder().rank(1L).value("decli1").build(),
                        DeviatingEcliDTO.builder().rank(2L).value("decli2").build())));

    Decision decision =
        Decision.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .deviatingEclis(Collections.emptyList())
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(decision)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              List<String> deviatingEclis = response.getResponseBody().coreData().deviatingEclis();
              assertThat(deviatingEclis).isEmpty();
            });
  }

  @Test
  void testWithNullDontChangeTheExistingDeviatingEclis() {
    var savedDTO =
        EntityBuilderTestUtil.createAndSaveDecision(
            repository,
            DecisionDTO.builder()
                .documentNumber("1234567890123")
                .documentationOffice(
                    documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
                .deviatingEclis(
                    List.of(
                        DeviatingEcliDTO.builder().rank(1L).value("decli1").build(),
                        DeviatingEcliDTO.builder().rank(2L).value("decli2").build())));

    Decision decision =
        Decision.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(decision)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              List<String> deviatingEclis = response.getResponseBody().coreData().deviatingEclis();
              assertThat(deviatingEclis).hasSize(2);
              assertThat(deviatingEclis).containsExactly("decli1", "decli2");
            });
  }

  // Deviating Court

  @Test
  void testReadOfDeviatingCourts() {
    EntityBuilderTestUtil.createAndSaveDecision(
        repository,
        DecisionDTO.builder()
            .documentNumber("1234567890123")
            .documentationOffice(
                documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
            .deviatingCourts(
                List.of(
                    DeviatingCourtDTO.builder().rank(1L).value("dc1").build(),
                    DeviatingCourtDTO.builder().rank(2L).value("dc2").build())));

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/1234567890123")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              List<String> deviatingCourts =
                  response.getResponseBody().coreData().deviatingCourts();
              assertThat(deviatingCourts).hasSize(2);
              assertThat(deviatingCourts).containsExactly("dc1", "dc2");
            });
  }

  @Test
  void testAddANewDeviatingCourtToAnExistingList() {
    var savedDTO =
        EntityBuilderTestUtil.createAndSaveDecision(
            repository,
            DecisionDTO.builder()
                .documentNumber("1234567890123")
                .documentationOffice(
                    documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
                .deviatingCourts(
                    List.of(
                        DeviatingCourtDTO.builder().rank(1L).value("dc1").build(),
                        DeviatingCourtDTO.builder().rank(2L).value("dc2").build())));

    Decision decision =
        Decision.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .deviatingCourts(List.of("dc1", "dc2", "dc3"))
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(decision)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              List<String> deviatingCourts =
                  response.getResponseBody().coreData().deviatingCourts();
              assertThat(deviatingCourts).hasSize(3);
              assertThat(deviatingCourts).containsExactly("dc1", "dc2", "dc3");
            });
  }

  @Test
  void testAddADeviatingCourtTwiceToAnExistingList() {
    var savedDTO =
        EntityBuilderTestUtil.createAndSaveDecision(
            repository,
            DecisionDTO.builder()
                .documentNumber("1234567890123")
                .documentationOffice(
                    documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
                .deviatingCourts(
                    List.of(
                        DeviatingCourtDTO.builder().rank(1L).value("dc1").build(),
                        DeviatingCourtDTO.builder().rank(2L).value("dc2").build())));

    Decision decision =
        Decision.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .deviatingFileNumbers(List.of("dfn1", "dfn2", "dfn2"))
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(decision)
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  void testRemoveOneDeviatingCourtFromExistingList() {
    var savedDTO =
        EntityBuilderTestUtil.createAndSaveDecision(
            repository,
            DecisionDTO.builder()
                .documentNumber("1234567890123")
                .documentationOffice(
                    documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
                .deviatingCourts(
                    List.of(
                        DeviatingCourtDTO.builder().rank(1L).value("dc1").build(),
                        DeviatingCourtDTO.builder().rank(2L).value("dc2").build())));

    Decision decision =
        Decision.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .deviatingCourts(List.of("dc2"))
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(decision)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              List<String> deviatingCourts =
                  response.getResponseBody().coreData().deviatingCourts();
              assertThat(deviatingCourts).hasSize(1);
              assertThat(deviatingCourts).containsExactly("dc2");
            });
  }

  @Test
  void testRemoveAllDeviatingCourtWithAEmplyListFromExistingList() {
    var savedDTO =
        EntityBuilderTestUtil.createAndSaveDecision(
            repository,
            DecisionDTO.builder()
                .documentNumber("1234567890123")
                .documentationOffice(
                    documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
                .deviatingCourts(
                    List.of(
                        DeviatingCourtDTO.builder().rank(1L).value("dc1").build(),
                        DeviatingCourtDTO.builder().rank(2L).value("dc2").build())));

    Decision decision =
        Decision.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .deviatingCourts(Collections.emptyList())
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(decision)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              List<String> deviatingCourts =
                  response.getResponseBody().coreData().deviatingCourts();
              assertThat(deviatingCourts).isEmpty();
            });
  }

  @Test
  void testWithNullDontChangeTheExistingDeviatingCourts() {
    var savedDTO =
        EntityBuilderTestUtil.createAndSaveDecision(
            repository,
            DecisionDTO.builder()
                .documentNumber("1234567890123")
                .documentationOffice(
                    documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
                .deviatingCourts(
                    List.of(
                        DeviatingCourtDTO.builder().rank(1L).value("dc1").build(),
                        DeviatingCourtDTO.builder().rank(2L).value("dc2").build())));

    Decision decision =
        Decision.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(decision)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              List<String> deviatingCourts =
                  response.getResponseBody().coreData().deviatingCourts();
              assertThat(deviatingCourts).hasSize(2);
              assertThat(deviatingCourts).containsExactly("dc1", "dc2");
            });
  }

  // Deviating Date

  @Test
  void testReadOfDeviatingDates() {

    EntityBuilderTestUtil.createAndSaveDecision(
        repository,
        DecisionDTO.builder()
            .documentNumber("1234567890123")
            .documentationOffice(
                documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
            .deviatingDates(
                List.of(
                    DeviatingDateDTO.builder().rank(1L).value(LocalDate.of(2000, 1, 2)).build(),
                    DeviatingDateDTO.builder().rank(2L).value(LocalDate.of(2010, 9, 10)).build())));

    risWebTestClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/documentunits/1234567890123")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              List<LocalDate> deviatingDates =
                  response.getResponseBody().coreData().deviatingDecisionDates();
              assertThat(deviatingDates).hasSize(2);
              assertThat(deviatingDates)
                  .containsExactly(LocalDate.of(2000, 1, 2), LocalDate.of(2010, 9, 10));
            });
  }

  @Test
  void testAddANewDeviatingDatesToAnExistingList() {
    var savedDTO =
        EntityBuilderTestUtil.createAndSaveDecision(
            repository,
            DecisionDTO.builder()
                .documentNumber("1234567890123")
                .documentationOffice(
                    documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
                .deviatingDates(
                    List.of(
                        DeviatingDateDTO.builder().rank(1L).value(LocalDate.of(2000, 1, 2)).build(),
                        DeviatingDateDTO.builder()
                            .rank(2L)
                            .value(LocalDate.of(2010, 9, 10))
                            .build())));

    Decision decision =
        Decision.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .deviatingDecisionDates(
                        List.of(
                            LocalDate.of(2000, 1, 2),
                            LocalDate.of(2010, 9, 10),
                            LocalDate.of(2020, 4, 5)))
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(decision)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              List<LocalDate> deviatingDates =
                  response.getResponseBody().coreData().deviatingDecisionDates();
              assertThat(deviatingDates).hasSize(3);
              assertThat(deviatingDates)
                  .containsExactly(
                      LocalDate.of(2000, 1, 2),
                      LocalDate.of(2010, 9, 10),
                      LocalDate.of(2020, 4, 5));
            });
  }

  @Test
  void testAddADeviatingDateTwiceToAnExistingList() {
    var savedDTO =
        EntityBuilderTestUtil.createAndSaveDecision(
            repository,
            DecisionDTO.builder()
                .documentNumber("1234567890123")
                .documentationOffice(
                    documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
                .deviatingDates(
                    List.of(
                        DeviatingDateDTO.builder().rank(1L).value(LocalDate.of(2000, 1, 2)).build(),
                        DeviatingDateDTO.builder()
                            .rank(2L)
                            .value(LocalDate.of(2010, 9, 10))
                            .build(),
                        DeviatingDateDTO.builder()
                            .rank(3L)
                            .value(LocalDate.of(2010, 9, 10))
                            .build())));

    Decision decision =
        Decision.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .deviatingDecisionDates(
                        List.of(
                            LocalDate.of(2000, 1, 2),
                            LocalDate.of(2010, 9, 10),
                            LocalDate.of(2010, 9, 10)))
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(decision)
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  void testRemoveOneDeviatingDateFromExistingList() {
    var savedDTO =
        EntityBuilderTestUtil.createAndSaveDecision(
            repository,
            DecisionDTO.builder()
                .documentNumber("1234567890123")
                .documentationOffice(
                    documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
                .deviatingDates(
                    List.of(
                        DeviatingDateDTO.builder().rank(1L).value(LocalDate.of(2000, 1, 2)).build(),
                        DeviatingDateDTO.builder()
                            .rank(2L)
                            .value(LocalDate.of(2010, 9, 10))
                            .build())));

    Decision decision =
        Decision.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .deviatingDecisionDates(List.of(LocalDate.of(2010, 9, 10)))
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(decision)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              List<LocalDate> deviatingDates =
                  response.getResponseBody().coreData().deviatingDecisionDates();
              assertThat(deviatingDates).hasSize(1);
              assertThat(deviatingDates).containsExactly(LocalDate.of(2010, 9, 10));
            });
  }

  @Test
  void testRemoveAllDeviatingDatesWithAEmplyListFromExistingList() {
    var savedDTO =
        EntityBuilderTestUtil.createAndSaveDecision(
            repository,
            DecisionDTO.builder()
                .documentNumber("1234567890123")
                .documentationOffice(
                    documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
                .deviatingDates(
                    List.of(
                        DeviatingDateDTO.builder().rank(1L).value(LocalDate.of(2000, 1, 2)).build(),
                        DeviatingDateDTO.builder()
                            .rank(2L)
                            .value(LocalDate.of(2010, 9, 10))
                            .build())));

    Decision decision =
        Decision.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .deviatingDecisionDates(Collections.emptyList())
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(decision)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              List<LocalDate> deviatingDates =
                  response.getResponseBody().coreData().deviatingDecisionDates();
              assertThat(deviatingDates).isEmpty();
            });
  }

  @Test
  void testWithNullDontChangeExistingDeviatingDates() {
    var savedDTO =
        EntityBuilderTestUtil.createAndSaveDecision(
            repository,
            DecisionDTO.builder()
                .documentNumber("1234567890123")
                .documentationOffice(
                    documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation()))
                .deviatingDates(
                    List.of(
                        DeviatingDateDTO.builder().rank(1L).value(LocalDate.of(2000, 1, 2)).build(),
                        DeviatingDateDTO.builder()
                            .rank(2L)
                            .value(LocalDate.of(2010, 9, 10))
                            .build())));

    Decision decision =
        Decision.builder()
            .uuid(savedDTO.getId())
            .documentNumber("1234567890123")
            .coreData(
                CoreData.builder()
                    .documentationOffice(DocumentationOffice.builder().abbreviation("DS").build())
                    .build())
            .build();

    risWebTestClient
        .withDefaultLogin()
        .put()
        .uri("/api/v1/caselaw/documentunits/" + savedDTO.getId())
        .bodyValue(decision)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response -> {
              List<LocalDate> deviatingDates =
                  response.getResponseBody().coreData().deviatingDecisionDates();
              assertThat(deviatingDates).hasSize(2);
              assertThat(deviatingDates)
                  .containsExactly(LocalDate.of(2000, 1, 2), LocalDate.of(2010, 9, 10));
            });
  }
}
