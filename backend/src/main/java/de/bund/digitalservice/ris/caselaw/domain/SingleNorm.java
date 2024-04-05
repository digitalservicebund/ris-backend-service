package de.bund.digitalservice.ris.caselaw.domain;

import java.time.LocalDate;
import lombok.Builder;

@Builder
public record SingleNorm(String singleNorm, LocalDate dateOfVersion, String dateOfRelevance) {}
