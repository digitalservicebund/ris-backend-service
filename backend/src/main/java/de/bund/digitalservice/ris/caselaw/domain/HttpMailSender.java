package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;

public interface HttpMailSender {
  void sendMail(
      String senderAddress,
      String receiverAddress,
      String subject,
      String content,
      String fileName,
      UUID documentUnitUuid);
}
