package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import org.jetbrains.annotations.NotNull;

@Builder
public record User(
    UUID id,
    String name,
    String email,
    DocumentationOffice documentationOffice,
    List<String> roles,
    String initials) {

  @NotNull
  @Override
  public String toString() {
    return "User[id=%s]".formatted(id);
  }
}
