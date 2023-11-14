package de.bund.digitalservice.ris.caselaw.domain;

import lombok.Builder;

@Builder
public record Status(PublicationStatus publicationStatus, boolean withError) {}
