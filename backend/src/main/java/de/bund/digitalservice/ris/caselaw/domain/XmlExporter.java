package de.bund.digitalservice.ris.caselaw.domain;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public interface XmlExporter {
  XmlExportResult generateXml(DocumentUnit documentUnit)
      throws ParserConfigurationException, TransformerException;

  String generateEncryptedXMLString(DocumentUnit documentUnit) throws XmlExporterException;
}
