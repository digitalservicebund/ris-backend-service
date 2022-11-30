package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import javax.validation.Valid;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;
import lombok.Builder;

@Builder(toBuilder = true)
public record DocumentUnit(
    Long id,
    UUID uuid,
    @Size(min = 13, max = 14, message = "documentNumber has to be 13 or 14 characters long")
        String documentNumber,
    @PastOrPresent Instant creationtimestamp,
    @PastOrPresent Instant fileuploadtimestamp,
    String s3path,
    String filetype,
    String filename,
    @Valid CoreData coreData,
    List<PreviousDecision> previousDecisions,
    Texts texts) {
  public static final DocumentUnit EMPTY =
      new DocumentUnit(null, null, null, null, null, null, null, null, null, null, null);
}
