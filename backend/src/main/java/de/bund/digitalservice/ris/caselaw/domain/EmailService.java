package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.UUID;

public interface EmailService {
  XmlHandoverMail handOver(DocumentUnit documentUnit, String receiverAddress, String issuerAddress);

  List<XmlHandoverMail> getHandoverResult(UUID documentUnitUuid);

  XmlExportResult getXmlPreview(DocumentUnit documentUnit);
}
