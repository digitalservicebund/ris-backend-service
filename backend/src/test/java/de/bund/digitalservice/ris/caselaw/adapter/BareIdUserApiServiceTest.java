package de.bund.digitalservice.ris.caselaw.adapter;

import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class BareIdUserApiServiceTest {

  @Autowired BareIdUserApiService bareIdUserApiService;

  @Test
  void getBareIdToken() {
    // TODO: Adjust user id for testing
    Assertions.assertNotNull(bareIdUserApiService.getUser(UUID.randomUUID()));
  }
}
