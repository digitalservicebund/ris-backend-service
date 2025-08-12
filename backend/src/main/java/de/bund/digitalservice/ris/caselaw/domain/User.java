package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record User(
    UUID id,
    String name,
    String email,
    DocumentationOffice documentationOffice,
    List<String> roles,
    String initials) {}
