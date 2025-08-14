package de.bund.digitalservice.ris.caselaw.domain;

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
    LocalDate publicationDate,
    String uri,
    String htmlLink) {}
