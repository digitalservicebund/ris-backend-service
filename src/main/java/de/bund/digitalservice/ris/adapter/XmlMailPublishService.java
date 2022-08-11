package de.bund.digitalservice.ris.adapter;

import de.bund.digitalservice.ris.domain.DocUnit;
import de.bund.digitalservice.ris.domain.DocumentUnitPublishException;
import de.bund.digitalservice.ris.domain.DocumentUnitPublishService;
import de.bund.digitalservice.ris.domain.ExportObject;
import de.bund.digitalservice.ris.domain.XmlMail;
import de.bund.digitalservice.ris.domain.XmlMailRepository;
import de.bund.digitalservice.ris.domain.XmlMailResponse;
import de.bund.digitalservice.ris.domain.export.juris.JurisFormattedXML;
import de.bund.digitalservice.ris.domain.export.juris.JurisXmlExporter;
import java.io.IOException;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class XmlMailPublishService implements DocumentUnitPublishService {
  private static final Logger LOGGER = LoggerFactory.getLogger(XmlMailPublishService.class);

  private final JurisXmlExporter jurisXmlExporter;
  private final XmlMailRepository repository;
  private final JavaMailSender mailSender;
  private final String fromMailAddress;
  private String toMailAddressList;

  public XmlMailPublishService(
      JurisXmlExporter jurisXmlExporter,
      XmlMailRepository repository,
      JavaMailSender mailSender,
      @Value("${mail.from.address}") String fromMailAddress) {
    this.jurisXmlExporter = jurisXmlExporter;
    this.repository = repository;
    this.mailSender = mailSender;
    this.fromMailAddress = fromMailAddress;
    this.toMailAddressList = "tester@test.com";
  }

  public void setToMailAddressList(String toMailAddressList) {
    this.toMailAddressList = toMailAddressList;
  }

  @Override
  public Mono<? extends ExportObject> publish(DocUnit documentUnit) {
    JurisFormattedXML xml;
    try {
      xml = jurisXmlExporter.generateXml(documentUnit);
    } catch (ParserConfigurationException | TransformerException ex) {
      return Mono.error(new DocumentUnitPublishException("Couldn't generate xml.", ex));
    }

    return generateMailSubject(documentUnit)
        .flatMap(mailSubject -> savePublishInformation(documentUnit.getId(), mailSubject, xml))
        //        .doOnNext(this::generateMail)
        .map(xmlMail -> new XmlMailResponse(documentUnit.getUuid(), xmlMail))
        .doOnError(ex -> LOGGER.error("Error by generation of mail message", ex));
  }

  private Mono<String> generateMailSubject(DocUnit documentUnit) {
    if (documentUnit.getDocumentnumber() == null) {
      return Mono.error(
          new DocumentUnitPublishException("No document number has set in the document unit."));
    }

    String subject = "id=BGH";
    subject += " name=jDVNAME";
    subject += " da=R";
    subject += " df=X";
    subject += " dt=N";
    subject += " mod=A";
    subject += " vg=" + documentUnit.getDocumentnumber();
    return Mono.just(subject);
  }

  private void generateMail(XmlMail xmlMail) throws DocumentUnitPublishException {
    if (toMailAddressList == null) {
      throw new DocumentUnitPublishException("No receiver mail address is set");
    }

    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper;

    try {
      helper = new MimeMessageHelper(message, true);
      helper.setFrom(fromMailAddress);
    } catch (MessagingException ex) {
      throw new DocumentUnitPublishException("Sender mail address is not correct", ex);
    }

    try {
      helper.setTo(InternetAddress.parse(toMailAddressList));
    } catch (MessagingException ex) {
      throw new DocumentUnitPublishException("Receiver mail address is not correct", ex);
    }

    try {
      helper.setSubject(xmlMail.mailSubject());
    } catch (MessagingException ex) {
      throw new DocumentUnitPublishException("Subject is not correct", ex);
    }

    try {
      DataSource dataSource = new ByteArrayDataSource(xmlMail.xml(), "application/xml");
      helper.addAttachment("test.xml", dataSource);
    } catch (MessagingException | IOException ex) {
      throw new DocumentUnitPublishException("Couldn't add xml as attachment.");
    }
  }

  private Mono<XmlMail> savePublishInformation(
      Long documentUnitId, String mailSubject, JurisFormattedXML xml) {
    String statusMessages = String.join("|", xml.status().statusMessages());
    XmlMail xmlMail =
        new XmlMail(
            null,
            documentUnitId,
            mailSubject,
            xml.xml(),
            xml.status().statusCode(),
            statusMessages,
            xml.fileName(),
            xml.publishDate());
    return repository.save(xmlMail);
  }
}
