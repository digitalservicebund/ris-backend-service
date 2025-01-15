package de.bund.digitalservice.ris.caselaw.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

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
    return null;
  }
}
