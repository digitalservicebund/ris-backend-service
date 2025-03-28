package de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
  @NotBlank
  private String word;

  @NotNull private IgnoredTextCheckType type;

  Boolean isEditable;
}
