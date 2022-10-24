package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PastOrPresent;
import lombok.Builder;

@Builder
public record DocumentUnit(
    Long id,
    UUID uuid,
    // TODO validate content
    @NotBlank(message = "documentNumber can't be blank") String documentNumber,
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
