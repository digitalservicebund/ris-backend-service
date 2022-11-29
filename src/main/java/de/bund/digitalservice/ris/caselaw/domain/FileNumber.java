package de.bund.digitalservice.ris.caselaw.domain;

public record FileNumber(Long id, Long documentUnitId, String fileNumber, Boolean isDeviating) {}
