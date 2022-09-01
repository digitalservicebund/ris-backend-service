package de.bund.digitalservice.ris.adapter;

import de.bund.digitalservice.ris.domain.DocumentUnitPublishException;
import de.bund.digitalservice.ris.domain.HttpMailSender;
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
      String fileName) {

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
    SendSmtpEmailAttachment attachment = new SendSmtpEmailAttachment();
    attachment.setName(fileName);
    attachment.setContent(content.getBytes(StandardCharsets.UTF_8));
    List<SendSmtpEmailAttachment> attachmentList = new ArrayList<>();
    attachmentList.add(attachment);
    SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
    sendSmtpEmail.setSender(sender);
    sendSmtpEmail.setTo(toList);
    sendSmtpEmail.setTextContent("neuris");
    sendSmtpEmail.setSubject(subject);
    sendSmtpEmail.setAttachment(attachmentList);

    try {
      api.sendTransacEmail(sendSmtpEmail);
    } catch (ApiException e) {
      throw new DocumentUnitPublishException("Couldn't send email.", e);
    }
  }
}
