package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import reactor.core.publisher.Flux;

public interface PublishReportAttachmentRepository {

  Flux<PublishReportAttachment> saveAll(List<PublishReportAttachment> report);
}
