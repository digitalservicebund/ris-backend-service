package de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype;

import java.util.UUID;
import lombok.Builder;

// Todo update domain with uuid and document_category?
@Builder
public record DocumentType(UUID uuid, String jurisShortcut, String label) {}
