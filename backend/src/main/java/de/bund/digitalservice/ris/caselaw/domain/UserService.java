package de.bund.digitalservice.ris.caselaw.domain;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private static final Map<String, DocumentationCenter> documentationCenterClaims =
      Map.of(
          "/caselaw/BGH", DocumentationCenter.BGH,
          "/caselaw/BVerfG", DocumentationCenter.BVerfG,
          "/DigitalService", DocumentationCenter.DigitalService,
          "/CC-RIS", DocumentationCenter.CCRIS);

  public User getUser(OidcUser oidcUser) {
    return User.builder()
        .name(oidcUser.getAttribute("name"))
        .documentationCenterAbbreviation(extractDocumentationCenter(oidcUser).toString())
        .build();
  }

  private DocumentationCenter extractDocumentationCenter(OidcUser oidcUser) {
    ArrayList<String> groups = Objects.requireNonNull(oidcUser.getAttribute("groups"));
    return groups.stream()
        .filter(documentationCenterClaims::containsKey)
        .findFirst()
        .map(documentationCenterClaims::get)
        .orElse(null);
  }
}
