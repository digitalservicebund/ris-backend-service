package de.bund.digitalservice.ris.caselaw.adapter.caselawldml;

import jakarta.xml.bind.annotation.XmlAttribute;
import lombok.Setter;

@Setter
public class BaseLdml {
  @XmlAttribute(name = "domainTerm", namespace = CaseLawLdml.RIS_NS)
  String domainTerm;
}
