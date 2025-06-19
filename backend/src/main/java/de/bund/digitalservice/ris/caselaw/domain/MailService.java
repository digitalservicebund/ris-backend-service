package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.UUID;

public interface MailService {
  HandoverMail handOver(Decision decision, String receiverAddress, String issuerAddress);

  HandoverMail handOver(
      LegalPeriodicalEdition edition, String receiverAddress, String issuerAddress);

  List<HandoverMail> getHandoverResult(UUID entityId, HandoverEntityType entityType);

  XmlTransformationResult getXmlPreview(Decision decision);

  List<XmlTransformationResult> getXmlPreview(LegalPeriodicalEdition edition);
}
