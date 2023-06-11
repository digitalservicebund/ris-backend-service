package de.bund.digitalservice.ris.caselaw.domain;

import reactor.core.publisher.Mono;

public interface DocumentNumberService {
  Mono<String> generateNextDocumentNumber(DocumentationOffice documentationOffice);
}
