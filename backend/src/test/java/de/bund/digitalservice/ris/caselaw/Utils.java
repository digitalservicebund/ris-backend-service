package de.bund.digitalservice.ris.caselaw;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockOidcLogin;

import java.util.Collections;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.OidcLoginMutator;

public class Utils {

  public static OidcLoginMutator getMockLogin() {
    return getMockLoginWithDocOffice("/DigitalService");
  }

  public static OidcLoginMutator getMockLoginWithDocOffice(String docOfficeGroup) {
    return mockOidcLogin()
        .idToken(
            token ->
                token.claims(
                    claims -> {
                      claims.put("groups", Collections.singletonList(docOfficeGroup));
                      claims.put("name", "testUser");
                      claims.put("email", "test@test.com");
                    }));
  }
}
