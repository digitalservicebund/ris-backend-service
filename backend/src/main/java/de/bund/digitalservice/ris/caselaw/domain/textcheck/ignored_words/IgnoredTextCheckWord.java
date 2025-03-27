package de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words;

import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class IgnoredTextCheckWord {

  private UUID uuid;

  @Size(max = 255)
  private String word;
}
