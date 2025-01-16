package de.bund.digitalservice.ris.caselaw.domain.languagetool;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface TextCorrectionService {
  Object check(String text) throws JsonProcessingException;
}
