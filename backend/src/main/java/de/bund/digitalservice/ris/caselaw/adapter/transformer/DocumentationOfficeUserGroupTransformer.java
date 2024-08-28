package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeUserGroupDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOfficeUserGroup;

public class DocumentationOfficeUserGroupTransformer {
  private DocumentationOfficeUserGroupTransformer() {}

  public static DocumentationOfficeUserGroup transformToDomain(
      DocumentationOfficeUserGroupDTO group) {
    if (group == null) {
      return null;
    }

    return DocumentationOfficeUserGroup.builder()
        .id(group.getId())
        .userGroupPathName(group.getUserGroupPathName())
        .isInternal(group.isInternal())
        .docOffice(
            DocumentationOffice.builder()
                .uuid(group.getDocumentationOffice().getId())
                .abbreviation(group.getDocumentationOffice().getAbbreviation())
                .build())
        .build();
  }
}
