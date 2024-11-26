package de.bund.digitalservice.ris.caselaw.domain;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.jobs.annotations.Recurring;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ScheduledPublicationService {

  private final DocumentationUnitRepository repository;

  private final HandoverService handoverService;

  public ScheduledPublicationService(
      DocumentationUnitRepository repository, HandoverService handoverService) {

    this.repository = repository;
    this.handoverService = handoverService;
  }

  @Recurring(id = "scheduled-publication-job", interval = "PT1M") // Runs every 1 minute
  @Job(name = "Publish scheduled doc units that are due")
  public void handoverScheduledDocUnits() {
    var scheduledDocUnitsDueNow = this.repository.getScheduledDocumentationUnitsDueNow();
    if (!scheduledDocUnitsDueNow.isEmpty()) {
      log.info("Publishing {} scheduled doc units due now", scheduledDocUnitsDueNow.size());
    }
    for (var docUnit : scheduledDocUnitsDueNow) {
      // Even if the publication fails, we want to unset the scheduling.
      handoverDocument(docUnit);
      setPublicationDates(docUnit);
    }
  }

  private void handoverDocument(DocumentationUnit docUnit) {
    try {
      this.handoverService.handoverDocumentationUnitAsMail(docUnit.uuid(), "mail@example.local");
    } catch (Exception e) {
      log.error(
          "Could not publish scheduled doc unit {}, scheduling will still be removed",
          docUnit.documentNumber());
    }
  }

  private void setPublicationDates(DocumentationUnit docUnit) {
    try {
      var updatedDocUnit =
          docUnit.toBuilder()
              .managementData(
                  docUnit.managementData().toBuilder()
                      .scheduledPublicationDateTime(null)
                      .lastPublicationDateTime(LocalDateTime.now())
                      .build())
              .build();
      repository.save(updatedDocUnit);
    } catch (Exception e) {
      log.error("Could not remove the scheduling for doc unit {}", docUnit.documentNumber());
    }
  }
}
