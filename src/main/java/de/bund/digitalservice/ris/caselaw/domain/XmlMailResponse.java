package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class XmlMailResponse implements MailResponse {
  private final UUID documentUnitUuid;
  private final String receiverAddress;
  private final String mailSubject;
  private final String xml;
  private final String statusCode;
  private final String[] statusMessages;
  private final Instant publishDate;

  public XmlMailResponse(UUID documentUnitUuid, XmlMail xmlMail) {
    this.documentUnitUuid = documentUnitUuid;
    this.receiverAddress = xmlMail.receiverAddress();
    this.mailSubject = xmlMail.mailSubject();
    this.xml = xmlMail.xml();
    this.publishDate = xmlMail.publishDate();

    this.statusCode = xmlMail.statusCode();
    if (xmlMail.statusMessages() != null) {
      this.statusMessages = xmlMail.statusMessages().split("\\|");
    } else {
      this.statusMessages = null;
    }
  }
}
