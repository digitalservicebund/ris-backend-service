package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;
import javax.annotation.Nullable;
import lombok.Builder;

@Builder
public record DocumentationOffice(String abbreviation, UUID id) {

  /**
   * Helper method to check if two DocumentationOffice objects are not null and have the same ID.
   */
  public static boolean areSameOffice(
      @javax.annotation.Nullable DocumentationOffice officeA,
      @Nullable DocumentationOffice officeB) {

    if (officeA == null || officeB == null) {
      return false;
    }
    return officeA.id().equals(officeB.id());
  }
}
