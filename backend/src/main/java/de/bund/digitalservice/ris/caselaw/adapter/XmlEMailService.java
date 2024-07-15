package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitHandoverException;
import de.bund.digitalservice.ris.caselaw.domain.EmailService;
import de.bund.digitalservice.ris.caselaw.domain.HttpMailSender;
import de.bund.digitalservice.ris.caselaw.domain.MailAttachment;
import de.bund.digitalservice.ris.caselaw.domain.XmlExportResult;
import de.bund.digitalservice.ris.caselaw.domain.XmlExporter;
import de.bund.digitalservice.ris.caselaw.domain.XmlHandoverMail;
import de.bund.digitalservice.ris.caselaw.domain.XmlHandoverRepository;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class XmlEMailService implements EmailService {
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private final XmlExporter xmlExporter;

  private final HttpMailSender mailSender;

  private final XmlHandoverRepository repository;

  private final Environment env;

  @Value("${mail.exporter.senderAddress:export.test@neuris}")
  private String senderAddress;

  @Value("${mail.exporter.jurisUsername:invalid-user}")
  private String jurisUsername;

  public XmlEMailService(
      XmlExporter xmlExporter,
      HttpMailSender mailSender,
      XmlHandoverRepository repository,
      Environment env) {
    this.xmlExporter = xmlExporter;
    this.mailSender = mailSender;
    this.repository = repository;
    this.env = env;
  }

  @Override
  public XmlHandoverMail handOver(
      DocumentUnit documentUnit, String receiverAddress, String issuerAddress) {
    XmlExportResult xml;
    try {
      xml = xmlExporter.generateXml(getTestDocumentUnit(documentUnit));
    } catch (ParserConfigurationException | TransformerException ex) {
      throw new DocumentationUnitHandoverException("Couldn't generate xml.", ex);
    }

    String mailSubject = generateMailSubject(documentUnit);

    XmlHandoverMail xmlHandoverMail =
        generateXmlHandoverMail(
            documentUnit.uuid(), receiverAddress, mailSubject, xml, issuerAddress);
    generateAndSendMail(xmlHandoverMail);
    if (!xmlHandoverMail.success()) {
      return xmlHandoverMail;
    }
    return repository.save(xmlHandoverMail);
  }

  @Override
  public List<XmlHandoverMail> getHandoverResult(UUID documentUnitUuid) {
    return repository.getHandoversByDocumentUnitUuid(documentUnitUuid);
  }

  @Override
  public XmlExportResult getXmlPreview(DocumentUnit documentUnit) {
    try {
      return xmlExporter.generateXml(documentUnit);
    } catch (ParserConfigurationException | TransformerException ex) {
      throw new DocumentationUnitHandoverException("Couldn't generate xml.", ex);
    }
  }

  private String generateMailSubject(DocumentUnit documentUnit) {
    if (documentUnit.documentNumber() == null) {
      throw new DocumentationUnitHandoverException(
          "No document number has set in the document unit.");
    }

    String deliveryDate =
        LocalDate.now(Clock.system(ZoneId.of("Europe/Berlin"))).format(DATE_FORMATTER);

    String subject = "id=juris";
    subject += " name=" + jurisUsername;
    subject += " da=R";
    subject += " df=X";
    subject += " dt=N";
    subject += " mod=T";
    subject += " ld=" + deliveryDate;
    subject += " vg=";
    subject += documentUnit.documentNumber();

    return subject;
  }

  private void generateAndSendMail(XmlHandoverMail xmlHandoverMail)
      throws DocumentationUnitHandoverException {
    if (xmlHandoverMail == null) {
      throw new DocumentationUnitHandoverException("No xml mail is set");
    }

    if (!xmlHandoverMail.success()) {
      return;
    }

    if (xmlHandoverMail.receiverAddress() == null) {
      throw new DocumentationUnitHandoverException("No receiver mail address is set");
    }

    mailSender.sendMail(
        senderAddress,
        xmlHandoverMail.receiverAddress(),
        xmlHandoverMail.mailSubject(),
        "neuris",
        Collections.singletonList(
            MailAttachment.builder()
                .fileName(xmlHandoverMail.fileName())
                .fileContent(xmlHandoverMail.xml())
                .build()),
        xmlHandoverMail.documentUnitUuid().toString());
  }

  private XmlHandoverMail generateXmlHandoverMail(
      UUID documentUnitUuid,
      String receiverAddress,
      String mailSubject,
      XmlExportResult xml,
      String issuerAddress) {
    var xmlHandoverMailBuilder =
        XmlHandoverMail.builder()
            .documentUnitUuid(documentUnitUuid)
            .success(xml.success())
            .statusMessages(xml.statusMessages());

    if (!xmlHandoverMailBuilder.build().isSuccess()) {
      return xmlHandoverMailBuilder.build();
    }

    return xmlHandoverMailBuilder
        .receiverAddress(receiverAddress)
        .mailSubject(mailSubject)
        .xml(xml.xml())
        .fileName(xml.fileName())
        .handoverDate(xml.creationDate())
        .issuerAddress(issuerAddress)
        .build();
  }

  private DocumentUnit getTestDocumentUnit(DocumentUnit documentUnit) {
    if (env.matchesProfiles("production")) {
      return documentUnit.toBuilder()
          .coreData(
              Optional.ofNullable(documentUnit.coreData())
                  .orElseGet(() -> CoreData.builder().build()))
          .build();
    }
    return documentUnit.toBuilder()
        .coreData(
            Optional.ofNullable(documentUnit.coreData())
                .map(
                    coreData ->
                        coreData.toBuilder()
                            .court(
                                Court.builder()
                                    .type("VGH")
                                    .location("Mannheim")
                                    .label("VGH Mannheim")
                                    .build())
                            .fileNumbers(
                                Stream.concat(
                                        Stream.of("TEST"),
                                        documentUnit.coreData().fileNumbers().stream())
                                    .toList())
                            .build())
                .orElseGet(
                    () ->
                        CoreData.builder()
                            .court(
                                Court.builder()
                                    .type("VGH")
                                    .location("Mannheim")
                                    .label("VGH Mannheim")
                                    .build())
                            .fileNumbers(Collections.singletonList("TEST"))
                            .build()))
        .build();
  }
}
