package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PortalPublicationJobDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PortalPublicationJobRepository;
import de.bund.digitalservice.ris.caselaw.domain.PortalPublicationTaskStatus;
import de.bund.digitalservice.ris.caselaw.domain.PortalPublicationTaskType;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class PortalPublicationJobService {

  private final PortalPublicationJobRepository publicationJobRepository;
  private final PublicPortalPublicationService internalPortalPublicationService;

  public PortalPublicationJobService(
      PortalPublicationJobRepository publicationJobRepository,
      PublicPortalPublicationService publicPortalPublicationService) {
    this.publicationJobRepository = publicationJobRepository;
    this.internalPortalPublicationService = publicPortalPublicationService;
  }

  @Scheduled(fixedDelayString = "PT5S")
  @SchedulerLock(name = "portal-publication-job", lockAtMostFor = "PT1H")
  @Transactional
  public void executePendingJobs() {

    List<PortalPublicationJobDTO> pendingJobs = publicationJobRepository.findAllPendingJobs();
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
    var publicationResult = publishChangelog(pendingJobs);
    publicationJobRepository.saveAll(pendingJobs);

    log.info(
        "Portal publication jobs successfully executed: {} units published, {} units deleted.",
        publicationResult.publishedCount,
        publicationResult.deletedCount);
  }

  private void executeJob(PortalPublicationJobDTO job) {
    if (job.getPublicationType() == PortalPublicationTaskType.PUBLISH) {
      try {
        this.internalPortalPublicationService.publishDocumentationUnit(job.getDocumentNumber());
        job.setPublicationStatus(PortalPublicationTaskStatus.SUCCESS);
      } catch (Exception e) {
        log.error("Could not publish documentation unit {}", job.getDocumentNumber(), e);
        job.setPublicationStatus(PortalPublicationTaskStatus.ERROR);
      }
    }

    if (job.getPublicationType() == PortalPublicationTaskType.DELETE) {
      try {
        this.internalPortalPublicationService.deleteDocumentationUnit(job.getDocumentNumber());
        job.setPublicationStatus(PortalPublicationTaskStatus.SUCCESS);
      } catch (Exception e) {
        log.error("Could not unpublish documentation unit {}", job.getDocumentNumber(), e);
        job.setPublicationStatus(PortalPublicationTaskStatus.ERROR);
      }
    }
  }

  private PublicationResult publishChangelog(List<PortalPublicationJobDTO> pendingJobs) {
    HashSet<String> publishDocNumbers =
        pendingJobs.stream()
            .filter(job -> job.getPublicationType() == PortalPublicationTaskType.PUBLISH)
            .filter(job -> job.getPublicationStatus() == PortalPublicationTaskStatus.SUCCESS)
            .map(PortalPublicationJobDTO::getDocumentNumber)
            .collect(Collectors.toCollection(HashSet::new));
    HashSet<String> deletedDocNumbers =
        pendingJobs.stream()
            .filter(job -> job.getPublicationType() == PortalPublicationTaskType.DELETE)
            .filter(job -> job.getPublicationStatus() == PortalPublicationTaskStatus.SUCCESS)
            .map(PortalPublicationJobDTO::getDocumentNumber)
            .collect(Collectors.toCollection(HashSet::new));

    var overlap = new HashSet<>(publishDocNumbers);
    overlap.retainAll(deletedDocNumbers);
    if (!overlap.isEmpty()) {
      overlap.forEach(
          docNumber -> {
            var latest =
                pendingJobs.stream()
                    .filter(job -> job.getDocumentNumber().equals(docNumber))
                    .sorted(Comparator.comparing(PortalPublicationJobDTO::getCreatedAt))
                    .toList()
                    .getLast();
            if (latest.getPublicationType() == PortalPublicationTaskType.PUBLISH) {
              deletedDocNumbers.removeAll(List.of(docNumber));
            } else {
              publishDocNumbers.removeAll(List.of(docNumber));
            }
          });
    }

    if (!publishDocNumbers.isEmpty() || !deletedDocNumbers.isEmpty()) {
      try {
        this.internalPortalPublicationService.uploadChangelog(
            publishDocNumbers.stream().map(it -> it + ".xml").toList(),
            deletedDocNumbers.stream().map(it -> it + ".xml").toList());
      } catch (Exception e) {
        log.error("Could not upload changelog file.", e);
      }
    }

    return new PublicationResult(publishDocNumbers.size(), deletedDocNumbers.size());
  }

  private record PublicationResult(int publishedCount, int deletedCount) {}
}
