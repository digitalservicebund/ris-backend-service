package de.bund.digitalservice.ris.caselaw.domain;

import lombok.Builder;

@Builder
public record DocumentUnitStatus(PublicationStatus status, boolean withError) {}
