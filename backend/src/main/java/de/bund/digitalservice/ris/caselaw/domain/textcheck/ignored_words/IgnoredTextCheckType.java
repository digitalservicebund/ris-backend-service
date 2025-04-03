package de.bund.digitalservice.ris.caselaw.domain.textcheck.ignored_words;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum IgnoredTextCheckType {
  GLOBAL("global"),
  DOCUMENTATION_UNIT("documentation_unit");
  @JsonValue private final String name;

  IgnoredTextCheckType(String name) {
    this.name = name;
  }
}
