package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitBuilder;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitPublishException;
import de.bund.digitalservice.ris.caselaw.domain.EmailPublishService;
import de.bund.digitalservice.ris.caselaw.domain.HttpMailSender;
import de.bund.digitalservice.ris.caselaw.domain.MailResponse;
import de.bund.digitalservice.ris.caselaw.domain.XmlExporter;
import de.bund.digitalservice.ris.caselaw.domain.XmlMail;
import de.bund.digitalservice.ris.caselaw.domain.XmlMailRepository;
import de.bund.digitalservice.ris.caselaw.domain.XmlMailResponse;
import de.bund.digitalservice.ris.caselaw.domain.XmlResultObject;
import java.util.UUID;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/** Implementation of the email publish service to publish the document unit as xml. */
@Service
public class XmlEMailPublishService implements EmailPublishService {
  private static final Logger LOGGER = LoggerFactory.getLogger(XmlEMailPublishService.class);

  private final XmlExporter xmlExporter;

  private final HttpMailSender mailSender;

  private final XmlMailRepository repository;

  @Value("${mail.exporter.senderAddress:test}")
  private String senderAddress;

  /**
   * Constructor to get the bean singletons.
   *
   * @param xmlExporter get the xml exporter to convert the document unit to xml
   * @param mailSender get the service to send the generated email
   * @param repository get the repository in which the published xml information and the generated
   *     mail subject are saved
   */
  public XmlEMailPublishService(
      XmlExporter xmlExporter, HttpMailSender mailSender, XmlMailRepository repository) {
    this.xmlExporter = xmlExporter;
    this.mailSender = mailSender;
    this.repository = repository;
  }

  /**
   * Publish the xml converted document unit via email.
   *
   * @param documentUnitDTO document unit which should be published
   * @param receiverAddress email address of the receiver of the xml version of the document unit.
   * @return the mail response object which was saved to the repository. It contains mail subject
   *     and xml data.
   */
  @Override
  public Mono<MailResponse> publish(DocumentUnitDTO documentUnitDTO, String receiverAddress) {
    XmlResultObject xml;
    try {
      xml =
          xmlExporter.generateXml(
              DocumentUnitBuilder.newInstance().setDocumentUnitDTO(documentUnitDTO).build());
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

  /**
   * Get the xml response object of the last published xml for a document unit id. It contains the
   * mail subject and the xml data.
   *
   * @param documentUnitId id of the document unit
   * @param documentUnitUuid uuid of the document unit
   * @return xml response object of document unit which was published last
   */
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
