package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public interface XmlExporter {
  XmlTransformationResult transformToXml(Decision decision, boolean prettyPrint)
      throws ParserConfigurationException, TransformerException;

  String generateEncryptedXMLString(Decision decision) throws XmlExporterException;

  List<XmlTransformationResult> transformToXml(LegalPeriodicalEdition edition)
      throws ParserConfigurationException, TransformerException;
}
