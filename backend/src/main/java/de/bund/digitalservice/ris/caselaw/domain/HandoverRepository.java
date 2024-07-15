package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.UUID;

public interface HandoverRepository {
  HandoverMail save(HandoverMail handoverMail);

  List<HandoverMail> getHandoversByDocumentUnitUuid(UUID documentUnitUuid);

  HandoverMail getLastXmlHandoverMail(UUID documentUnitUuid);
}
