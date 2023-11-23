package de.bund.digitalservice.ris.caselaw.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;

@Builder
public record DocumentationUnitSearchResult(
    @JsonProperty("documentationUnitId") UUID uuid,
    String documentNumber,
    Court court,
    String fileNumber,
    String fileName,
    LocalDate decisionDate,
    DocumentType documentType,
    Status status) {}
