package de.bund.digitalservice.ris.caselaw.adapter;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BareIdUserApiTokenServiceTest {

  @Autowired private BareIdUserApiTokenService service;

  @Test
  void getAccessToken() {

    var result = service.getAccessToken();
    assertNotNull(result.getTokenValue());
  }
}
