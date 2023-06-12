package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitPublishException;
import de.bund.digitalservice.ris.caselaw.domain.HttpMailSender;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  private static final Logger LOGGER = LoggerFactory.getLogger(SendInBlueHttpMailSender.class);

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
      String fileName,
      String fileContent,
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

    SendSmtpEmailAttachment attachment = new SendSmtpEmailAttachment();
    attachment.setName(fileName);
    attachment.setContent(fileContent.getBytes(StandardCharsets.UTF_8));
    List<SendSmtpEmailAttachment> attachmentList = new ArrayList<>();
    attachmentList.add(attachment);

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
