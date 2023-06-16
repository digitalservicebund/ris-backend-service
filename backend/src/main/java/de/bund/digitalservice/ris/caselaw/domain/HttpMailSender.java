package de.bund.digitalservice.ris.caselaw.domain;

public interface HttpMailSender {
  void sendMail(
      String senderAddress,
      String receiverAddress,
      String subject,
      String content,
      String fileName,
      String fileContent,
      String tag);
}
