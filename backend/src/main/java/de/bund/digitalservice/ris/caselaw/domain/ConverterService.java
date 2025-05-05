package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.docx.Docx2Html;
import java.util.UUID;

public interface ConverterService {
  Docx2Html getConvertedObject(String fileName);

  Docx2Html getConvertedObject(String fileName, String format, UUID documentationUnitId);
}
