package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record DocumentationOffice(String abbreviation, UUID id, List<ProcessStep> processSteps) {}
