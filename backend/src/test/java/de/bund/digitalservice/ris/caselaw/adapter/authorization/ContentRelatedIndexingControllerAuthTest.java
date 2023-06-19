package de.bund.digitalservice.ris.caselaw.adapter.authorization;

import static de.bund.digitalservice.ris.caselaw.Utils.buildDocOffice;
import static de.bund.digitalservice.ris.caselaw.Utils.getMockLoginWithDocOffice;
import static de.bund.digitalservice.ris.caselaw.Utils.setUpDocumentationOfficeMocks;
import static de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatus.PUBLISHED;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import de.bund.digitalservice.ris.caselaw.adapter.AuthService;
import de.bund.digitalservice.ris.caselaw.adapter.ContentRelatedIndexingController;
import de.bund.digitalservice.ris.caselaw.adapter.FieldOfLawService;
import de.bund.digitalservice.ris.caselaw.adapter.KeycloakUserService;
import de.bund.digitalservice.ris.caselaw.adapter.KeywordService;
import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatus;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = ContentRelatedIndexingController.class)
@Import({SecurityConfig.class, AuthService.class})
class ContentRelatedIndexingControllerAuthTest {
  @Autowired private WebTestClient webClient;

  @MockBean private DocumentUnitService documentUnitService;
  @MockBean private FieldOfLawService fieldOfLawService;
  @MockBean private KeycloakUserService userService;
  @MockBean private KeywordService keywordService;

  private static final UUID TEST_UUID = UUID.fromString("88888888-4444-4444-4444-121212121212");
  private final String docOffice1Group = "/CC-RIS";
  private final String docOffice2Group = "/caselaw/BGH";
  private final DocumentationOffice docOffice1 = buildDocOffice("CC-RIS", "XX");
  private final DocumentationOffice docOffice2 = buildDocOffice("BGH", "CO");

  @BeforeEach
  void setUp() {
    setUpDocumentationOfficeMocks(
        userService, docOffice1, docOffice1Group, docOffice2, docOffice2Group);
  }

  @Test
  void testGetFieldsOfLaw() {
    when(fieldOfLawService.getFieldsOfLawForDocumentUnit(TEST_UUID)).thenReturn(Mono.empty());
    mockDocumentUnit(docOffice1, PUBLISHED);

    String uri =
        "/api/v1/caselaw/documentunits/" + TEST_UUID + "/contentrelatedindexing/fieldsoflaw";

    webClient
        .mutateWith(csrf())
        .mutateWith(getMockLoginWithDocOffice(docOffice1Group))
        .get()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isOk();

    webClient
        .mutateWith(csrf())
        .mutateWith(getMockLoginWithDocOffice(docOffice2Group))
        .get()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isOk(); // because status is PUBLISHED

    mockDocumentUnit(docOffice1, null);

    webClient
        .mutateWith(csrf())
        .mutateWith(getMockLoginWithDocOffice(docOffice2Group))
        .get()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testAddFieldOfLaw() {
    String identifier = "ABC";
    when(fieldOfLawService.addFieldOfLawToDocumentUnit(TEST_UUID, identifier))
        .thenReturn(Mono.empty());
    mockDocumentUnit(docOffice2, null);

    String uri =
        "/api/v1/caselaw/documentunits/"
            + TEST_UUID
            + "/contentrelatedindexing/fieldsoflaw/"
            + identifier;

    webClient
        .mutateWith(csrf())
        .mutateWith(getMockLoginWithDocOffice(docOffice1Group))
        .put()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isForbidden();

    webClient
        .mutateWith(csrf())
        .mutateWith(getMockLoginWithDocOffice(docOffice2Group))
        .put()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void testRemoveFieldOfLaw() {
    String identifier = "ABC";
    when(fieldOfLawService.removeFieldOfLawToDocumentUnit(TEST_UUID, identifier))
        .thenReturn(Mono.empty());
    mockDocumentUnit(docOffice1, null);

    String uri =
        "/api/v1/caselaw/documentunits/"
            + TEST_UUID
            + "/contentrelatedindexing/fieldsoflaw/"
            + identifier;

    webClient
        .mutateWith(csrf())
        .mutateWith(getMockLoginWithDocOffice(docOffice1Group))
        .delete()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isOk();

    webClient
        .mutateWith(csrf())
        .mutateWith(getMockLoginWithDocOffice(docOffice2Group))
        .delete()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testGetKeywords() {
    when(keywordService.getKeywordsForDocumentUnit(TEST_UUID)).thenReturn(Mono.empty());
    mockDocumentUnit(docOffice2, PUBLISHED);

    String uri = "/api/v1/caselaw/documentunits/" + TEST_UUID + "/contentrelatedindexing/keywords";

    webClient
        .mutateWith(csrf())
        .mutateWith(getMockLoginWithDocOffice(docOffice1Group))
        .get()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isOk(); // because status is PUBLISHED

    webClient
        .mutateWith(csrf())
        .mutateWith(getMockLoginWithDocOffice(docOffice2Group))
        .get()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isOk();

    mockDocumentUnit(docOffice2, null);

    webClient
        .mutateWith(csrf())
        .mutateWith(getMockLoginWithDocOffice(docOffice1Group))
        .get()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testAddKeyword() {
    String keyword = "XYZ";
    when(keywordService.addKeywordToDocumentUnit(TEST_UUID, keyword)).thenReturn(Mono.empty());
    mockDocumentUnit(docOffice1, null);

    String uri =
        "/api/v1/caselaw/documentunits/"
            + TEST_UUID
            + "/contentrelatedindexing/keywords/"
            + keyword;

    webClient
        .mutateWith(csrf())
        .mutateWith(getMockLoginWithDocOffice(docOffice1Group))
        .put()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isOk();

    webClient
        .mutateWith(csrf())
        .mutateWith(getMockLoginWithDocOffice(docOffice2Group))
        .put()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testDeleteKeyword() {
    String keyword = "XZY";
    when(keywordService.deleteKeywordFromDocumentUnit(TEST_UUID, keyword)).thenReturn(Mono.empty());
    mockDocumentUnit(docOffice2, null);

    String uri =
        "/api/v1/caselaw/documentunits/"
            + TEST_UUID
            + "/contentrelatedindexing/keywords/"
            + keyword;

    webClient
        .mutateWith(csrf())
        .mutateWith(getMockLoginWithDocOffice(docOffice1Group))
        .delete()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isForbidden();

    webClient
        .mutateWith(csrf())
        .mutateWith(getMockLoginWithDocOffice(docOffice2Group))
        .delete()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isOk();
  }

  private void mockDocumentUnit(DocumentationOffice docOffice, DocumentUnitStatus status) {
    when(documentUnitService.getByUuid(TEST_UUID))
        .thenReturn(
            Mono.just(
                DocumentUnit.builder()
                    .status(status)
                    .coreData(CoreData.builder().documentationOffice(docOffice).build())
                    .build()));
  }
}
