package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.Optional;

/**
 * The user groups represent the groups in Bare.ID / Keycloak, however, there is no guarantee that
 * the two sets of groups are in sync.
 */
public interface DocumentationOfficeUserGroupService {
  /** Get all user groups that exist for given doc office. */
  List<DocumentationOfficeUserGroup> getUserGroupsForDocOffice(
      DocumentationOffice documentationOffice);

  /**
   * Given a list of groups, returns the first group that is associated with a doc office. Caution:
   * A user can be associated with multiple groups in Keycloak.
   */
  Optional<DocumentationOfficeUserGroup> getFirstUserGroupWithDocOffice(List<String> userGroups);
}
