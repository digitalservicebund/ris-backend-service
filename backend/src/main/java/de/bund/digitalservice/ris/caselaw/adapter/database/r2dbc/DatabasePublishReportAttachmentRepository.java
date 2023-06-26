package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import java.util.UUID;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface DatabasePublishReportAttachmentRepository
    extends R2dbcRepository<PublishReportAttachment, UUID> {}
