package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseAppealStatusRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseAppellantRepository;
import de.bund.digitalservice.ris.caselaw.domain.appeal.AppealStatus;
import de.bund.digitalservice.ris.caselaw.domain.appeal.Appellant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AppealOptionsService {

  private final DatabaseAppellantRepository databaseAppellantRepository;
  private final DatabaseAppealStatusRepository databaseAppealStatusRepository;

  public AppealOptionsService(
      DatabaseAppellantRepository databaseAppellantRepository,
      DatabaseAppealStatusRepository databaseAppealStatusRepository) {
    this.databaseAppellantRepository = databaseAppellantRepository;
    this.databaseAppealStatusRepository = databaseAppealStatusRepository;
  }

  public List<Appellant> getAppellantOptions() {
    return databaseAppellantRepository.findAll().stream()
        .map(appellantDTO -> new Appellant(appellantDTO.getId(), appellantDTO.getValue()))
        .toList();
  }

  public List<AppealStatus> getAppealStatusOptions() {
    return databaseAppealStatusRepository.findAll().stream()
        .map(status -> new AppealStatus(status.getId(), status.getValue()))
        .toList();
  }
}
