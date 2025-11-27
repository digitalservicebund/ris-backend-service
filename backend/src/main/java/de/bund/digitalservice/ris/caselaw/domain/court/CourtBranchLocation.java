package de.bund.digitalservice.ris.caselaw.domain.court;

import java.util.UUID;
import lombok.Builder;

@Builder
public record CourtBranchLocation(UUID id, String value) {}
