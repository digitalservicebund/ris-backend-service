package de.bund.digitalservice.ris.caselaw.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.XmlExporter;
import de.bund.digitalservice.ris.caselaw.domain.XmlExporterException;
import de.bund.digitalservice.ris.caselaw.domain.XmlTransformationResult;
import de.bund.digitalservice.ris.domain.export.juris.JurisXmlExporter;
import de.bund.digitalservice.ris.domain.export.juris.ResultObject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

/** Wraps the Juris XML exporter to provide a common interface for XML export. */
public class JurisXmlExporterWrapper implements XmlExporter {
  private final JurisXmlExporter jurisXmlExporter;

  public JurisXmlExporterWrapper(ObjectMapper objectMapper) {
    this.jurisXmlExporter = new JurisXmlExporter(objectMapper);
  }

  /**
   * Generates juris XML from a documentation unit.
   *
   * @param documentUnit the documentation unit
   * @return the XML export result that may be unsuccessful and may contain error messages
   * @throws ParserConfigurationException if the XML generation fails due to a configuration error
   * @throws TransformerException if the XML generation fails due to failed transformation
   */
  @Override
  public XmlTransformationResult transformToXml(DocumentUnit documentUnit)
      throws ParserConfigurationException, TransformerException {
    ResultObject resultObject = jurisXmlExporter.generateXml(documentUnit);
    return new XmlTransformationResult(
        resultObject.xml(),
        resultObject.status().statusCode().equals("200"),
        resultObject.status().statusMessages(),
        resultObject.fileName(),
        resultObject.publishDate());
  }

  @Override
  public String generateEncryptedXMLString(DocumentUnit documentUnit) throws XmlExporterException {
    String resultObject;
    try {
      resultObject = jurisXmlExporter.generateEncryptedXMLString(documentUnit);
    } catch (Exception e) {
      throw new XmlExporterException("Failed to generate encrypted XML string", e);
    }
    return resultObject;
  }
}
