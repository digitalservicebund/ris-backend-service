package de.bund.digitalservice.ris.adapter;

import de.bund.digitalservice.ris.domain.DocUnit;
import de.bund.digitalservice.ris.domain.XmlExporter;
import de.bund.digitalservice.ris.domain.XmlResultObject;
import java.time.Instant;
import java.util.Collections;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class MockXmlExporter implements XmlExporter {

  @Override
  public XmlResultObject generateXml(DocUnit documentUnit)
      throws ParserConfigurationException, TransformerException {
    return new XmlResultObject("xml", "200", Collections.emptyList(), "test.xml", Instant.now());
  }
}
