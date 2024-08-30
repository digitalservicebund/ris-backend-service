package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * The user groups represent the groups in Bare.ID / Keycloak, however, there is no guarantee that
 * the two sets of groups are in sync.
 */
public interface DocumentationOfficeUserGroupService {
  /** Get all user groups from memory. (Will only be updated after server restart.) */
  List<DocumentationOfficeUserGroup> getAllUserGroups();

  List<DocumentationOfficeUserGroup> getExternalUserGroups(OidcUser oidcUser);
}
