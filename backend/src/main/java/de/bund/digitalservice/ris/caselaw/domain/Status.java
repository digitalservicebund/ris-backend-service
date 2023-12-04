package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Instant;
import lombok.Builder;

@Builder
public record Status(PublicationStatus publicationStatus, boolean withError, Instant createdAt) {}
