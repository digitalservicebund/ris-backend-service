package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@ToString(callSuper = true)
@Getter
@NoArgsConstructor
@Data
public class ForeignLanguageVersion {
  private UUID id;
  private LanguageCode languageCode;
  private String link;
}
