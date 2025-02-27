package de.bund.digitalservice.ris.caselaw.domain.textcheck;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum CategoryType {
  REASONS("reasons"),
  CASE_FACTS("caseFacts"),
  DECISION_REASON("decisionReason"),
  HEADNOTE("headnote"),
  GUIDING_PRINCIPLE("guidingPrinciple"),
  TENOR("tenor"),
  UNKNOWN("unbekannt");

  @JsonValue private final String name;

  CategoryType(String name) {
    this.name = name;
  }

  public static CategoryType forName(String category) {
    for (CategoryType type : CategoryType.values()) {
      if (type.name.equals(category)) {
        return type;
      }
    }

    return UNKNOWN;
  }
}
