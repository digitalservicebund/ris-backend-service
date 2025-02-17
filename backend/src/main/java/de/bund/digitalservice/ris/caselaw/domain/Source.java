package de.bund.digitalservice.ris.caselaw.domain;

import lombok.Builder;

@Builder(toBuilder = true)
public record Source(SourceValue value, String sourceRawValue) {}
