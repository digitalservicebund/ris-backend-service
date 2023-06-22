package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import de.bund.digitalservice.ris.caselaw.domain.ExporterHtmlReport;
import de.bund.digitalservice.ris.caselaw.domain.ExporterHtmlReportRepository;
import java.util.List;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class PostgresExporterHtmlReportRepositoryImpl implements ExporterHtmlReportRepository {

  private final DatabaseExporterHtmlReportRepository repository;
  private final DatabaseDocumentUnitRepository documentUnitRepository;

  public PostgresExporterHtmlReportRepositoryImpl(
      DatabaseExporterHtmlReportRepository repository,
      DatabaseDocumentUnitRepository documentUnitRepository) {

    this.repository = repository;
    this.documentUnitRepository = documentUnitRepository;
  }

  @Override
  public Mono<Void> saveAll(List<ExporterHtmlReport> reports) {
    return Flux.fromIterable(reports)
        .flatMap(
            report ->
                documentUnitRepository
                    .findByDocumentnumber(report.documentNumber())
                    .map(
                        documentUnit ->
                            ExporterHtmlReportDTO.builder()
                                .documentUnitId(documentUnit.getUuid())
                                .receivedDate(report.receivedDate())
                                .html(report.html())
                                .build()))
        .collectList()
        .map(repository::saveAll)
        .flatMap(result -> Mono.empty());
  }
}
