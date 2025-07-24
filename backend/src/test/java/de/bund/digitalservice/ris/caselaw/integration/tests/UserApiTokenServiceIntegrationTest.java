package de.bund.digitalservice.ris.caselaw.integration.tests;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.bund.digitalservice.ris.caselaw.adapter.BareIdUserApiTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "spring.flyway.locations=classpath:db/migration",
      "otc.obs.bucket-name=testBucket",
      "otc.obs.endpoint=testUrl",
      "local.file-storage=.local-storage",
      "mail.from.address=test@test.com",
      "mail.exporter.jurisUsername=test-user",
      "mail.exporter.recipientAddress=neuris@example.com",
      "management.endpoint.health.probes.enabled=true",
      "management.health.livenessState.enabled=true",
      "management.health.readinessState.enabled=true",
      "management.endpoint.health.group.readiness.include=readinessState,db,redis",
      "spring.security.oauth2.client.provider.keycloak.issuer-uri=${OAUTH2_CLIENT_ISSUER}"
    })
class UserApiTokenServiceIntegrationTest {

  @Autowired private BareIdUserApiTokenService service;

  @Test
  void testGetAccessToken() {
    var result = service.getAccessToken();
    assertNotNull(result.getTokenValue());
  }
}
