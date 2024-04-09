package de.bund.digitalservice.ris.caselaw.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder(toBuilder = true)
public record DocumentUnit(
    UUID uuid,
    @Size(min = 13, max = 14, message = "documentNumber has to be 13 or 14 characters long")
        String documentNumber,
    DataSource dataSource,
    List<OriginalFileDocument> originalFiles,
    @Valid CoreData coreData,
    List<PreviousDecision> previousDecisions,
    List<EnsuingDecision> ensuingDecisions,
    Texts texts,
    List<String> borderNumbers,
    Status status,
    ContentRelatedIndexing contentRelatedIndexing) {}
