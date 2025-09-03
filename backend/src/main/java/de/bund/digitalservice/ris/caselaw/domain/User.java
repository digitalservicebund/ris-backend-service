package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder(toBuilder = true)
public record User(
    UUID id,
    UUID externalId,
    String name,
    String firstName, // TODO delete?
    String lastName, // TODO delete?
    String email,
    DocumentationOffice documentationOffice,
    List<String> roles,
    String initials) {

  @Override
  public String toString() {
    return "User[id=%s]".formatted(id);
  }
}
