package de.bund.digitalservice.ris.caselaw;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockOidcLogin;

import de.bund.digitalservice.ris.caselaw.adapter.KeycloakUserService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.OidcLoginMutator;
import reactor.core.publisher.Mono;

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

  public static void setUpDocumentationOfficeMocks(
      KeycloakUserService userService,
      DocumentationOffice docOffice1,
      String docOffice1Group,
      DocumentationOffice docOffice2,
      String docOffice2Group) {
    doReturn(Mono.just(docOffice1))
        .when(userService)
        .getDocumentationOffice(
            argThat(
                (OidcUser user) -> {
                  List<String> groups = user.getAttribute("groups");
                  return Objects.requireNonNull(groups).get(0).equals(docOffice1Group);
                }));
    doReturn(Mono.just(docOffice2))
        .when(userService)
        .getDocumentationOffice(
            argThat(
                (OidcUser user) -> {
                  List<String> groups = user.getAttribute("groups");
                  return Objects.requireNonNull(groups).get(0).equals(docOffice2Group);
                }));
  }

  public static DocumentationOffice buildDocOffice(String label, String abbr) {
    return DocumentationOffice.builder().label(label).abbreviation(abbr).build();
  }
}
