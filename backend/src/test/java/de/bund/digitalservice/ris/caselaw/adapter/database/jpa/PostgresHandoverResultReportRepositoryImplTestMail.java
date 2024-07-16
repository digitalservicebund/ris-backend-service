package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.domain.HandoverReport;
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
class PostgresHandoverResultReportRepositoryImplTestMail {

  PostgresHandoverReportRepositoryImpl reportRepository;
  @MockBean private DatabaseHandoverReportRepository handoverReportRepository;
  @MockBean private DatabaseDocumentationUnitRepository documentUnitRepository;

  @BeforeEach
  public void setup() {
    this.reportRepository =
        new PostgresHandoverReportRepositoryImpl(handoverReportRepository, documentUnitRepository);
  }

  @Test
  void saveAll() {
    var docUnit = DocumentationUnitDTO.builder().build();
    Instant received = Instant.now();

    when(documentUnitRepository.findByDocumentNumber("ABC126543712683"))
        .thenReturn(Optional.of(docUnit));

    when(handoverReportRepository.saveAll(any(Iterable.class)))
        .thenReturn(
            List.of(
                HandoverReportDTO.builder()
                    .content("report content")
                    .receivedDate(received)
                    .documentUnitId(docUnit.getId())
                    .build()));

    HandoverReport report =
        HandoverReport.builder()
            .documentNumber("ABC126543712683")
            .content("report content")
            .receivedDate(received)
            .build();

    List<HandoverReport> savedReportList =
        reportRepository.saveAll(Collections.singletonList(report));

    assertThat(savedReportList).hasSize(1);
    assertThat(savedReportList.get(0).content()).isEqualTo("report content");
    assertThat(savedReportList.get(0).receivedDate()).isEqualTo(received);

    verify(handoverReportRepository)
        .saveAll(
            Collections.singletonList(
                HandoverReportDTO.builder()
                    .content("report content")
                    .receivedDate(received)
                    .documentUnitId(docUnit.getId())
                    .id(any())
                    .build()));
  }
}
