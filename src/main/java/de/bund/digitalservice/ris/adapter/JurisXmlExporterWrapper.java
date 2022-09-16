package de.bund.digitalservice.ris.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bund.digitalservice.ris.domain.DocumentUnit;
import de.bund.digitalservice.ris.domain.XmlExporter;
import de.bund.digitalservice.ris.domain.XmlResultObject;
import de.bund.digitalservice.ris.domain.export.juris.JurisXmlExporter;
import de.bund.digitalservice.ris.domain.export.juris.ResultObject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class JurisXmlExporterWrapper implements XmlExporter {
  private final JurisXmlExporter jurisXmlExporter;

  public JurisXmlExporterWrapper(ObjectMapper objectMapper) {
    this.jurisXmlExporter = new JurisXmlExporter(objectMapper);
  }

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
}
