package de.bund.digitalservice.ris.caselaw.domain;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

/** Interface to generate xml for a document unit */
public interface XmlExporter {
  XmlResultObject generateXml(DocumentUnit documentUnit)
      throws ParserConfigurationException, TransformerException;

  String generateEncryptedXMLString(DocumentUnit documentUnit) throws Exception;
}
