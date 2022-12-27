package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class XmlMailResponse implements MailResponse {
  private UUID documentUnitUuid;
  private String receiverAddress;
  private String mailSubject;
  private String xml;
  private String statusCode;
  private List<String> statusMessages;
  private Instant publishDate;

  public XmlMailResponse(UUID documentUnitUuid, XmlMail xmlMail) {
    this.documentUnitUuid = documentUnitUuid;
    this.receiverAddress = xmlMail.receiverAddress();
    this.mailSubject = xmlMail.mailSubject();
    this.xml = xmlMail.xml();
    this.publishDate = xmlMail.publishDate();

    this.statusCode = xmlMail.statusCode();
    if (xmlMail.statusMessages() != null) {
      this.statusMessages = xmlMail.statusMessages();
    } else {
      this.statusMessages = null;
    }
  }
}
