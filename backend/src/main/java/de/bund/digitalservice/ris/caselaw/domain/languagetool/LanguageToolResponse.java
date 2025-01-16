package de.bund.digitalservice.ris.caselaw.domain.languagetool;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LanguageToolResponse {

  private List<Match> matches;
}
