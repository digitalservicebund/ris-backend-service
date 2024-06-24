package de.bund.digitalservice.ris.caselaw;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.getMockLogin;
import static de.bund.digitalservice.ris.caselaw.AuthUtils.getMockLoginWithDocOffice;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

import org.springframework.test.web.reactive.server.WebTestClient;

public class RisWebTestClient {

  private final WebTestClient webTestClient;

  public RisWebTestClient(WebTestClient webTestClient) {
    this.webTestClient = webTestClient;
  }

  public WebTestClient withDefaultLogin() {
    return this.webTestClient.mutateWith(csrf()).mutateWith(getMockLogin());
  }

  public WebTestClient withLogin(String docOfficeGroup) {
    return this.webTestClient
        .mutateWith(csrf())
        .mutateWith(getMockLoginWithDocOffice(docOfficeGroup));
  }
}
