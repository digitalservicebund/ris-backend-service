package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PortalPublicationJobDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PortalPublicationJobRepository;
import de.bund.digitalservice.ris.caselaw.domain.PortalPublicationTaskStatus;
import de.bund.digitalservice.ris.caselaw.domain.PortalPublicationTaskType;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PortalPublicationJobService {

  private final PortalPublicationJobRepository publicationJobRepository;
  private final PublicPortalPublicationService publicPortalPublicationService;

  public PortalPublicationJobService(
      PortalPublicationJobRepository publicationJobRepository,
      PublicPortalPublicationService publicPortalPublicationService) {
    this.publicationJobRepository = publicationJobRepository;
    this.publicPortalPublicationService = publicPortalPublicationService;
  }

  //                        ↓ day of month (1-31)
  //                      ↓ hour (0-23)
  //                    ↓ minute (0-59)
  //                 ↓ second (0-59)
  // Default:        0 30 2 * * * (After migration: CET: 4:30)
  @Scheduled(cron = "0 30 2 * * *")
  public void publishNightlyChangelog() {
    try {
      publicPortalPublicationService.uploadChangelog();
    } catch (Exception e) {
      log.error("Could not upload nightly changelog file.", e);
    }
  }

  @Scheduled(fixedDelayString = "PT5S")
  @SchedulerLock(name = "portal-publication-job", lockAtMostFor = "PT15M")
  public void executePendingJobs() {

    List<PortalPublicationJobDTO> pendingJobs = publicationJobRepository.findNextPendingJobsBatch();
    if (pendingJobs.isEmpty()) {
      return;
    }

    log.info("Executing {} portal publication jobs", pendingJobs.size());
    log.info(
        "Executing portal publication jobs for doc numbers: {}",
        pendingJobs.stream()
            .map(PortalPublicationJobDTO::getDocumentNumber)
            .collect(Collectors.joining(", ")));
    for (PortalPublicationJobDTO job : pendingJobs) {
      executeJob(job);
    }
    var publicationResult = writeChangelog(pendingJobs);
    publicationJobRepository.saveAll(pendingJobs);

    log.info(
        "Portal publication jobs successfully executed: {} units published, {} units deleted.",
        publicationResult.publishedCount,
        publicationResult.deletedCount);
  }

  private void executeJob(PortalPublicationJobDTO job) {
    if (job.getPublicationType() == PortalPublicationTaskType.PUBLISH) {
      try {
        this.publicPortalPublicationService.publishDocumentationUnit(job.getDocumentNumber());
        job.setPublicationStatus(PortalPublicationTaskStatus.SUCCESS);
      } catch (Exception e) {
        log.error("Could not publish documentation unit {}", job.getDocumentNumber(), e);
        job.setPublicationStatus(PortalPublicationTaskStatus.ERROR);
      }
    }

    if (job.getPublicationType() == PortalPublicationTaskType.DELETE) {
      try {
        this.publicPortalPublicationService.deleteDocumentationUnit(job.getDocumentNumber());
        job.setPublicationStatus(PortalPublicationTaskStatus.SUCCESS);
      } catch (Exception e) {
        log.error("Could not unpublish documentation unit {}", job.getDocumentNumber(), e);
        job.setPublicationStatus(PortalPublicationTaskStatus.ERROR);
      }
    }
  }

  private PublicationResult writeChangelog(List<PortalPublicationJobDTO> pendingJobs) {
    List<PortalPublicationJobDTO> successFullJobsWithoutDuplicates =
        pendingJobs.stream()
            .filter(job -> job.getPublicationStatus() == PortalPublicationTaskStatus.SUCCESS)
            .collect(
                Collectors.toMap(
                    PortalPublicationJobDTO::getDocumentNumber,
                    job -> job,
                    (existing, replacement) ->
                        existing.getCreatedAt().isAfter(replacement.getCreatedAt())
                            ? existing
                            : replacement))
            .values()
            .stream()
            .toList();

    List<String> publishDocNumbers =
        successFullJobsWithoutDuplicates.stream()
            .filter(job -> job.getPublicationType() == PortalPublicationTaskType.PUBLISH)
            .map(job -> job.getDocumentNumber() + ".xml")
            .toList();
    List<String> deletedDocNumbers =
        successFullJobsWithoutDuplicates.stream()
            .filter(job -> job.getPublicationType() == PortalPublicationTaskType.DELETE)
            .map(job -> job.getDocumentNumber() + ".xml")
            .toList();

    // disabled until portal team tells us to write individual batch changelogs again
    //    if (!publishDocNumbers.isEmpty() || !deletedDocNumbers.isEmpty()) {
    //      try {
    //        this.internalPortalPublicationService.uploadChangelog(publishDocNumbers,
    // deletedDocNumbers);
    //      } catch (Exception e) {
    //        log.error("Could not upload changelog file.", e);
    //      }
    //    }

    return new PublicationResult(publishDocNumbers.size(), deletedDocNumbers.size());
  }

  private record PublicationResult(int publishedCount, int deletedCount) {}
}
