package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PortalPublicationJobDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PortalPublicationJobRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PortalPublicationJobService {

  private final PortalPublicationJobRepository publicationJobRepository;

  public PortalPublicationJobService(PortalPublicationJobRepository publicationJobRepository) {
    this.publicationJobRepository = publicationJobRepository;
  }

  @Scheduled(fixedDelayString = "PT5S")
  @SchedulerLock(name = "portal-publication-job", lockAtMostFor = "PT1H")
  public void executePendingJobs() {
    List<PortalPublicationJobDTO> pendingJobs = publicationJobRepository.findPendingJobs();
    if (pendingJobs.isEmpty()) {
      return;
    }
  }
}
