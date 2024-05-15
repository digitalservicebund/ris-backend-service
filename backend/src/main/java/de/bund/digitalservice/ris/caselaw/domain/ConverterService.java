package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.docx.Docx2Html;

public interface ConverterService {
  Docx2Html getConvertedObject(String fileName);
}
