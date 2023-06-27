package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface PublishReportAttachmentRepository {

  Flux<PublicationReport> saveAll(List<PublicationReport> report);

  Flux<PublicationReport> getAllForDocumentUnit(UUID uuid);
}
