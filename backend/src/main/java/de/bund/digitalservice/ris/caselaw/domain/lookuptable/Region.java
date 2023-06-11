package de.bund.digitalservice.ris.caselaw.domain.lookuptable;

import lombok.Builder;

@Builder
public record Region(String code, String label) {}
