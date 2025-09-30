package de.bund.digitalservice.ris.caselaw.adapter.caselawldml;

import jakarta.xml.bind.annotation.XmlAnyElement;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlMixed;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement
public class JaxbHtml {

  @XmlAttribute(name = "name")
  private String name;

  @XmlAttribute(namespace = CaseLawLdml.RIS_NS, name = "domainTerm")
  private String domainTerm;

  @XmlAnyElement @XmlMixed private List<Object> html;

  public static JaxbHtml build(List<Object> html) {
    if (html == null || html.isEmpty() || html.stream().allMatch(Objects::isNull)) {
      return null;
    }

    return new JaxbHtml(html);
  }

  public JaxbHtml(List<Object> html) {
    this.html = html;
  }
}
