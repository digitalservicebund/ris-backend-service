package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.domain.PublicationReport;
import de.bund.digitalservice.ris.caselaw.domain.PublicationReportRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresPublicationReportRepositoryImpl implements PublicationReportRepository {

  private final DatabasePublicationReportRepository repository;
  private final DatabaseDocumentationUnitRepository documentUnitRepository;

  public PostgresPublicationReportRepositoryImpl(
      DatabasePublicationReportRepository repository,
      DatabaseDocumentationUnitRepository documentUnitRepository) {

    this.repository = repository;
    this.documentUnitRepository = documentUnitRepository;
  }

  @Override
  public List<PublicationReport> saveAll(List<PublicationReport> reports) {
    List<PublicationReportDTO> publicationReportDTOs =
        reports.stream()
            .map(
                report -> {
                  Optional<DocumentationUnitDTO> documentUnitDTO =
                      documentUnitRepository.findByDocumentNumber(report.documentNumber());

                  return documentUnitDTO
                      .map(
                          documentationUnitDTO ->
                              PublicationReportDTO.builder()
                                  .documentUnitId(documentationUnitDTO.getId())
                                  .receivedDate(report.receivedDate())
                                  .content(report.content())
                                  .build())
                      .orElse(null);
                })
            .filter(Objects::nonNull)
            .toList();

    return repository.saveAll(publicationReportDTOs).stream()
        .map(
            report ->
                PublicationReport.builder()
                    .receivedDate(report.getReceivedDate())
                    .content(report.getContent())
                    .build())
        .toList();
  }

  @Override
  public List<PublicationReport> getAllByDocumentUnitUuid(UUID documentUnitUuid) {
    return repository.findAllByDocumentUnitId(documentUnitUuid).stream()
        .map(
            report ->
                PublicationReport.builder()
                    .receivedDate(report.getReceivedDate())
                    .content(report.getContent())
                    .build())
        .toList();
  }
}
