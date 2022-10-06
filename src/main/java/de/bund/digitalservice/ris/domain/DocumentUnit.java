package de.bund.digitalservice.ris.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record DocumentUnit(
    Long id,
    UUID uuid,
    String documentNumber,
    Instant creationtimestamp,
    Instant fileuploadtimestamp,
    String s3path,
    String filetype,
    String filename,
    CoreData coreData,
    List<PreviousDecision> previousDecisions,
    Texts texts) {
  public static final DocumentUnit EMPTY =
      new DocumentUnit(null, null, null, null, null, null, null, null, null, null, null);
}
