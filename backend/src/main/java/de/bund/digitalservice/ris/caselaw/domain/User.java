package de.bund.digitalservice.ris.caselaw.domain;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;

@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public record User(
    UUID id,
    UUID externalId,
    String firstName,
    String lastName,
    String email,
    DocumentationOffice documentationOffice,
    boolean internal) {

  @Override
  public String toString() {
    return "User[id=%s]".formatted(id);
  }

  @JsonGetter("name")
  public String name() {
    String name =
        Stream.of(firstName, lastName)
            .filter(org.apache.commons.lang3.StringUtils::isNotBlank)
            .collect(Collectors.joining(" "));
    return name.isEmpty() ? null : name;
  }

  @JsonGetter("initials")
  public String initials() {
    String initials =
        Stream.of(firstName, lastName)
            .filter(org.apache.commons.lang3.StringUtils::isNotBlank)
            .map(s -> s.substring(0, 1).toUpperCase())
            .collect(Collectors.joining(""));
    return initials.isEmpty() ? null : initials;
  }
}
