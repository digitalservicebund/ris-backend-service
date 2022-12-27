package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitPublishException;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import de.bund.digitalservice.ris.caselaw.domain.HttpMailSender;
import de.bund.digitalservice.ris.caselaw.domain.MailResponse;
import de.bund.digitalservice.ris.caselaw.domain.XmlExporter;
import de.bund.digitalservice.ris.caselaw.domain.XmlMail;
import de.bund.digitalservice.ris.caselaw.domain.XmlMailRepository;
import de.bund.digitalservice.ris.caselaw.domain.XmlMailResponse;
import de.bund.digitalservice.ris.caselaw.domain.XmlResultObject;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private final XmlExporter xmlExporter;

  private final HttpMailSender mailSender;

  private final XmlMailRepository repository;

  @Value("${mail.exporter.senderAddress:test}")
  private String senderAddress;

  public XmlEMailPublishService(
      XmlExporter xmlExporter, HttpMailSender mailSender, XmlMailRepository repository) {

    this.xmlExporter = xmlExporter;
    this.mailSender = mailSender;
    this.repository = repository;
  }

  @Override
  public Mono<MailResponse> publish(DocumentUnit documentUnit, String receiverAddress) {
    XmlResultObject xml;
    try {
      xml = xmlExporter.generateXml(documentUnit);
    } catch (ParserConfigurationException | TransformerException ex) {
      return Mono.error(new DocumentUnitPublishException("Couldn't generate xml.", ex));
    }

    return generateMailSubject(documentUnit)
        .map(mailSubject -> generateXmlMail(documentUnit.uuid(), receiverAddress, mailSubject, xml))
        .doOnNext(this::generateAndSendMail)
        .flatMap(this::savePublishInformation)
        .doOnError(ex -> LOGGER.error("Error by generation of mail message", ex))
        .map(xmlMail -> new XmlMailResponse(documentUnit.uuid(), xmlMail));
  }

  @Override
  public Mono<MailResponse> getLastPublishedXml(UUID documentUnitUuid) {
    return repository.getLastPublishedXml(documentUnitUuid);
  }

  private Mono<String> generateMailSubject(DocumentUnit documentUnit) {
    if (documentUnit.documentNumber() == null) {
      return Mono.error(
          new DocumentUnitPublishException("No document number has set in the document unit."));
    }

    String deliveryDate =
        LocalDate.now(Clock.system(ZoneId.of("Europe/Berlin"))).format(DATE_FORMATTER);

    String subject = "id=juris";
    subject += " name=NeuRIS";
    subject += " da=R";
    subject += " df=X";
    subject += " dt=N";
    subject += " mod=T";
    subject += " ld=" + deliveryDate;
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
      UUID documentUnitUuid, String receiverAddress, String mailSubject, XmlResultObject xml) {

    if (xml.statusCode().equals("400")) {
      return new XmlMail(
          documentUnitUuid, null, null, null, xml.statusCode(), xml.statusMessages(), null, null);
    }

    return new XmlMail(
        documentUnitUuid,
        receiverAddress,
        mailSubject,
        xml.xml(),
        xml.statusCode(),
        xml.statusMessages(),
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
