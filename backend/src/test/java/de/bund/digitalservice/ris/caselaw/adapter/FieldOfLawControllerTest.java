package de.bund.digitalservice.ris.caselaw.adapter;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = FieldOfLawController.class)
@WithMockUser
class FieldOfLawControllerTest {
  @Autowired private WebTestClient webClient;

  @MockBean private FieldOfLawService service;

  @Test
  void testGetFieldsOfLaw_withoutQuery_shouldCallServiceWithoutValue() {
    when(service.getFieldsOfLawBySearchQuery(Optional.empty())).thenReturn(Flux.empty());

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/caselaw/fieldsoflaw")
        .exchange()
        .expectStatus()
        .isOk();

    verify(service, times(1)).getFieldsOfLawBySearchQuery(Optional.empty());
  }

  @Test
  void testGetFieldsOfLaw_withQuery_shouldCallServiceWithValue() {
    when(service.getFieldsOfLawBySearchQuery(Optional.of("root"))).thenReturn(Flux.empty());

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/caselaw/fieldsoflaw?q=root")
        .exchange()
        .expectStatus()
        .isOk();

    verify(service, times(1)).getFieldsOfLawBySearchQuery(Optional.of("root"));
  }

  @Test
  void testGetChildrenOfFieldOfLaw() {
    when(service.getChildrenOfFieldOfLaw("root")).thenReturn(Flux.empty());

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/caselaw/fieldsoflaw/root/children")
        .exchange()
        .expectStatus()
        .isOk();

    verify(service, times(1)).getChildrenOfFieldOfLaw("root");
  }

  @Test
  void testGetTreeForFieldOfLaw() {
    when(service.getTreeForFieldOfLaw("root")).thenReturn(Mono.empty());

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/caselaw/fieldsoflaw/root/tree")
        .exchange()
        .expectStatus()
        .isOk();

    verify(service, times(1)).getTreeForFieldOfLaw("root");
  }
}
