package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import de.bund.digitalservice.ris.caselaw.domain.PublicationReport;
import de.bund.digitalservice.ris.caselaw.domain.PublishReportAttachmentRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public class PostgresPublishReportAttachmentAttachmentRepositoryImpl
    implements PublishReportAttachmentRepository {

  private final DatabasePublishReportAttachmentRepository repository;
  private final DatabaseDocumentUnitRepository documentUnitRepository;

  public PostgresPublishReportAttachmentAttachmentRepositoryImpl(
      DatabasePublishReportAttachmentRepository repository,
      DatabaseDocumentUnitRepository documentUnitRepository) {

    this.repository = repository;
    this.documentUnitRepository = documentUnitRepository;
  }

  @Override
  public Flux<PublicationReport> saveAll(List<PublicationReport> reports) {
    return Flux.fromIterable(reports)
        .flatMap(
            report ->
                documentUnitRepository
                    .findByDocumentnumber(report.documentNumber())
                    .map(
                        documentUnit ->
                            PublishReportAttachmentDTO.builder()
                                .id(UUID.randomUUID())
                                .documentUnitId(documentUnit.getUuid())
                                .receivedDate(report.receivedDate())
                                .content(report.content())
                                .newEntry(true)
                                .build()))
        .collectList()
        .flatMapMany(repository::saveAll)
        .map(
            report ->
                PublicationReport.builder()
                    // TODO add documentNumber?
                    .receivedDate(report.getReceivedDate())
                    .content(report.getContent())
                    .build());
  }

  @Override
  public Flux<PublicationReport> getAllForDocumentUnit(UUID documentUnitId) {
    return repository
        .findAllByDocumentUnitId(documentUnitId)
        .map(
            report ->
                PublicationReport.builder()
                    .receivedDate(report.getReceivedDate())
                    .content(report.getContent())
                    .build());
  }
}
