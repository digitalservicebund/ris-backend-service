package de.bund.digitalservice.ris.caselaw.adapter.languagetool;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Match {
  private String message;
  private String shortMessage;
  @Builder.Default private List<Replacement> replacements = new ArrayList<>();
  private int offset;
  private int length;
  private Context context;
  private String sentence;
  @Builder.Default private Type type = new Type();
  private Rule rule;
  private boolean ignoreForIncompleteSentence;
  private int contextForSureMatch;
  private String textContent;

  public int end() {
    return offset + length;
  }
}
