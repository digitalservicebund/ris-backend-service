package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

@Service
@Slf4j
public class HandoverService {

  private final DocumentationUnitRepository repository;
  private final LegalPeriodicalEditionRepository editionRepository;
  private final HandoverReportRepository handoverReportRepository;
  private final MailService mailService;
  private final DeltaMigrationRepository deltaMigrationRepository;
  private final DocumentationUnitHistoryLogService historyLogService;

  @Value("${mail.exporter.recipientAddress:neuris@example.com}")
  private String recipientAddress;

  public HandoverService(
      DocumentationUnitRepository repository,
      MailService mailService,
      DeltaMigrationRepository migrationService,
      HandoverReportRepository handoverReportRepository,
      LegalPeriodicalEditionRepository editionRepository,
      DocumentationUnitHistoryLogService historyLogService) {

    this.repository = repository;
    this.mailService = mailService;
    this.deltaMigrationRepository = migrationService;
    this.handoverReportRepository = handoverReportRepository;
    this.editionRepository = editionRepository;
    this.historyLogService = historyLogService;
  }

  /**
   * Handover a documentation unit to the email service.
   *
   * @param documentationUnitId the UUID of the documentation unit
   * @param issuerAddress the email address of the issuer
   * @return the handover result
   * @throws DocumentationUnitNotExistsException if the documentation unit does not exist
   */
  public HandoverMail handoverDocumentationUnitAsMail(
      UUID documentationUnitId, String issuerAddress, @Nullable User user)
      throws DocumentationUnitNotExistsException {

    DocumentationUnit documentationUnit = repository.findByUuid(documentationUnitId);

    if (documentationUnit instanceof Decision decision) {
      String description = "Dokeinheit an jDV übergeben";
      historyLogService.saveHistoryLog(
          decision.uuid(), user, HistoryLogEventType.HANDOVER, description);
      return mailService.handOver(decision, recipientAddress, issuerAddress);
    } else {
      log.info("Documentable type not supported: {}", documentationUnit.getClass().getName());
      return null;
    }
  }

  /**
   * Handover a edition to the email service.
   *
   * @param editionId the UUID of the edition
   * @param issuerAddress the email address of the issuer
   * @return the handover result or null if the edition has no references
   * @throws IOException if the edition does not exist
   */
  public HandoverMail handoverEditionAsMail(UUID editionId, String issuerAddress)
      throws IOException {
    LegalPeriodicalEdition edition =
        editionRepository
            .findById(editionId)
            .orElseThrow(() -> new IOException("Edition not found: " + editionId));

    HandoverMail handoverMail = mailService.handOver(edition, recipientAddress, issuerAddress);
    if (!handoverMail.success()) {
      log.warn("Failed to send mail for edition {}", editionId);
    }
    return handoverMail;
  }

  /**
   * Create a preview juris xml for a documentation unit.
   *
   * @param documentUuid the UUID of the documentation unit
   * @return the export result, containing the juris xml and export metadata
   */
  public XmlTransformationResult createPreviewXml(UUID documentUuid)
      throws DocumentationUnitNotExistsException {

    DocumentationUnit documentationUnit = repository.findByUuid(documentUuid);

    if (documentationUnit instanceof Decision decision) {
      return mailService.getXmlPreview(decision);
    } else {
      log.info("Documentable type not supported: {}", documentationUnit.getClass().getName());
    }
    return null;
  }

  /**
   * Create a preview juris xml for a edition
   *
   * @param editionId the id of the edition
   * @return the export result, containing the juris xml and export metadata
   */
  public List<XmlTransformationResult> createEditionPreviewXml(UUID editionId) throws IOException {
    LegalPeriodicalEdition edition =
        editionRepository
            .findById(editionId)
            .orElseThrow(() -> new IOException("Edition not found: " + editionId));
    return mailService.getXmlPreview(edition);
  }

  /**
   * Get the event log for a entity (documentation unit or edition), containing jDV email handover
   * operations, handover reports (response emails from the jDV) and migrations/import events.
   *
   * @param entityId the UUID of the entity
   * @return the event log
   */
  public List<EventRecord> getEventLog(UUID entityId, HandoverEntityType entityType) {
    List<EventRecord> list =
        ListUtils.union(
            mailService.getHandoverResult(entityId, entityType),
            handoverReportRepository.getAllByEntityId(entityId));
    var migration = deltaMigrationRepository.getLatestMigration(entityId);
    if (migration != null) {
      list.add(
          migration.xml() != null
              ? migration.toBuilder().xml(prettifyXml(migration.xml())).build()
              : migration);
    }
    list.sort(Comparator.comparing(EventRecord::getDate).reversed());
    return list;
  }

  /**
   * Prettify an XML string.
   *
   * @param xml the XML string
   * @return the prettified XML string
   */
  public static String prettifyXml(String xml) {
    try {
      // Parse input string to DOM Document

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document =
          builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

      @SuppressWarnings("java:S2755")
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

      // Transform to string
      StringWriter writer = new StringWriter();
      writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      transformer.transform(new DOMSource(document), new StreamResult(writer));

      return writer.toString();
    } catch (Exception e) {
      throw new HandoverException("Failed to prettify XML", e);
    }
  }
}
