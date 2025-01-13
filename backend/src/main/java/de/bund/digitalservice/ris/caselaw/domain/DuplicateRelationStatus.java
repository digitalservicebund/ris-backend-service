package de.bund.digitalservice.ris.caselaw.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.List;

public enum DuplicateRelationStatus {
  PENDING,
  IGNORED;

  @JsonCreator
  public static DuplicateRelationStatus fromValue(String value) {
    for (DuplicateRelationStatus status : DuplicateRelationStatus.values()) {
      if (status.name().equalsIgnoreCase(value)) {
        return status;
      }
    }
    throw new IllegalArgumentException(
        "Invalid value '"
            + value
            + "' for DuplicateRelationStatus. Allowed values are: "
            + List.of(DuplicateRelationStatus.values()));
  }

  @JsonValue
  public String toValue() {
    return name();
  }
}
