package de.bund.digitalservice.ris.caselaw.domain;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private static final Map<String, String> documentationCenters =
      Map.of(
          "BGH_user", "BGH",
          "BVerfG_user", "BVerfG",
          "DS_user", "DigitalService");

  public User getUser(OidcUser oidcUser) {
    return User.builder()
        .name(oidcUser.getAttribute("name"))
        .documentationCenterAbbreviation(extractDocumentationCenter(oidcUser))
        .build();
  }

  private String extractDocumentationCenter(OidcUser oidcUser) {
    ArrayList<String> groups = Objects.requireNonNull(oidcUser.getAttribute("groups"));
    return groups.stream()
        .filter(documentationCenters::containsKey)
        .findFirst()
        .map(documentationCenters::get)
        .orElse(null);
  }
}
