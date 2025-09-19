package de.bund.digitalservice.ris.caselaw.adapter.languagetool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rule {
  private String id;
  private String description;
  private String issueType;
  private Category category;
}
