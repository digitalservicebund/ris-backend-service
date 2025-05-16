package de.bund.digitalservice.ris.caselaw.adapter.eurlex;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDSDocOffice;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import de.bund.digitalservice.ris.caselaw.TestConfig;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.SearchService;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import de.bund.digitalservice.ris.caselaw.webtestclient.RisWebTestClient;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = EurLexController.class)
@Import({TestConfig.class})
class EurLexControllerTest {
  @Autowired private RisWebTestClient risWebClient;

  @MockitoBean private SearchService service;

  @MockitoBean private UserService userService;

  private final DocumentationOffice docOffice = buildDSDocOffice();

  @BeforeEach
  void setUp() {
    doReturn(docOffice)
        .when(userService)
        .getDocumentationOffice(
            argThat(
                (OidcUser currentUser) -> {
                  List<String> groups = currentUser.getAttribute("groups");
                  return Objects.requireNonNull(groups).get(0).equals("/DS");
                }));
  }

  @Test
  void testGetSearchResults_withoutParameters() {
    risWebClient
        .withDefaultLogin()
        .get()
        .uri("/api/v1/caselaw/eurlex")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Page.class);

    verify(service, times(1))
        .getSearchResults(
            null,
            docOffice,
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty());
  }

  @Test
  void testGetSearchResults_withAllParameters() {
    risWebClient
        .withDefaultLogin()
        .get()
        .uri(
            "/api/v1/caselaw/eurlex?page=2&file-number=file-number&celex=celex&court=court&start-date=2025-01-01&end-date=2025-03-01")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(Page.class);

    verify(service, times(1))
        .getSearchResults(
            "2",
            docOffice,
            Optional.of("file-number"),
            Optional.of("celex"),
            Optional.of("court"),
            Optional.of(LocalDate.of(2025, Month.JANUARY, 1)),
            Optional.of(LocalDate.of(2025, Month.MARCH, 1)));
  }
}
