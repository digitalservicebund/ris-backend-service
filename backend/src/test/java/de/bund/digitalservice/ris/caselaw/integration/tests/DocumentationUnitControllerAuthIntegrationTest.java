package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.domain.PublicationStatus.PUBLISHED;
import static de.bund.digitalservice.ris.caselaw.domain.PublicationStatus.PUBLISHING;
import static de.bund.digitalservice.ris.caselaw.domain.PublicationStatus.UNPUBLISHED;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import de.bund.digitalservice.ris.caselaw.EntityBuilderTestUtil;
import de.bund.digitalservice.ris.caselaw.SliceTestImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitListItem;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Slice;

class DocumentationUnitControllerAuthIntegrationTest extends BaseIntegrationTest {

  @Autowired private RisWebTestClient risWebTestClient;
  @Autowired private DatabaseDocumentationUnitRepository repository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;

  static Stream<Arguments> getUnauthorizedCases() {
    return Stream.of(
        Arguments.of("CC-RIS", "/BGH", UNPUBLISHED), Arguments.of("BGH", "/CC-RIS", UNPUBLISHED));
  }

  static Stream<Arguments> getAuthorizedCases() {
    return Stream.of(
        Arguments.of("CC-RIS", "/BGH", PUBLISHED),
        Arguments.of("CC-RIS", "/BGH", PUBLISHING),
        Arguments.of("BGH", "/BGH", UNPUBLISHED),
        Arguments.of("BGH", "/BGH", PUBLISHED),
        Arguments.of("BGH", "/BGH", PUBLISHING));
  }

  private DocumentationOfficeDTO ccRisOffice;
  private DocumentationOfficeDTO bghOffice;

  @BeforeEach
  void setUp() {
    // created via db migration V0_79__caselaw_insert_default_documentation_offices
    ccRisOffice = documentationOfficeRepository.findByAbbreviation("CC-RIS");
    bghOffice = documentationOfficeRepository.findByAbbreviation("BGH");
  }

  @AfterEach
  void cleanUp() {
    repository.deleteAll();
  }

  @ParameterizedTest
  @MethodSource("getAuthorizedCases")
  void testGetAll_shouldBeAccessible(
      String docUnitOfficeString, String userGroupPath, PublicationStatus publicationStatus) {

    DocumentationOfficeDTO docUnitOffice;
    if (docUnitOfficeString.equals(bghOffice.getAbbreviation())) {
      docUnitOffice = bghOffice;
    } else {
      docUnitOffice = ccRisOffice;
    }

    DecisionDTO documentationUnitDTO =
        EntityBuilderTestUtil.createAndSaveDecision(
            repository, createNewDocumentationUnitDTO(docUnitOffice), publicationStatus);

    Slice<DocumentationUnitListItem> docUnitsSearchResult =
        risWebTestClient
            .withLogin(userGroupPath)
            .get()
            .uri("/api/v1/caselaw/documentunits/search?pg=0&sz=10")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<SliceTestImpl<DocumentationUnitListItem>>() {})
            .returnResult()
            .getResponseBody();

    var docUnit =
        docUnitsSearchResult.stream()
            .filter(unit -> unit.uuid().equals(documentationUnitDTO.getId()))
            .findFirst()
            .get();
    assertThat(docUnit.status().publicationStatus()).isEqualTo(publicationStatus);

    risWebTestClient
        .withLogin(userGroupPath)
        .get()
        .uri("/api/v1/caselaw/documentunits/" + documentationUnitDTO.getDocumentNumber())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .returnResult();
  }

  @ParameterizedTest
  @MethodSource("getUnauthorizedCases")
  void testGetAll_shouldNotBeAccessible(
      String docUnitOfficeAbbreviation, String userGroupPath, PublicationStatus publicationStatus) {

    DocumentationOfficeDTO docUnitOffice;
    if (docUnitOfficeAbbreviation.equals(bghOffice.getAbbreviation())) {
      docUnitOffice = bghOffice;
    } else {
      docUnitOffice = ccRisOffice;
    }

    DocumentationUnitDTO documentationUnitDTO =
        EntityBuilderTestUtil.createAndSaveDecision(
            repository, createNewDocumentationUnitDTO(docUnitOffice), publicationStatus);

    Slice<DocumentationUnitListItem> docUnitsSearchResult =
        risWebTestClient
            .withLogin(userGroupPath)
            .get()
            .uri("/api/v1/caselaw/documentunits/search?pg=0&sz=10")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(new TypeReference<SliceTestImpl<DocumentationUnitListItem>>() {})
            .returnResult()
            .getResponseBody();

    assertThat(docUnitsSearchResult)
        .noneMatch(result -> result.uuid().equals(documentationUnitDTO.getId()));

    risWebTestClient
        .withLogin(userGroupPath)
        .get()
        .uri("/api/v1/caselaw/documentunits/" + documentationUnitDTO.getDocumentNumber())
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testUnpublishedDocumentationUnitIsForbiddenForOtherOffice() {
    DecisionDTO documentationUnitDTO =
        EntityBuilderTestUtil.createAndSaveDecision(
            repository, createNewDocumentationUnitDTO(ccRisOffice), UNPUBLISHED);

    // Documentation Office 1
    risWebTestClient
        .withLogin(("/CC-RIS"))
        .get()
        .uri("/api/v1/caselaw/documentunits/" + documentationUnitDTO.getDocumentNumber())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().uuid())
                    .isEqualTo(documentationUnitDTO.getId()));

    // Documentation Office 2
    risWebTestClient
        .withLogin("/BGH")
        .get()
        .uri("/api/v1/caselaw/documentunits/" + documentationUnitDTO.getDocumentNumber())
        .exchange()
        .expectStatus()
        .isForbidden();

    saveStatus(
        documentationUnitDTO,
        Instant.now().plus(1, ChronoUnit.DAYS),
        Status.builder().publicationStatus(PUBLISHING).build());

    risWebTestClient
        .withLogin(("/BGH"))
        .get()
        .uri("/api/v1/caselaw/documentunits/" + documentationUnitDTO.getDocumentNumber())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().uuid())
                    .isEqualTo(documentationUnitDTO.getId()));

    saveStatus(
        documentationUnitDTO,
        Instant.now().plus(2, ChronoUnit.DAYS),
        Status.builder().publicationStatus(PUBLISHED).build());

    risWebTestClient
        .withLogin(("/BGH"))
        .get()
        .uri("/api/v1/caselaw/documentunits/" + documentationUnitDTO.getDocumentNumber())
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Decision.class)
        .consumeWith(
            response ->
                assertThat(response.getResponseBody().uuid())
                    .isEqualTo(documentationUnitDTO.getId()));
  }

  private DecisionDTO.DecisionDTOBuilder createNewDocumentationUnitDTO(
      DocumentationOfficeDTO documentationOffice) {
    String documentNumber =
        new Random().ints(13, 0, 10).mapToObj(Integer::toString).collect(Collectors.joining());
    return DecisionDTO.builder()
        .documentNumber(documentNumber)
        .documentationOffice(documentationOffice);
  }

  private void saveStatus(DecisionDTO documentationUnitDTO, Instant createdAt, Status status) {
    repository.save(
        documentationUnitDTO.toBuilder()
            .status(
                StatusDTO.builder()
                    .publicationStatus(status.publicationStatus())
                    .withError(status.withError())
                    .createdAt(createdAt)
                    .documentationUnit(documentationUnitDTO)
                    .build())
            .build());
  }
}
