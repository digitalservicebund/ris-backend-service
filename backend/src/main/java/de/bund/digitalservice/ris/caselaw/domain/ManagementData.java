package de.bund.digitalservice.ris.caselaw.domain;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder(toBuilder = true)
public record ManagementData(
    @PastOrPresent LocalDateTime lastPublicationDateTime,
    @Future LocalDateTime scheduledPublicationDateTime,
    @Email String scheduledByEmail,
    List<DuplicateRelation> duplicateRelations,
    List<String> borderNumbers,
    LocalDateTime lastUpdatedAtDateTime,
    String lastUpdatedByName,
    String lastUpdatedByDocOffice,
    LocalDateTime createdAtDateTime,
    String createdByName,
    String createdByDocOffice,
    LocalDateTime firstPublishedAtDateTime) {}
