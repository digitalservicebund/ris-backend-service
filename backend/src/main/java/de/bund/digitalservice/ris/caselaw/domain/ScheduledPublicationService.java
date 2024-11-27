package de.bund.digitalservice.ris.caselaw.domain;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ScheduledPublicationService {

  private final DocumentationUnitRepository repository;

  private final HandoverService handoverService;

  /**
   * "Terminierte Abgabe": Cron job that finds doc units that have a scheduled publishing date-time
   * now, or in the past and published them. Even if publication fails, we will unset the scheduling
   * and save the timestamp of the last attempt.
   */
  public ScheduledPublicationService(
      DocumentationUnitRepository repository, HandoverService handoverService) {

    this.repository = repository;
    this.handoverService = handoverService;
  }

  @Scheduled(fixedRateString = "PT1M") // Runs every minute
  @SchedulerLock(name = "scheduled-publication-job", lockAtMostFor = "PT3M")
  public void handoverScheduledDocUnits() {
    var scheduledDocUnitsDueNow = this.repository.getScheduledDocumentationUnitsDueNow();
    if (!scheduledDocUnitsDueNow.isEmpty()) {
      log.info("Publishing {} scheduled doc units due now", scheduledDocUnitsDueNow.size());
    }
    for (var docUnit : scheduledDocUnitsDueNow) {
      // We will continue processing on any exceptions that are thrown.
      handoverDocument(docUnit);
      savePublicationDates(docUnit);
    }
  }

  private void handoverDocument(DocumentationUnit docUnit) {
    try {
      String email = docUnit.managementData().scheduledByEmail();
      this.handoverService.handoverDocumentationUnitAsMail(docUnit.uuid(), email);
    } catch (Exception e) {
      // No rethrow: even if the publication fails, we want to unset the scheduling.
      log.error(
          "Could not publish scheduled doc unit {}, scheduling will still be removed",
          docUnit.documentNumber(),
          e);
    }
  }

  private void savePublicationDates(DocumentationUnit docUnit) {
    try {
      var updatedDocUnit = setPublicationDates(docUnit);
      repository.save(updatedDocUnit);
    } catch (Exception e) {
      // No rethrow: continue with other doc units, even if save fails for this one.
      log.error("Could not remove the scheduling for doc unit {}", docUnit.documentNumber(), e);
    }
  }

  private DocumentationUnit setPublicationDates(DocumentationUnit docUnit) {
    return docUnit.toBuilder()
        .managementData(
            docUnit.managementData().toBuilder()
                .scheduledPublicationDateTime(null)
                .lastPublicationDateTime(LocalDateTime.now())
                .build())
        .build();
  }
}
