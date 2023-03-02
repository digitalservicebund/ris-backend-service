package de.bund.digitalservice.ris.caselaw.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder(toBuilder = true)
public record DocumentUnit(
    UUID uuid,
    @Size(min = 13, max = 14, message = "documentNumber has to be 13 or 14 characters long")
        String documentNumber,
    @PastOrPresent Instant creationtimestamp,
    @PastOrPresent Instant fileuploadtimestamp,
    DataSource dataSource,
    String s3path,
    String filetype,
    String filename,
    @Valid CoreData coreData,
    List<PreviousDecision> previousDecisions,
    Texts texts,
    ContentRelatedIndexing contentRelatedIndexing) {
  public static final DocumentUnit EMPTY =
      new DocumentUnit(null, null, null, null, null, null, null, null, null, null, null, null);
}
