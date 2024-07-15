package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.UUID;

public interface XmlHandoverRepository {
  XmlHandoverMail save(XmlHandoverMail xmlHandoverMail);

  List<XmlHandoverMail> getHandoversByDocumentUnitUuid(UUID documentUnitUuid);

  XmlHandoverMail getLastXmlHandoverMail(UUID documentUnitUuid);
}
