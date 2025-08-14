package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Instant;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record SearchResult(
    String ecli,
    String celex,
    String courtType,
    String courtLocation,
    LocalDate date,
    String fileNumber,
    Instant publicationDate,
    String uri,
    String htmlLink) {}
