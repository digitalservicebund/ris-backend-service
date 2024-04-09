package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.OriginalFileDocumentDTO;
import de.bund.digitalservice.ris.caselaw.domain.OriginalFileDocument;

public class OriginalFileDocumentTransformer {

  public static OriginalFileDocument transformToDomain(OriginalFileDocumentDTO dto) {
    return OriginalFileDocument.builder()
        .name(dto.getFilename())
        .extension(dto.getExtension())
        .s3path(dto.getS3ObjectPath())
        .uploadTimestamp(dto.getUploadTimestamp())
        .build();
  }
}
