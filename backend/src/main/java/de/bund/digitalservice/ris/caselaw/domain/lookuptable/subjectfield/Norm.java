package de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield;

import lombok.Builder;

@Builder
public record Norm(String shortcut, String enbez) {}
