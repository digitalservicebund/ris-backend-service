package de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype;

import lombok.Builder;

// Todo update domain with uuid and document_category?
@Builder
public record DocumentType(String jurisShortcut, String label) {}
