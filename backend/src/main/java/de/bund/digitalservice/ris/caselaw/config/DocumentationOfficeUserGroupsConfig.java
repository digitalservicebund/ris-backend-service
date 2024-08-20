package de.bund.digitalservice.ris.caselaw.config;

import de.bund.digitalservice.ris.caselaw.adapter.DocumentationOfficeConfigUserGroup;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentationOfficeUserGroupsConfig {

  /**
   * Caution: Changing values will delete and recreate the affected group in our database. This
   * means all references to the affected group will be deleted, e.g., the links of the affected
   * user group to procedures will be cleared.
   */
  @Bean
  public List<DocumentationOfficeConfigUserGroup> getDocumentationOfficeConfigUserGroups() {
    return List.of(
        DocumentationOfficeConfigUserGroup.builder()
            .docOfficeAbbreviation("BGH")
            .userGroupPathName("/caselaw/BGH")
            .isInternal(true)
            .build(),
        DocumentationOfficeConfigUserGroup.builder()
            .docOfficeAbbreviation("BGH")
            .userGroupPathName("/caselaw/BGH/Intern")
            .isInternal(true)
            .build(),
        DocumentationOfficeConfigUserGroup.builder()
            .docOfficeAbbreviation("BGH")
            .userGroupPathName("/caselaw/BGH/Extern/Miotke")
            .isInternal(false)
            .build(),
        DocumentationOfficeConfigUserGroup.builder()
            .docOfficeAbbreviation("BGH")
            .userGroupPathName("/caselaw/BGH/Extern/Busenks")
            .isInternal(false)
            .build(),
        DocumentationOfficeConfigUserGroup.builder()
            .docOfficeAbbreviation("BVerfG")
            .userGroupPathName("/caselaw/BVerfG")
            .isInternal(true)
            .build(),
        DocumentationOfficeConfigUserGroup.builder()
            .docOfficeAbbreviation("BAG")
            .userGroupPathName("/caselaw/BAG")
            .isInternal(true)
            .build(),
        DocumentationOfficeConfigUserGroup.builder()
            .docOfficeAbbreviation("BFH")
            .userGroupPathName("/caselaw/BFH")
            .isInternal(true)
            .build(),
        DocumentationOfficeConfigUserGroup.builder()
            .docOfficeAbbreviation("BSG")
            .userGroupPathName("/caselaw/BSG")
            .isInternal(true)
            .build(),
        DocumentationOfficeConfigUserGroup.builder()
            .docOfficeAbbreviation("BPatG")
            .userGroupPathName("/caselaw/BPatG")
            .isInternal(true)
            .build(),
        DocumentationOfficeConfigUserGroup.builder()
            .docOfficeAbbreviation("BVerwG")
            .userGroupPathName("/caselaw/BVerwG")
            .isInternal(true)
            .build(),
        DocumentationOfficeConfigUserGroup.builder()
            .docOfficeAbbreviation("OVGNW")
            .userGroupPathName("/caselaw/OVG_NRW")
            .isInternal(true)
            .build(),
        DocumentationOfficeConfigUserGroup.builder()
            .docOfficeAbbreviation("BZSt")
            .userGroupPathName("/caselaw/BZSt")
            .isInternal(true)
            .build(),
        DocumentationOfficeConfigUserGroup.builder()
            .docOfficeAbbreviation("DS")
            .userGroupPathName("/DS")
            .isInternal(true)
            .build(),
        DocumentationOfficeConfigUserGroup.builder()
            .docOfficeAbbreviation("DS")
            .userGroupPathName("/DS/Intern")
            .isInternal(true)
            .build(),
        DocumentationOfficeConfigUserGroup.builder()
            .docOfficeAbbreviation("DS")
            .userGroupPathName("/DS/Extern")
            .isInternal(false)
            .build(),
        DocumentationOfficeConfigUserGroup.builder()
            .docOfficeAbbreviation("CC-RIS")
            .userGroupPathName("/CC-RIS")
            .isInternal(true)
            .build());
  }
}
