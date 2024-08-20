package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.domain.HandoverReport;
import de.bund.digitalservice.ris.caselaw.domain.HandoverReportRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * Postgres Repository for reports (responses from the mail API) of performed jDV handover
 * operations.
 */
@Repository
public class PostgresHandoverReportRepositoryImpl implements HandoverReportRepository {

  private final DatabaseHandoverReportRepository repository;
  private final DatabaseDocumentationUnitRepository documentationUnitRepository;

  public PostgresHandoverReportRepositoryImpl(
      DatabaseHandoverReportRepository repository,
      DatabaseDocumentationUnitRepository documentationUnitRepository) {

    this.repository = repository;
    this.documentationUnitRepository = documentationUnitRepository;
  }

  /**
   * Saves all handover reports to the database.
   *
   * @param reports the handover reports to save
   * @return the saved handover reports
   */
  @Override
  public List<HandoverReport> saveAll(List<HandoverReport> reports) {
    List<HandoverReportDTO> handoverReportDTOS =
        reports.stream()
            .map(
                report -> {
                  Optional<DocumentationUnitDTO> documentationUnitDTO =
                      documentationUnitRepository.findByDocumentNumber(report.documentNumber());

                  return documentationUnitDTO
                      .map(
                          dto ->
                              HandoverReportDTO.builder()
                                  .documentationUnitId(dto.getId())
                                  .receivedDate(report.receivedDate())
                                  .content(report.content())
                                  .build())
                      .orElse(null);
                })
            .filter(Objects::nonNull)
            .toList();

    return repository.saveAll(handoverReportDTOS).stream()
        .map(
            report ->
                HandoverReport.builder()
                    .receivedDate(report.getReceivedDate())
                    .content(report.getContent())
                    .build())
        .toList();
  }

  /**
   * Retrieves all handover reports for a given documentation unit.
   *
   * @param documentationUnitUuid the document unit UUID
   * @return the handover reports
   */
  @Override
  public List<HandoverReport> getAllByDocumentationUnitUuid(UUID documentationUnitUuid) {
    return repository.findAllByDocumentationUnitId(documentationUnitUuid).stream()
        .map(
            report ->
                HandoverReport.builder()
                    .receivedDate(report.getReceivedDate())
                    .content(report.getContent())
                    .build())
        .toList();
  }
}
