package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import de.bund.digitalservice.ris.caselaw.domain.ExporterHtmlReport;
import de.bund.digitalservice.ris.caselaw.domain.ExporterHtmlReportRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

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
  public Flux<ExporterHtmlReport> saveAll(List<ExporterHtmlReport> reports) {
    return Flux.fromIterable(reports)
        .flatMap(
            report ->
                documentUnitRepository
                    .findByDocumentnumber(report.documentNumber())
                    .map(
                        documentUnit ->
                            ExporterHtmlReportDTO.builder()
                                .id(UUID.randomUUID())
                                .documentUnitId(documentUnit.getUuid())
                                .receivedDate(report.receivedDate())
                                .html(report.html())
                                .newEntry(true)
                                .build()))
        .collectList()
        .flatMapMany(repository::saveAll)
        .map(
            report ->
                ExporterHtmlReport.builder()
                    // TODO add documentNumber
                    .receivedDate(report.getReceivedDate())
                    .html(report.getHtml())
                    .build());
  }
}
