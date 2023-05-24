package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class KeycloakUserService implements UserService {
  private final DatabaseDocumentationOfficeRepository documentationOfficeRepository;

  private static final Map<String, String> documentationCenterClaims =
      Map.of(
          "/caselaw/BGH", "BGH",
          "/caselaw/BVerfG", "BVerfG",
          "/DigitalService", "DigitalService",
          "/tester/CC-RIS", "CC-RIS");

  public KeycloakUserService(DatabaseDocumentationOfficeRepository documentationOfficeRepository) {
    this.documentationOfficeRepository = documentationOfficeRepository;
  }

  public Mono<User> getUser(OidcUser oidcUser) {
    return extractDocumentationOffice(oidcUser)
        .map(documentationOffice -> createUser(oidcUser, documentationOffice))
        .switchIfEmpty(Mono.defer(() -> Mono.just(createUser(oidcUser, null))));
  }

  public Mono<DocumentationOffice> getDocumentationOffice(OidcUser oidcUser) {
    return getUser(oidcUser).map(User::documentationOffice);
  }

  private User createUser(OidcUser oidcUser, DocumentationOffice documentationOffice) {
    return User.builder()
        .name(oidcUser.getAttribute("name"))
        .documentationOffice(documentationOffice)
        .build();
  }

  private Mono<DocumentationOffice> extractDocumentationOffice(OidcUser oidcUser) {
    List<String> groups = Objects.requireNonNull(oidcUser.getAttribute("groups"));
    String documentationOfficeKey =
        groups.stream()
            .filter(documentationCenterClaims::containsKey)
            .findFirst()
            .map(documentationCenterClaims::get)
            .orElse(null);

    return documentationOfficeRepository
        .findByLabel(documentationOfficeKey)
        .map(
            documentationOfficeDTO ->
                DocumentationOffice.builder()
                    .label(documentationOfficeDTO.getLabel())
                    .abbreviation(documentationOfficeDTO.getAbbreviation())
                    .build());
  }
}
