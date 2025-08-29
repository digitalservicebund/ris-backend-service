package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.UserTransformer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public abstract class UserService {
  public abstract User getUser(OidcUser oidcUser);

  public abstract User getUser(UUID uuid);

  public abstract List<User> getUsers(OidcUser oidcUser);

  public abstract Optional<UserGroup> getUserGroup(OidcUser oidcUser);

  public abstract void persistUsers(List<User> users);

  public DocumentationOffice getDocumentationOffice(OidcUser oidcUser) {
    return getUser(oidcUser).documentationOffice();
  }

  public String getEmail(OidcUser oidcUser) {
    return oidcUser.getEmail();
  }

  public Boolean isInternal(OidcUser oidcUser) {
    List<String> roles = oidcUser.getClaimAsStringList("roles");
    if (roles != null) {
      return roles.contains("Internal");
    }
    return false;
  }

  protected User createUser(OidcUser oidcUser, DocumentationOffice documentationOffice) {
    return UserTransformer.transformToDomain(oidcUser, documentationOffice);
  }
}
