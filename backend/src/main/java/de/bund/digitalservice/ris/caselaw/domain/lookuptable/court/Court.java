package de.bund.digitalservice.ris.caselaw.domain.lookuptable.court;

import lombok.Builder;

@Builder
public record Court(String type, String location, String label, String revoked) {}
