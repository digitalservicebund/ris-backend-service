package de.bund.digitalservice.ris.caselaw.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.XmlExporter;
import de.bund.digitalservice.ris.caselaw.domain.XmlResultObject;
import de.bund.digitalservice.ris.domain.export.juris.JurisXmlExporter;
import de.bund.digitalservice.ris.domain.export.juris.ResultObject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

/**
 * Implementation of the xml exporter interface under the use of the juris xml exporter.
 *
 * <p>The juris xml exporter is a library which came from a private repository and can used by
 * authorized users. All other user must use the {@link MockXmlExporter}.
 */
public class JurisXmlExporterWrapper implements XmlExporter {
  private final JurisXmlExporter jurisXmlExporter;

  /**
   * Generate a juris xml exporter with internal configured object mapper.
   *
   * @param objectMapper internal object mapper singleton
   */
  public JurisXmlExporterWrapper(ObjectMapper objectMapper) {
    this.jurisXmlExporter = new JurisXmlExporter(objectMapper);
  }

  /**
   * Generate XML with the juris xml exporter.
   *
   * @param documentUnit document unit which should converted into xml.
   * @return a result object which contains the xml and information about the transformation {@see
   *     XmlResultObject}
   * @throws ParserConfigurationException
   * @throws TransformerException
   */
  @Override
  public XmlResultObject generateXml(DocumentUnit documentUnit)
      throws ParserConfigurationException, TransformerException {
    ResultObject resultObject = jurisXmlExporter.generateXml(documentUnit);
    return new XmlResultObject(
        resultObject.xml(),
        resultObject.status().statusCode(),
        resultObject.status().statusMessages(),
        resultObject.fileName(),
        resultObject.publishDate());
  }

  @Override
  public String generateEncryptedXMLString(DocumentUnit documentUnit) throws Exception {
    String resultObject = jurisXmlExporter.generateEncryptedXMLString(documentUnit);
    return resultObject;
  }
}
