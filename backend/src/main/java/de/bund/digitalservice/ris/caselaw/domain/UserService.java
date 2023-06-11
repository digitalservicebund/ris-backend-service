package de.bund.digitalservice.ris.caselaw.domain;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import reactor.core.publisher.Mono;

public interface UserService {
  Mono<User> getUser(OidcUser oidcUser);

  Mono<DocumentationOffice> getDocumentationOffice(OidcUser oidcUser);
}
