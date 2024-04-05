package de.bund.digitalservice.ris.caselaw.domain;

import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;

@Builder
public record SingleNorm(
    UUID id, String singleNorm, LocalDate dateOfVersion, String dateOfRelevance) {}
