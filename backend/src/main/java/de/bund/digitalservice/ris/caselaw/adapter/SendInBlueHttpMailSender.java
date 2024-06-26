package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitPublishException;
import de.bund.digitalservice.ris.caselaw.domain.HttpMailSender;
import de.bund.digitalservice.ris.caselaw.domain.MailAttachment;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import sendinblue.ApiClient;
import sendinblue.ApiException;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.TransactionalEmailsApi;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailAttachment;
import sibModel.SendSmtpEmailSender;
import sibModel.SendSmtpEmailTo;

public class SendInBlueHttpMailSender implements HttpMailSender {
  private final String apiKey;

  public SendInBlueHttpMailSender(String apiKey) {
    this.apiKey = apiKey;
  }

  @Override
  public void sendMail(
      String senderAddress,
      String receiverAddress,
      String subject,
      String content,
      List<MailAttachment> mailAttachments,
      String tag) {

    ApiClient defaultClient = Configuration.getDefaultApiClient();
    ApiKeyAuth apiKeyAuth = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
    apiKeyAuth.setApiKey(apiKey);

    TransactionalEmailsApi api = new TransactionalEmailsApi();
    SendSmtpEmailSender sender = new SendSmtpEmailSender();
    sender.setEmail(senderAddress);
    sender.setName("Neuris XML Exporter");
    List<SendSmtpEmailTo> toList = new ArrayList<>();
    SendSmtpEmailTo to = new SendSmtpEmailTo();
    to.setEmail(receiverAddress);
    toList.add(to);

    List<SendSmtpEmailAttachment> attachmentList = new ArrayList<>();
    for (MailAttachment a : mailAttachments) {
      SendSmtpEmailAttachment attachment = new SendSmtpEmailAttachment();
      attachment.setName(a.fileName());
      attachment.setContent(a.fileContent().getBytes(StandardCharsets.UTF_8));
      attachmentList.add(attachment);
    }

    List<String> tags = new ArrayList<>();
    tags.add(tag);
    SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
    sendSmtpEmail.setSender(sender);
    sendSmtpEmail.setTo(toList);
    sendSmtpEmail.setTextContent(content);
    sendSmtpEmail.setSubject(subject);
    sendSmtpEmail.setAttachment(attachmentList);
    sendSmtpEmail.setTags(tags);

    try {
      api.sendTransacEmail(sendSmtpEmail);
    } catch (ApiException e) {
      throw new DocumentUnitPublishException("Couldn't send email.", e);
    }
  }
}
