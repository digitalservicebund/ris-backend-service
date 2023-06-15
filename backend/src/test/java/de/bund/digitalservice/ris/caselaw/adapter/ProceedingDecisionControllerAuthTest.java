package de.bund.digitalservice.ris.caselaw.adapter;

import static de.bund.digitalservice.ris.caselaw.Utils.getMockLoginWithDocOffice;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import de.bund.digitalservice.ris.caselaw.config.SecurityConfig;
import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.ProceedingDecision;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = ProceedingDecisionController.class)
@Import({SecurityConfig.class, AuthService.class})
class ProceedingDecisionControllerAuthTest {
  @Autowired private WebTestClient webClient;

  @MockBean private DocumentUnitService service;
  @MockBean private KeycloakUserService userService;

  private static final UUID TEST_UUID = UUID.fromString("88888888-4444-4444-4444-121212121212");
  private static final UUID CHILD_TEST_UUID =
      UUID.fromString("99999999-5555-5555-5555-232323232323");
  private final String docOffice1Group = "/CC-RIS";
  private final DocumentationOffice docOffice1 =
      DocumentationOffice.builder().label("CC-RIS").abbreviation("XX").build();
  private final String docOffice2Group = "/caselaw/BGH";
  private final DocumentationOffice docOffice2 =
      DocumentationOffice.builder().label("BGH").abbreviation("CO").build();

  @BeforeEach
  void setUp() {
    doReturn(Mono.just(docOffice1))
        .when(userService)
        .getDocumentationOffice(
            argThat(
                (OidcUser user) -> {
                  List<String> groups = user.getAttribute("groups");
                  return Objects.requireNonNull(groups).get(0).equals(docOffice1Group);
                }));
    doReturn(Mono.just(docOffice2))
        .when(userService)
        .getDocumentationOffice(
            argThat(
                (OidcUser user) -> {
                  List<String> groups = user.getAttribute("groups");
                  return Objects.requireNonNull(groups).get(0).equals(docOffice2Group);
                }));
  }

  @Test
  void testCreateProceedingDecision() {
    mockDocumentUnit(docOffice1);
    when(service.createProceedingDecision(
            any(UUID.class), any(ProceedingDecision.class), any(DocumentationOffice.class)))
        .thenReturn(Flux.empty());

    String uri = "/api/v1/caselaw/documentunits/" + TEST_UUID + "/proceedingdecisions";

    webClient
        .mutateWith(csrf())
        .mutateWith(getMockLoginWithDocOffice(docOffice1Group))
        .put()
        .uri(uri)
        .bodyValue(ProceedingDecision.builder().build())
        .exchange()
        .expectStatus()
        .isOk();

    webClient
        .mutateWith(csrf())
        .mutateWith(getMockLoginWithDocOffice(docOffice2Group))
        .put()
        .uri(uri)
        .bodyValue(ProceedingDecision.builder().build())
        .exchange()
        .expectStatus()
        .isForbidden();
  }

  @Test
  void testLinkProceedingDecision() {
    mockDocumentUnit(docOffice2);
    when(service.linkProceedingDecision(any(UUID.class), any(UUID.class))).thenReturn(Mono.empty());

    String uri =
        "/api/v1/caselaw/documentunits/" + TEST_UUID + "/proceedingdecisions/" + CHILD_TEST_UUID;

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
  void testRemoveProceedingDecision() {
    mockDocumentUnit(docOffice1);
    when(service.removeProceedingDecision(any(UUID.class), any(UUID.class)))
        .thenReturn(Mono.empty());

    String uri =
        "/api/v1/caselaw/documentunits/" + TEST_UUID + "/proceedingdecisions/" + CHILD_TEST_UUID;

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

  private void mockDocumentUnit(DocumentationOffice docOffice) {
    when(service.getByUuid(TEST_UUID))
        .thenReturn(
            Mono.just(
                DocumentUnit.builder()
                    .uuid(TEST_UUID)
                    .coreData(CoreData.builder().documentationOffice(docOffice).build())
                    .build()));
  }
}
