package de.bund.digitalservice.ris.caselaw.adapter.caselawldml;

import de.bund.digitalservice.ris.caselaw.adapter.XmlUtils;
import jakarta.xml.bind.annotation.XmlAnyElement;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlMixed;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;
import javax.xml.transform.Templates;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.NodeList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement
public class JaxbHtml {

  private static final Logger logger = LogManager.getLogger(JaxbHtml.class);

  private static final Templates removeNameSpacesXslt =
      XmlUtils.getTemplates("caselawhandover/shared/removeNameSpaces.xslt");

  @XmlAttribute(name = "name")
  private String name;

  @XmlAnyElement @XmlMixed private List<Object> html;

  public static JaxbHtml build(String html) {
    // Lombok build() can't return null when one input is null
    if (StringUtils.isBlank(html)) {
      return null;
    }
    return new JaxbHtml(html);
  }

  public JaxbHtml(String html) {
    NodeList nodeList = XmlUtils.htmlStringToNodeList(html);
    this.html = XmlUtils.toList(nodeList).stream().map(e -> (Object) e).toList();
  }

  public String toHtmlString() {
    return XmlUtils.applyXsltToNodeList(removeNameSpacesXslt, XmlUtils.jaxbParseToNodeList(html));
  }
}
