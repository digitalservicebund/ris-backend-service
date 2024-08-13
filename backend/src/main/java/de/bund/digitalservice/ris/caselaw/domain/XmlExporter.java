package de.bund.digitalservice.ris.caselaw.domain;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public interface XmlExporter {
  XmlTransformationResult transformToXml(DocumentationUnit documentationUnit)
      throws ParserConfigurationException, TransformerException;

  String generateEncryptedXMLString(DocumentationUnit documentationUnit)
      throws XmlExporterException;
}
