package de.bund.digitalservice.ris.caselaw.adapter.caselawldml;

import jakarta.xml.bind.annotation.XmlElement;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class DocTitle {

  @XmlElement(name = "subFlow", namespace = CaseLawLdml.AKN_NS)
  private JaxbHtml content;

  public DocTitle(JaxbHtml content) {
    content.setName("titleWrapper");
    this.content = content;
  }
}
