package de.bund.digitalservice.ris.caselaw.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

public enum CorrectionType {
  BERICHTIGUNGSBESCHLUSS("Berichtigungsbeschluss"),
  ERGAENZUNGSBESCHUSS("Ergänzungsbeschuss"),
  ERGAENZUNGSURTEIL("Ergänzungsurteil"),
  UNRICHTIGKEITEN("Unrichtigkeiten"),
  SCHREIBFEHLERBERICHTIGUNG("Schreibfehlerberichtigung");

  @JsonValue @Getter private final String label;

  private static final Map<String, CorrectionType> map = new HashMap<>(values().length, 1);

  static {
    for (CorrectionType c : values()) map.put(c.label, c);
  }

  CorrectionType(String label) {
    this.label = label;
  }

  public static CorrectionType of(String name) {
    CorrectionType result = map.get(name);

    if (result == null) {
      throw new IllegalArgumentException("Invalid correction type name: " + name);
    }

    return result;
  }
}
