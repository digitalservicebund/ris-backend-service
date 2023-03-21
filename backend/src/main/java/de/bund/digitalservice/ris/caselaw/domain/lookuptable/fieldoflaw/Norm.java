package de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw;

import lombok.Builder;

@Builder
public record Norm(String abbreviation, String singleNormDescription) {}
