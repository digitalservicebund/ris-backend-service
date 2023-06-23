package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import reactor.core.publisher.Mono;

public interface ExporterHtmlReportRepository {

  Mono<String> saveAll(List<ExporterHtmlReport> report);
}
