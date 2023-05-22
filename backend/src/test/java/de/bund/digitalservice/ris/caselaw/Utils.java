package de.bund.digitalservice.ris.caselaw;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockOidcLogin;

import java.util.Collections;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.OidcLoginMutator;

public class Utils {

  public static OidcLoginMutator getMockLogin() {
    return mockOidcLogin()
        .idToken(
            token ->
                token.claims(
                    claims -> {
                      claims.put("groups", Collections.singletonList("/DigitalService"));
                      claims.put("name", "testUser");
                    }));
  }
}
