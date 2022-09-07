package de.bund.digitalservice.ris.adapter;

import de.bund.digitalservice.ris.domain.DocUnit;
import de.bund.digitalservice.ris.domain.DocumentUnitPublishException;
import de.bund.digitalservice.ris.domain.EmailPublishService;
import de.bund.digitalservice.ris.domain.HttpMailSender;
import de.bund.digitalservice.ris.domain.MailResponse;
import de.bund.digitalservice.ris.domain.XmlMail;
import de.bund.digitalservice.ris.domain.XmlMailRepository;
import de.bund.digitalservice.ris.domain.XmlMailResponse;
import de.bund.digitalservice.ris.domain.export.juris.JurisXmlExporter;
import de.bund.digitalservice.ris.domain.export.juris.ResultObject;
import java.util.UUID;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class XmlEMailPublishService implements EmailPublishService {
  private static final Logger LOGGER = LoggerFactory.getLogger(XmlEMailPublishService.class);

  private final JurisXmlExporter jurisXmlExporter;

  private final HttpMailSender mailSender;

  private final XmlMailRepository repository;

  @Value("${mail.exporter.senderAddress:test}")
  private String senderAddress;

  public XmlEMailPublishService(
      JurisXmlExporter jurisXmlExporter, HttpMailSender mailSender, XmlMailRepository repository) {
    this.jurisXmlExporter = jurisXmlExporter;
    this.mailSender = mailSender;
    this.repository = repository;
  }

  @Override
  public Mono<MailResponse> publish(DocUnit documentUnit, String receiverAddress) {
    ResultObject xml;
    try {
      xml = jurisXmlExporter.generateXml(documentUnit);
    } catch (ParserConfigurationException | TransformerException ex) {
      return Mono.error(new DocumentUnitPublishException("Couldn't generate xml.", ex));
    }

    return generateMailSubject(documentUnit)
        .map(
            mailSubject -> generateXmlMail(documentUnit.getId(), receiverAddress, mailSubject, xml))
        .doOnNext(this::generateAndSendMail)
        .flatMap(this::savePublishInformation)
        .doOnError(ex -> LOGGER.error("Error by generation of mail message", ex))
        .map(xmlMail -> new XmlMailResponse(documentUnit.getUuid(), xmlMail));
  }

  @Override
  public Mono<MailResponse> getLastPublishedXml(Long documentUnitId, UUID documentUnitUuid) {
    return repository
        .findTopByDocumentUnitIdOrderByPublishDateDesc(documentUnitId)
        .map(xmlMail -> new XmlMailResponse(documentUnitUuid, xmlMail));
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
    subject += " vg=Testvorgang";
    return Mono.just(subject);
  }

  private void generateAndSendMail(XmlMail xmlMail) throws DocumentUnitPublishException {
    if (xmlMail == null) {
      throw new DocumentUnitPublishException("No xml mail is set");
    }

    if (xmlMail.statusCode().equals("400")) {
      return;
    }

    if (xmlMail.receiverAddress() == null) {
      throw new DocumentUnitPublishException("No receiver mail address is set");
    }

    mailSender.sendMail(
        senderAddress,
        xmlMail.receiverAddress(),
        xmlMail.mailSubject(),
        xmlMail.xml(),
        xmlMail.fileName());
  }

  private XmlMail generateXmlMail(
      Long documentUnitId, String receiverAddress, String mailSubject, ResultObject xml) {

    String statusMessages = String.join("|", xml.status().statusMessages());
    if (xml.status().statusCode().equals("400")) {
      return new XmlMail(
          null,
          documentUnitId,
          null,
          null,
          null,
          xml.status().statusCode(),
          statusMessages,
          null,
          null);
    }

    return new XmlMail(
        null,
        documentUnitId,
        receiverAddress,
        mailSubject,
        xml.xml(),
        xml.status().statusCode(),
        statusMessages,
        xml.fileName(),
        xml.publishDate());
  }

  private Mono<XmlMail> savePublishInformation(XmlMail xmlMail) {
    if (xmlMail.statusCode().equals("400")) {
      return Mono.just(xmlMail);
    }

    return repository.save(xmlMail);
  }
}
