package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import reactor.core.publisher.Flux;

public interface ExporterHtmlReportRepository {

  Flux<ExporterHtmlReport> saveAll(List<ExporterHtmlReport> report);
}
