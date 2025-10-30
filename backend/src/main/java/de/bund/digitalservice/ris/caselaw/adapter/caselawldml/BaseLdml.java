package de.bund.digitalservice.ris.caselaw.adapter.caselawldml;

import jakarta.xml.bind.annotation.XmlAttribute;
import lombok.Setter;

@Setter
public class BaseLdml {
  @XmlAttribute(namespace = CaseLawLdml.AKN_NS, name = "eId")
  private String eId;

  @XmlAttribute(name = "domainTerm", namespace = CaseLawLdml.RIS_NS)
  String domainTerm;
}
