package de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype;

import java.util.UUID;
import lombok.Builder;

@Builder
public record DocumentType(UUID uuid, String jurisShortcut, String label) {}
