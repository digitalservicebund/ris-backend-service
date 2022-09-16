package de.bund.digitalservice.ris.domain;

import java.time.Instant;
import java.util.UUID;

public record DocumentUnit(
    UUID uuid,
    String documentnumber,
    Instant creationtimestamp,
    Instant fileuploadtimestamp,
    String s3path,
    String filetype,
    String filename,
    CoreData coreData,
    Texts texts) {
  public static final DocumentUnit EMPTY =
      new DocumentUnit(null, null, null, null, null, null, null, null, null);
}
