package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.UUID;

public interface PublicationReportRepository {

  List<PublicationReport> saveAll(List<PublicationReport> reports);

  List<PublicationReport> getAllByDocumentUnitUuid(UUID documentUnitUuid);
}
