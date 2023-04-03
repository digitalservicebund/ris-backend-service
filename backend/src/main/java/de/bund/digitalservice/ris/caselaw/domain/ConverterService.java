package de.bund.digitalservice.ris.caselaw.domain;

public interface ConverterService<ORIGINAL, CONVERTED> {
  CONVERTED getConvertedObject(String fileName);
}
