package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.UUID;

public interface MailService {
  HandoverMail handOver(DocumentUnit documentUnit, String receiverAddress, String issuerAddress);

  List<HandoverMail> getHandoverResult(UUID documentUnitUuid);

  XmlTransformationResult getXmlPreview(DocumentUnit documentUnit);
}
