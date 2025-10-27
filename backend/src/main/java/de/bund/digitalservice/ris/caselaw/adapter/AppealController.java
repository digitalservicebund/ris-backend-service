package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseAppealStatusRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseAppellantRepository;
import de.bund.digitalservice.ris.caselaw.domain.appeal.AppealStatus;
import de.bund.digitalservice.ris.caselaw.domain.appeal.Appellant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/caselaw/appeal")
@Slf4j
public class AppealController {

  DatabaseAppellantRepository databaseAppellantRepository;
  DatabaseAppealStatusRepository databaseAppealStatusRepository;

  public AppealController(
      DatabaseAppellantRepository databaseAppellantRepository,
      DatabaseAppealStatusRepository databaseAppealStatusRepository) {
    this.databaseAppellantRepository = databaseAppellantRepository;
    this.databaseAppealStatusRepository = databaseAppealStatusRepository;
  }

  /**
   * Retrieves a list of possible options for appellants (Rechtsmittelführer) GET
   * /api/v1/caselaw/appeal/appellants
   *
   * @return ResponseEntity with a List of all possible Appellant options
   */
  @GetMapping("/appellants")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<Appellant>> getAppellants() {
    var appellants =
        databaseAppellantRepository.findAll().stream()
            .map(appellantDTO -> new Appellant(appellantDTO.getId(), appellantDTO.getValue()))
            .toList();
    return ResponseEntity.ok(appellants);
  }

  @GetMapping("/statuses")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<AppealStatus>> getStatuses() {
    var statuses =
        databaseAppealStatusRepository.findAll().stream()
            .map(status -> new AppealStatus(status.getId(), status.getValue()))
            .toList();
    return ResponseEntity.ok(statuses);
  }
}
