package de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words;

import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class IgnoredTextCheckWord {

  private UUID id;

  @Size(max = 255)
  private String word;

  @Size(max = 255)
  private DocumentationOffice documentationOffice;
}
