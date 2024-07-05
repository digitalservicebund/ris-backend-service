package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.UUID;

public interface EmailPublishService {
  XmlPublication publish(DocumentUnit documentUnit, String receiverAddress, String issuerAddress);

  List<Publication> getPublications(UUID documentUnitUuid);

  XmlResultObject getPublicationPreview(DocumentUnit documentUnit);
}
