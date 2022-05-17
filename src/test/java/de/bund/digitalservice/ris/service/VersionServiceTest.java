package de.bund.digitalservice.ris.service;

import static org.junit.jupiter.api.Assertions.*;

import de.bund.digitalservice.ris.datamodel.VersionInfo;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

@SpringBootTest(properties = "api.version.version=dummyVersionValue")
class VersionServiceTest {

  @Value("${api.version.version}")
  private String VERSION;

  @Autowired private VersionService service;

  @Test
  public void testGenerateVersionInfo() {
    StepVerifier.create(service.generateVersionInfo())
        .consumeNextWith(
            responseEntity -> {
              assertNotNull(responseEntity);
              assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
              VersionInfo versionInfo = (VersionInfo) responseEntity.getBody();
              assertEquals(Objects.requireNonNull(versionInfo).getVersion(), VERSION);
              assertNotNull(versionInfo.getCommitSHA());
            })
        .verifyComplete();
  }
}
