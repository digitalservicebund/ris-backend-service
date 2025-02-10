package de.bund.digitalservice.ris.caselaw.adapter.languagetool;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Context {
  private String text;
  private int offset;
  private int length;
}
