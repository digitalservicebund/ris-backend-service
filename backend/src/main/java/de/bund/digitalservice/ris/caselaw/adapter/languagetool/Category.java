package de.bund.digitalservice.ris.caselaw.adapter.languagetool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
  private String id;
  private String name;
}
