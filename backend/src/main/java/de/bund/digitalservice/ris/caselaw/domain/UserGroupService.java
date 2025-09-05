package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.Optional;

/**
 * The user groups represent the groups in Bare.ID / Keycloak, however, there is no guarantee that
 * the two sets of groups are in sync.
 */
public interface UserGroupService {
  /** Get all user groups from memory. (Will only be updated after server restart.) */
  List<UserGroup> getAllUserGroups();

  Optional<UserGroup> getDocumentationOfficeFromGroupPathNames(List<String> userGroupPathNames);

  List<UserGroup> getExternalUserGroups(DocumentationOffice documentationOffice);

  List<UserGroup> getAllGroupsForDocumentationOffice(DocumentationOffice office);
}
