package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.domain.PublicationReport;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class PostgresPublicationReportRepositoryImplTest {

  PostgresPublicationReportRepositoryImpl reportRepository;
  @MockBean private DatabasePublicationReportRepository publicationReportRepository;
  @MockBean private DatabaseDocumentationUnitRepository documentUnitRepository;

  @BeforeEach
  public void setup() {
    this.reportRepository =
        new PostgresPublicationReportRepositoryImpl(
            publicationReportRepository, documentUnitRepository);
  }

  @Test
  void saveAll() {
    var docUnit = DocumentationUnitDTO.builder().build();
    Instant received = Instant.now();

    when(documentUnitRepository.findByDocumentNumber("ABC126543712683"))
        .thenReturn(Optional.of(docUnit));

    when(publicationReportRepository.saveAll(any(Iterable.class)))
        .thenReturn(
            List.of(
                PublicationReportDTO.builder()
                    .content("report content")
                    .receivedDate(received)
                    .documentUnitId(docUnit.getId())
                    .build()));

    PublicationReport report =
        PublicationReport.builder()
            .documentNumber("ABC126543712683")
            .content("report content")
            .receivedDate(received)
            .build();

    List<PublicationReport> savedReportList =
        reportRepository.saveAll(Collections.singletonList(report));

    assertThat(savedReportList).hasSize(1);
    assertThat(savedReportList.get(0).content()).isEqualTo("report content");
    assertThat(savedReportList.get(0).receivedDate()).isEqualTo(received);

    verify(publicationReportRepository)
        .saveAll(
            Collections.singletonList(
                PublicationReportDTO.builder()
                    .content("report content")
                    .receivedDate(received)
                    .documentUnitId(docUnit.getId())
                    .id(any())
                    .build()));
  }
}
