package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;

public interface HttpMailSender {
  void sendMail(
      String senderAddress,
      String receiverAddress,
      String subject,
      String content,
      List<MailAttachment> mailAttachments,
      List<MailAttachmentImage> imageAttachments,
      String tag);
}
