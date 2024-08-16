package de.bund.digitalservice.ris.caselaw.domain;

import lombok.Builder;

@Builder
public record User(String name, String email, DocumentationOffice documentationOffice) {}
