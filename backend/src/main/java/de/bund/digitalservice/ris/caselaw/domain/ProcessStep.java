package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;
import lombok.Builder;

@Builder
public record ProcessStep(UUID uuid, ProcessStepName name, String abbreviation) {}
