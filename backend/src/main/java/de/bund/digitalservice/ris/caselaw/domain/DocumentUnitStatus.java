package de.bund.digitalservice.ris.caselaw.domain;

import lombok.Builder;

@Builder
public record DocumentUnitStatus(PublicationStatus publicationStatus, boolean withError) {}
