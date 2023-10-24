package de.bund.digitalservice.ris.caselaw.domain;

import lombok.Builder;

@Builder
@Deprecated
public record DocumentUnitStatus(PublicationStatus publicationStatus, boolean withError) {}
