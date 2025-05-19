package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;

public interface ConverterService {
  Attachment2Html getConvertedObject(String fileName);

  Attachment2Html getConvertedObject(String format, String fileName, UUID documentationUnitId);
}
