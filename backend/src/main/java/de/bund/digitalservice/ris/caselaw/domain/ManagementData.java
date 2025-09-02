package de.bund.digitalservice.ris.caselaw.domain;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.PastOrPresent;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder(toBuilder = true)
public record ManagementData(
    @PastOrPresent LocalDateTime lastHandoverDateTime,
    @Future LocalDateTime scheduledPublicationDateTime,
    @Email String scheduledByEmail,
    List<DuplicateRelation> duplicateRelations,
    List<String> borderNumbers,
    Instant lastUpdatedAtDateTime,
    String lastUpdatedByName,
    String lastUpdatedByDocOffice,
    Instant createdAtDateTime,
    String createdByName,
    String createdByDocOffice,
    Instant firstPublishedAtDateTime,
    Instant lastPublishedAtDateTime) {}
