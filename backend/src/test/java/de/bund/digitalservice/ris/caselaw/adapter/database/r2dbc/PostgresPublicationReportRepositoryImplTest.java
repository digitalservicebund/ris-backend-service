package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.bund.digitalservice.ris.caselaw.domain.PublicationReport;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
class PostgresPublicationReportRepositoryImplTest {

  PostgresPublicationReportRepositoryImpl reportRepository;
  @MockBean private DatabasePublicationReportRepository publicationReportRepository;
  @MockBean private DatabaseDocumentUnitRepository documentUnitRepository;

  @BeforeEach
  public void setup() {
    this.reportRepository =
        new PostgresPublicationReportRepositoryImpl(
            publicationReportRepository, documentUnitRepository);
  }

  @Test
  void saveAll() {
    var docUnit = DocumentUnitDTO.builder().uuid(UUID.randomUUID()).build();
    Instant received = Instant.now();

    Mockito.when(documentUnitRepository.findByDocumentnumber("ABC126543712683"))
        .thenReturn(Mono.just(docUnit));

    Mockito.when(publicationReportRepository.saveAll(Mockito.any(Iterable.class)))
        .thenReturn(
            Flux.just(
                PublicationReportDTO.builder()
                    .content("report content")
                    .newEntry(true)
                    .receivedDate(received)
                    .documentUnitId(docUnit.getUuid())
                    .id(UUID.randomUUID())
                    .build()));

    PublicationReport report =
        PublicationReport.builder()
            .documentNumber("ABC126543712683")
            .content("report content")
            .receivedDate(received)
            .build();

    StepVerifier.create(reportRepository.saveAll(Collections.singletonList(report)))
        .consumeNextWith(
            publicationReport -> {
              assertEquals("report content", publicationReport.content());
              assertEquals(received, publicationReport.receivedDate());
            })
        .verifyComplete();

    Mockito.verify(publicationReportRepository)
        .saveAll(
            Collections.singletonList(
                PublicationReportDTO.builder()
                    .content("report content")
                    .newEntry(true)
                    .receivedDate(received)
                    .documentUnitId(docUnit.getUuid())
                    .id(Mockito.any())
                    .build()));
  }
}
