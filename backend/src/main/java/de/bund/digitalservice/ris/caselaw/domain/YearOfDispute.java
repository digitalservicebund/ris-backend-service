package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Year;
import java.util.UUID;
import lombok.Builder;

@Builder
public record YearOfDispute(UUID id, Year year) {}
