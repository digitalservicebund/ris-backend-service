package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;

public interface ConverterService {
  Attachment2Html getConvertedObject(String s3Path);

  Attachment2Html getConvertedObject(String format, String s3Path, UUID documentationUnitId);
}
