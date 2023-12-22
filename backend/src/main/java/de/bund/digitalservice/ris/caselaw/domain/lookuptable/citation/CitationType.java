package de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation;

import java.util.UUID;
import lombok.Builder;

@Builder
public record CitationType(UUID uuid, String jurisShortcut, String label) {}
