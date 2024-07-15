package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.UUID;

public interface HandoverReportRepository {

  List<HandoverReport> saveAll(List<HandoverReport> reports);

  List<HandoverReport> getAllByDocumentUnitUuid(UUID documentUnitUuid);
}
