package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PortalPublicationJobDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PortalPublicationJobRepository;
import de.bund.digitalservice.ris.caselaw.domain.PortalPublicationTaskStatus;
import de.bund.digitalservice.ris.caselaw.domain.PortalPublicationTaskType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PortalPublicationJobService {

  private final PortalPublicationJobRepository publicationJobRepository;
  private final PortalPublicationService portalPublicationService;

  public PortalPublicationJobService(
      PortalPublicationJobRepository publicationJobRepository,
      PortalPublicationService portalPublicationService) {
    this.publicationJobRepository = publicationJobRepository;
    this.portalPublicationService = portalPublicationService;
  }

  //                        ↓ day of month (1-31)
  //                      ↓ hour (0-23)
  //                    ↓ minute (0-59)
  //                 ↓ second (0-59)
  // Default:        0 30 4 * * * (After migration: 4:30)
  @Scheduled(cron = "0 30 4 * * *", zone = "Europe/Berlin")
  @SchedulerLock(name = "nightly-changelog-publish")
  public void publishNightlyChangelog() {
    try {
      portalPublicationService.uploadFullReindexChangelog();
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
    List<PortalPublicationResult> results = new ArrayList<>();
    for (PortalPublicationJobDTO job : pendingJobs) {
      var result = executeJob(job);
      if (result != null) results.add(result);
    }
    var publicationResult = writeChangelog(results, pendingJobs);
    publicationJobRepository.saveAll(pendingJobs);

    log.info(
        "Portal publication jobs successfully executed: {} files published, {} files deleted.",
        publicationResult.publishedCount,
        publicationResult.deletedCount);
  }

  private PortalPublicationResult executeJob(PortalPublicationJobDTO job) {
    if (job.getPublicationType() == PortalPublicationTaskType.PUBLISH) {
      try {
        var result =
            this.portalPublicationService.publishDocumentationUnit(job.getDocumentNumber());
        job.setPublicationStatus(PortalPublicationTaskStatus.SUCCESS);
        return result;
      } catch (Exception e) {
        log.error("Could not publish documentation unit {}", job.getDocumentNumber(), e);
        job.setPublicationStatus(PortalPublicationTaskStatus.ERROR);
      }
    }

    if (job.getPublicationType() == PortalPublicationTaskType.DELETE) {
      try {
        var result = this.portalPublicationService.deleteDocumentationUnit(job.getDocumentNumber());
        job.setPublicationStatus(PortalPublicationTaskStatus.SUCCESS);
        return result;
      } catch (Exception e) {
        log.error("Could not unpublish documentation unit {}", job.getDocumentNumber(), e);
        job.setPublicationStatus(PortalPublicationTaskStatus.ERROR);
      }
    }
    return null;
  }

  private PublicationResult writeChangelog(
      List<PortalPublicationResult> results, List<PortalPublicationJobDTO> pendingJobs) {
    Map<String, PortalPublicationJobDTO> jobsWithoutDuplicates =
        pendingJobs.stream()
            .filter(job -> job.getPublicationStatus() == PortalPublicationTaskStatus.SUCCESS)
            .collect(
                Collectors.toMap(
                    job -> job.getDocumentNumber() + "/" + job.getDocumentNumber() + ".xml",
                    job -> job,
                    (existing, replacement) ->
                        existing.getCreatedAt().isAfter(replacement.getCreatedAt())
                            ? existing
                            : replacement));

    Set<String> publishDocNumbers = new HashSet<>();
    Set<String> deletedDocNumbers = new HashSet<>();
    results.forEach(
        result -> {
          publishDocNumbers.addAll(result.changedPaths());
          deletedDocNumbers.addAll(result.deletedPaths());
        });

    var duplicates = new ArrayList<>(publishDocNumbers);
    duplicates.retainAll(deletedDocNumbers);

    duplicates.forEach(
        duplicate -> {
          var job = jobsWithoutDuplicates.get(duplicate);
          if (job == null) {
            publishDocNumbers.remove(duplicate);
            deletedDocNumbers.remove(duplicate);
          } else {
            if (job.getPublicationType() == PortalPublicationTaskType.PUBLISH) {
              deletedDocNumbers.removeIf(it -> it.equals(duplicate));
            } else {
              publishDocNumbers.removeIf(it -> it.equals(duplicate));
            }
          }
        });

    if (!publishDocNumbers.isEmpty() || !deletedDocNumbers.isEmpty()) {
      try {
        this.portalPublicationService.uploadChangelog(
            publishDocNumbers.stream().toList(), deletedDocNumbers.stream().toList());
      } catch (Exception e) {
        log.error("Could not upload changelog file.", e);
      }
    }

    return new PublicationResult(publishDocNumbers.size(), deletedDocNumbers.size());
  }

  private record PublicationResult(int publishedCount, int deletedCount) {}
}
