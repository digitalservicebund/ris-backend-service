package de.bund.digitalservice.ris.caselaw.domain.lookuptable;

import java.util.UUID;
import lombok.Builder;

@Builder
public record Region(UUID id, String code, String longText, boolean applicability) {}
