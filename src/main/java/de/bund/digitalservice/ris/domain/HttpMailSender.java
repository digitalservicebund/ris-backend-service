package de.bund.digitalservice.ris.domain;

public interface HttpMailSender {
  public void sendMail(
      String senderAddress,
      String receiverAddress,
      String subject,
      String content,
      String fileName);
}
