package de.bund.digitalservice.ris.caselaw.domain.formex;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;

public class XmlTransformerFactory {

  private XmlTransformerFactory() {}

  public static Transformer createTransformer() throws TransformerConfigurationException {
    final javax.xml.transform.TransformerFactory factory =
        javax.xml.transform.TransformerFactory.newInstance();
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
    final Transformer transformer = factory.newTransformer();
    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    transformer.setOutputProperty(OutputKeys.INDENT, "no");
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

    return transformer;
  }
}
