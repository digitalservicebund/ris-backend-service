package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.docx.Docx2Html;
import reactor.core.publisher.Mono;

public interface ConverterService {
  Mono<Docx2Html> getConvertedObject(String fileName);
}
