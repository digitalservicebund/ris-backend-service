package de.bund.digitalservice.ris.domain;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public interface XmlExporter {
  XmlResultObject generateXml(DocumentUnit documentUnit)
      throws ParserConfigurationException, TransformerException;
}
