package de.bund.digitalservice.ris.caselaw.domain.appeal;

import java.util.UUID;
import lombok.Builder;

@Builder
public record Appellant(UUID id, String value) {}
