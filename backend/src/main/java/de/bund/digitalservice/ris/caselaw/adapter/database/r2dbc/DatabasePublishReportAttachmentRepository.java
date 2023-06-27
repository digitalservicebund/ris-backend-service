package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import java.util.UUID;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface DatabasePublishReportAttachmentRepository
    extends R2dbcRepository<PublishReportAttachmentDTO, UUID> {
  Flux<PublishReportAttachmentDTO> findAllByDocumentUnitId(UUID documentUnitId);
}
