package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.CoreData;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitHandoverException;
import de.bund.digitalservice.ris.caselaw.domain.HandoverMail;
import de.bund.digitalservice.ris.caselaw.domain.HandoverRepository;
import de.bund.digitalservice.ris.caselaw.domain.HttpMailSender;
import de.bund.digitalservice.ris.caselaw.domain.MailAttachment;
import de.bund.digitalservice.ris.caselaw.domain.MailService;
import de.bund.digitalservice.ris.caselaw.domain.XmlExporter;
import de.bund.digitalservice.ris.caselaw.domain.XmlTransformationResult;
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

/** Implementation of the {@link MailService} interface that sends juris-XML files via email. */
@Service
public class HandoverMailService implements MailService {
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private final XmlExporter xmlExporter;

  private final HttpMailSender mailSender;

  private final HandoverRepository repository;

  private final Environment env;

  @Value("${mail.exporter.senderAddress:export.test@neuris}")
  private String senderAddress;

  @Value("${mail.exporter.jurisUsername:invalid-user}")
  private String jurisUsername;

  public HandoverMailService(
      XmlExporter xmlExporter,
      HttpMailSender mailSender,
      HandoverRepository repository,
      Environment env) {
    this.xmlExporter = xmlExporter;
    this.mailSender = mailSender;
    this.repository = repository;
    this.env = env;
  }

  /**
   * Hands over a documentation unit as XML to jDV via email.
   *
   * @param documentUnit the documentation unit to hand over
   * @param receiverAddress the email address of the receiver
   * @param issuerAddress the email address of the issuer
   * @return the result of the handover
   * @throws DocumentationUnitHandoverException if the XML export fails
   */
  @Override
  public HandoverMail handOver(
      DocumentUnit documentUnit, String receiverAddress, String issuerAddress) {
    XmlTransformationResult xml;
    try {
      xml = xmlExporter.transformToXml(getTestDocumentUnit(documentUnit));
    } catch (ParserConfigurationException | TransformerException ex) {
      throw new DocumentationUnitHandoverException("Couldn't generate xml.", ex);
    }

    String mailSubject = generateMailSubject(documentUnit);

    HandoverMail handoverMail =
        generateXmlHandoverMail(
            documentUnit.uuid(), receiverAddress, mailSubject, xml, issuerAddress);
    generateAndSendMail(handoverMail);
    if (!handoverMail.success()) {
      return handoverMail;
    }
    return repository.save(handoverMail);
  }

  /**
   * Returns the results of performed handover operations for a documentation unit.
   *
   * @param documentUnitUuid the UUID of the documentation unit
   * @return a list of results of all handover operations for the documentation unit
   */
  @Override
  public List<HandoverMail> getHandoverResult(UUID documentUnitUuid) {
    return repository.getHandoversByDocumentUnitUuid(documentUnitUuid);
  }

  /**
   * Generates a preview of the XML that would be sent via email.
   *
   * @param documentUnit the documentation unit
   * @return the XML export result, containing the XML and possibly errors
   * @throws DocumentationUnitHandoverException if the XML export fails
   */
  @Override
  public XmlTransformationResult getXmlPreview(DocumentUnit documentUnit) {
    try {
      return xmlExporter.transformToXml(documentUnit);
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

  private void generateAndSendMail(HandoverMail handoverMail)
      throws DocumentationUnitHandoverException {
    if (handoverMail == null) {
      throw new DocumentationUnitHandoverException("No xml mail is set");
    }

    if (!handoverMail.success()) {
      return;
    }

    if (handoverMail.receiverAddress() == null) {
      throw new DocumentationUnitHandoverException("No receiver mail address is set");
    }

    mailSender.sendMail(
        senderAddress,
        handoverMail.receiverAddress(),
        handoverMail.mailSubject(),
        "neuris",
        Collections.singletonList(
            MailAttachment.builder()
                .fileName(handoverMail.fileName())
                .fileContent(handoverMail.xml())
                .build()),
        handoverMail.documentUnitUuid().toString());
  }

  private HandoverMail generateXmlHandoverMail(
      UUID documentUnitUuid,
      String receiverAddress,
      String mailSubject,
      XmlTransformationResult xml,
      String issuerAddress) {
    var xmlHandoverMailBuilder =
        HandoverMail.builder()
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
