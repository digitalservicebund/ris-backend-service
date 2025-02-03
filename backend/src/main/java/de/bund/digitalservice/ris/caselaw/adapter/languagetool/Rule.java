package de.bund.digitalservice.ris.caselaw.adapter.languagetool;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Rule {
  private String id;
  private String description;
  private String issueType;
  private Category category;
}
