package de.bund.digitalservice.ris.controller;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import de.bund.digitalservice.ris.service.VersionService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = VersionController.class)
@WithMockUser
@Tag("test")
class VersionControllerTest {

  @Autowired private WebTestClient webClient;

  @MockBean private VersionService service;

  @Test
  void testGetVersion() {
    webClient.mutateWith(csrf()).get().uri("/api/v1/version").exchange().expectStatus().isOk();
  }
}
