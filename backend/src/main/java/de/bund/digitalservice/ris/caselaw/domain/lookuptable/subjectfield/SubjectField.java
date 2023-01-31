package de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield;

import lombok.Builder;

@Builder
public record SubjectField(
    Long id, String subjectFieldNumber, String subjectFieldText, String navigationTerm) {}
