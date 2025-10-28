package de.bund.digitalservice.ris.caselaw.adapter.transformer.ldml;

import de.bund.digitalservice.ris.caselaw.adapter.XmlUtilService;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mapping.MappingException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@Slf4j
public class HtmlTransformer {

  private final DocumentBuilderFactory documentBuilderFactory;

  public HtmlTransformer(DocumentBuilderFactory documentBuilderFactory) {
    this.documentBuilderFactory = documentBuilderFactory;
  }

  public List<Object> htmlStringToObjectList(String html) {
    if (StringUtils.isBlank(html)) {
      return Collections.emptyList();
    }

    html = html.replace("&nbsp;", "&#160;");

    /* Pre-process:
    HTML allows tags that are not closed. However, XML does not. That's why we do
    this string-manipulation based workaround of closing the img and br tag.
    Colgroup are style elements for columns in table and are not needed */
    html =
        html.replaceAll("(<img\\b[^>]*?)(?<!/)>", "$1/>")
            .replaceAll("<\\s*br\\s*>(?!\\s*<\\s*/\\s*br\\s*>)", "<br/>")
            .replaceAll("<colgroup[^>]*>.*?</colgroup>", "");

    /* Pre-process:
    Remove all ignore-once tags as they mark locally ignore text check issues that are irrelevant for the portal
    */
    html = html.replaceAll("<(/?)ignore-once>", "");

    try {
      String wrapped = "<wrapper>" + html + "</wrapper>";

      DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
      Document doc = builder.parse(new InputSource(new StringReader(wrapped)));

      NodeList childNodes = doc.getDocumentElement().getChildNodes();

      return XmlUtilService.toList(childNodes).stream().map(e -> (Object) e).toList();
    } catch (ParserConfigurationException | IOException | SAXException e) {
      log.error("Xml transformation error.", e);
      throw new MappingException(e.getMessage());
    }
  }
}
