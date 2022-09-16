package de.bund.digitalservice.ris.adapter;

import de.bund.digitalservice.ris.domain.DocumentUnitBuilder;
import de.bund.digitalservice.ris.domain.DocumentUnitDTO;
import de.bund.digitalservice.ris.domain.DocumentUnitPublishException;
import de.bund.digitalservice.ris.domain.EmailPublishService;
import de.bund.digitalservice.ris.domain.HttpMailSender;
import de.bund.digitalservice.ris.domain.MailResponse;
import de.bund.digitalservice.ris.domain.XmlExporter;
import de.bund.digitalservice.ris.domain.XmlMail;
import de.bund.digitalservice.ris.domain.XmlMailRepository;
import de.bund.digitalservice.ris.domain.XmlMailResponse;
import de.bund.digitalservice.ris.domain.XmlResultObject;
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
  public Mono<MailResponse> publish(DocumentUnitDTO documentUnitDTO, String receiverAddress) {
    XmlResultObject xml;
    try {
      xml =
          xmlExporter.generateXml(
              DocumentUnitBuilder.newInstance().setDocUnitDTO(documentUnitDTO).build());
    } catch (ParserConfigurationException | TransformerException ex) {
      return Mono.error(new DocumentUnitPublishException("Couldn't generate xml.", ex));
    }

    return generateMailSubject(documentUnitDTO)
        .map(
            mailSubject ->
                generateXmlMail(documentUnitDTO.getId(), receiverAddress, mailSubject, xml))
        .doOnNext(this::generateAndSendMail)
        .flatMap(this::savePublishInformation)
        .doOnError(ex -> LOGGER.error("Error by generation of mail message", ex))
        .map(xmlMail -> new XmlMailResponse(documentUnitDTO.getUuid(), xmlMail));
  }

  @Override
  public Mono<MailResponse> getLastPublishedXml(Long documentUnitId, UUID documentUnitUuid) {
    return repository
        .findTopByDocumentUnitIdOrderByPublishDateDesc(documentUnitId)
        .map(xmlMail -> new XmlMailResponse(documentUnitUuid, xmlMail));
  }

  private Mono<String> generateMailSubject(DocumentUnitDTO documentUnit) {
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
      Long documentUnitId, String receiverAddress, String mailSubject, XmlResultObject xml) {

    String statusMessages = String.join("|", xml.statusMessages());
    if (xml.statusCode().equals("400")) {
      return new XmlMail(
          null, documentUnitId, null, null, null, xml.statusCode(), statusMessages, null, null);
    }

    return new XmlMail(
        null,
        documentUnitId,
        receiverAddress,
        mailSubject,
        xml.xml(),
        xml.statusCode(),
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
