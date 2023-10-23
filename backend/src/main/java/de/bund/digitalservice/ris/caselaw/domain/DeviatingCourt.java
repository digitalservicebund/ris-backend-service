package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;
import lombok.Builder;

@Builder
public record DeviatingCourt(UUID id, String court) {}
