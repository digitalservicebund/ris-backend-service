package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.UUID;

public interface XmlPublicationRepository {
  XmlPublication save(XmlPublication xmlPublication);

  List<Publication> getPublicationsByDocumentUnitUuid(UUID documentUnitUuid);

  XmlPublication getLastXmlPublication(UUID documentUnitUuid);
}
