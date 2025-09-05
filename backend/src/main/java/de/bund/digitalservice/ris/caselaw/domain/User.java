package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;
import lombok.Builder;

@Builder(toBuilder = true)
public record User(
    UUID id,
    UUID externalId,
    String name,
    String firstName,
    String lastName,
    String email,
    DocumentationOffice documentationOffice,
    boolean internal,
    String initials) {

  @Override
  public String toString() {
    return "User[id=%s]".formatted(id);
  }
}
