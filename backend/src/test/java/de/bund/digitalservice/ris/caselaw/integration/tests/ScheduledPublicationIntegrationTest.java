package de.bund.digitalservice.ris.caselaw.integration.tests;

import static de.bund.digitalservice.ris.caselaw.AuthUtils.buildDSDocOffice;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.byLessThan;
import static org.assertj.core.api.Assertions.within;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import de.bund.digitalservice.ris.caselaw.EntityBuilderTestUtil;
import de.bund.digitalservice.ris.caselaw.adapter.MockXmlExporter;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitHistoryLogService;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLogEventType;
import de.bund.digitalservice.ris.caselaw.domain.User;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import(MockXmlExporter.class)
class ScheduledPublicationIntegrationTest extends BaseIntegrationTest {

  @Autowired private DatabaseDocumentationUnitRepository docUnitRepository;
  @Autowired private DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @Autowired private DocumentationUnitHistoryLogService docUnitHistoryLogService;

  private final DocumentationOffice docOffice = buildDSDocOffice();
  private DocumentationOfficeDTO documentationOffice;

  @BeforeEach
  void setUp() {
    documentationOffice =
        documentationOfficeRepository.findByAbbreviation(docOffice.abbreviation());
  }

  @Test
  @SuppressWarnings("java:S5961")
  void shouldPublishOnlyDueDocUnitsAndSendErrorNotificationOnSchedule() {
    // Valid doc unit -> publication will succeed
    DocumentationUnitDTO docUnitDueNow =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
            docUnitRepository,
            DecisionDTO.builder()
                .documentationOffice(documentationOffice)
                .documentNumber("docnr123456_1")
                .scheduledByEmail("test@example.local")
                .scheduledPublicationDateTime(LocalDateTime.now())
                .date(LocalDate.now()));

    // Doc unit is not yet due -> will not be touched
    DocumentationUnitDTO docUnitScheduledForFuture =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
            docUnitRepository,
            DecisionDTO.builder()
                .documentationOffice(documentationOffice)
                .documentNumber("docnr123456_2")
                .scheduledByEmail("test@example.local")
                .scheduledPublicationDateTime(LocalDateTime.now().plusMinutes(3))
                .date(LocalDate.now()));

    // Invalid doc unit will be unscheduled + send error notification
    DocumentationUnitDTO docUnitWithFailingXmlExport =
        EntityBuilderTestUtil.createAndSavePublishedDocumentationUnit(
            docUnitRepository,
            DecisionDTO.builder()
                .documentationOffice(documentationOffice)
                .documentNumber("docnr123456_3")
                .scheduledByEmail("invalid-docunit@example.local")
                .scheduledPublicationDateTime(LocalDateTime.now().minusMinutes(3))
                // Missing decision date let's MockXmlExporter fail
                .date(null));
    // The assertion might take longer -> record now beforehand.
    LocalDateTime now = LocalDateTime.now();

    await()
        .atMost(Duration.ofSeconds(62))
        .untilAsserted(
            () -> {
              var publishedDocUnit = docUnitRepository.findById(docUnitDueNow.getId()).get();
              assertThat(publishedDocUnit.getScheduledByEmail()).isNull();
              assertThat(publishedDocUnit.getScheduledPublicationDateTime()).isNull();
              assertThat(publishedDocUnit.getLastPublicationDateTime())
                  .isCloseTo(now, byLessThan(60, ChronoUnit.SECONDS));

              var failedDocUnit =
                  docUnitRepository.findById(docUnitWithFailingXmlExport.getId()).get();
              assertThat(failedDocUnit.getScheduledByEmail()).isNull();
              assertThat(failedDocUnit.getScheduledPublicationDateTime()).isNull();
              assertThat(failedDocUnit.getLastPublicationDateTime())
                  .isCloseTo(now, byLessThan(60, ChronoUnit.SECONDS));

              var scheduledDocUnit =
                  docUnitRepository.findById(docUnitScheduledForFuture.getId()).get();
              assertThat(scheduledDocUnit.getScheduledByEmail())
                  .isEqualTo(docUnitScheduledForFuture.getScheduledByEmail());
              assertThat(scheduledDocUnit.getScheduledPublicationDateTime())
                  .isCloseTo(
                      docUnitScheduledForFuture.getScheduledPublicationDateTime(),
                      byLessThan(1, ChronoUnit.SECONDS));
              assertThat(scheduledDocUnit.getLastPublicationDateTime()).isNull();
            });

    assertThat(docUnitRepository.findAll()).hasSize(3);

    var error = "Terminierte Abgabe fehlgeschlagen: ";
    var uuid = docUnitDueNow.getId();
    // One handover mail to jDV is sent out.
    verify(mailSender, times(1))
        .sendMail(any(), any(), argThat(s -> !s.startsWith(error)), any(), any(), eq(uuid + ""));

    var subject = error + docUnitWithFailingXmlExport.getDocumentNumber();
    // One error notification mail to the user is sent out.
    verify(mailSender, times(1))
        .sendMail(any(), eq("invalid-docunit@example.local"), eq(subject), any(), any(), any());

    var user = User.builder().documentationOffice(buildDSDocOffice()).build();
    var logs = docUnitHistoryLogService.getHistoryLogs(docUnitDueNow.getId(), user);

    assertThat(logs).hasSize(3);

    // The lastPublicationDate is set -> additional update event is logged
    assertThat(logs.get(0).eventType()).isEqualTo(HistoryLogEventType.UPDATE);
    assertThat(logs.get(0).createdBy()).isEqualTo("NeuRIS");

    assertThat(logs.get(1).description()).isEqualTo("Terminierte Abgabe gelöscht");
    assertThat(logs.get(1).createdBy()).isEqualTo("NeuRIS");
    assertThat(logs.get(1).eventType()).isEqualTo(HistoryLogEventType.SCHEDULED_PUBLICATION);
    assertThat(logs.get(1).createdAt()).isCloseTo(Instant.now(), within(5, ChronoUnit.SECONDS));
    assertThat(logs.get(1).documentationOffice()).isNull();

    assertThat(logs.get(2).description()).isEqualTo("Dokeinheit an jDV übergeben");
    assertThat(logs.get(2).createdBy()).isEqualTo("NeuRIS");
    assertThat(logs.get(2).eventType()).isEqualTo(HistoryLogEventType.HANDOVER);
    assertThat(logs.get(2).createdAt()).isCloseTo(Instant.now(), within(5, ChronoUnit.SECONDS));
    assertThat(logs.get(2).documentationOffice()).isNull();
  }
}
