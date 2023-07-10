package de.bund.digitalservice.ris.caselaw.adapter.authorization;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDocOffice;
import static de.bund.digitalservice.ris.caselaw.AuthUtils.setUpDocumentationOfficeMocks;
import static de.bund.digitalservice.ris.caselaw.domain.PublicationStatus.PUBLISHED;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.RisWebTestClient;
import de.bund.digitalservice.ris.caselaw.TestConfig;
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
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = ContentRelatedIndexingController.class)
@Import({SecurityConfig.class, AuthService.class, TestConfig.class})
class ContentRelatedIndexingControllerAuthTest {
  @Autowired private RisWebTestClient risWebTestClient;

  @MockBean private DocumentUnitService documentUnitService;
  @MockBean private FieldOfLawService fieldOfLawService;
  @MockBean private KeycloakUserService userService;
  @MockBean private KeywordService keywordService;
  @MockBean ReactiveClientRegistrationRepository clientRegistrationRepository;

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

    risWebTestClient.withLogin(docOffice1Group).get().uri(uri).exchange().expectStatus().isOk();

    risWebTestClient
        .withLogin(docOffice2Group)
        .get()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isOk(); // because status is PUBLISHED

    mockDocumentUnit(docOffice1, null);

    risWebTestClient
        .withLogin(docOffice2Group)
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

    risWebTestClient
        .withLogin(docOffice1Group)
        .put()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isForbidden();

    risWebTestClient.withLogin(docOffice2Group).put().uri(uri).exchange().expectStatus().isOk();
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

    risWebTestClient.withLogin(docOffice1Group).delete().uri(uri).exchange().expectStatus().isOk();

    risWebTestClient
        .withLogin(docOffice2Group)
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

    risWebTestClient
        .withLogin(docOffice1Group)
        .get()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isOk(); // because status is PUBLISHED

    risWebTestClient.withLogin(docOffice2Group).get().uri(uri).exchange().expectStatus().isOk();

    mockDocumentUnit(docOffice2, null);

    risWebTestClient
        .withLogin(docOffice1Group)
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

    risWebTestClient.withLogin(docOffice1Group).put().uri(uri).exchange().expectStatus().isOk();

    risWebTestClient
        .withLogin(docOffice2Group)
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

    risWebTestClient
        .withLogin(docOffice1Group)
        .delete()
        .uri(uri)
        .exchange()
        .expectStatus()
        .isForbidden();

    risWebTestClient.withLogin(docOffice2Group).delete().uri(uri).exchange().expectStatus().isOk();
  }

  private void mockDocumentUnit(DocumentationOffice docOffice, PublicationStatus status) {
    when(documentUnitService.getByUuid(TEST_UUID))
        .thenReturn(
            Mono.just(
                DocumentUnit.builder()
                    .status(DocumentUnitStatus.builder().status(status).build())
                    .coreData(CoreData.builder().documentationOffice(docOffice).build())
                    .build()));
  }
}
