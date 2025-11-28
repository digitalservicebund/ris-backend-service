package de.bund.digitalservice.ris.caselaw.adapter.caselawldml;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlValue;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class Ref {
  @XmlAttribute(namespace = CaseLawLdml.RIS_NS, name = "domainTerm")
  private String domainTerm;

  @XmlAttribute(name = "href")
  private String href;

  @XmlValue private String value;
}
