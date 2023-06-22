package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.Map;

public interface HttpMailSender {
  void sendMail(
      String senderAddress,
      String receiverAddress,
      String subject,
      String content,
      List<Map.Entry<String, String>> attachments,
      String tag);
}
