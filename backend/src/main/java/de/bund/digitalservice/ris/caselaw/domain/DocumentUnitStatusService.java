package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitStatusDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitStatusRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class DocumentUnitStatusService {

  private final DocumentUnitStatusRepository repository;

  public DocumentUnitStatusService(DocumentUnitStatusRepository repository) {
    this.repository = repository;
  }

  public Mono<String> setInitialStatus(UUID documentUnitUuid, Instant creationTimestamp) {
    return repository
        .save(
            DocumentUnitStatusDTO.builder()
                .id(UUID.randomUUID())
                .documentUnitId(documentUnitUuid)
                .createdAt(creationTimestamp)
                .status("unpublished")
                .build())
        .map(DocumentUnitStatusDTO::getStatus);
  }
}
