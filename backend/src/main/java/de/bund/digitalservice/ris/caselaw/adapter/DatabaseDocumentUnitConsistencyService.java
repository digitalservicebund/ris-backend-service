package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocUnitConsistencyRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DatabaseDocumentUnitConsistencyService {
  private final DatabaseDocUnitConsistencyRepository consistencyRepository;

  public DatabaseDocumentUnitConsistencyService(
      DatabaseDocUnitConsistencyRepository consistencyRepository) {
    this.consistencyRepository = consistencyRepository;
  }

  /**
   * Both decision and pending_proceeding inherit from documentation_unit. It is not possible to
   * define foreign key constraints to ensure there are no documentation units without either a
   * decision or pending proceeding. This illegal state leads to JPA errors when loading such
   * entities.
   *
   * <p>If such an error is logged, the corresponding documentation units must be manually deleted.
   */
  @Scheduled(cron = "20 23 5 * * *", zone = "Europe/Berlin")
  @SchedulerLock(name = "doc-unit-inheritance-consistency", lockAtMostFor = "PT5M")
  @Transactional
  public void checkInheritanceConsistency() {
    try {
      List<DocumentationUnitIdentifier> invalidDocUnits =
          this.consistencyRepository.findDocUnitsWithoutDecisionOrPendingProceeding();
      if (!invalidDocUnits.isEmpty()) {
        String docNumbers =
            invalidDocUnits.stream()
                .map(DocumentationUnitIdentifier::documentNumber)
                .collect(Collectors.joining(", "));
        String docIds =
            invalidDocUnits.stream()
                .map(DocumentationUnitIdentifier::id)
                .map(UUID::toString)
                .collect(Collectors.joining(", "));
        log.error(
            "Found documentation units without decision or pending proceeding. Please delete them manually. doc-nr's: [{}], ids: [{}]",
            docNumbers,
            docIds);
      }
    } catch (Exception e) {
      log.error("Error while checking doc unit inheritance consistency XXX", e);
    }
  }

  public record DocumentationUnitIdentifier(UUID id, String documentNumber) {}
}
