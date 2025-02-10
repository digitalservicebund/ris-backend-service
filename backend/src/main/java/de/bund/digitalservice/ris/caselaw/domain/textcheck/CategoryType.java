package de.bund.digitalservice.ris.caselaw.domain.textcheck;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum CategoryType {
  REASONS("Gründe"),
  CASE_FACTS("Tatbestand"),
  DECISION_REASON("Entscheidungsgründe");

  @JsonValue private final String name;

  CategoryType(String name) {
    this.name = name;
  }
}
