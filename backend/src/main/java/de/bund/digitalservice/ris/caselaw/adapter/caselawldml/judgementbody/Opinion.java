package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.judgementbody;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "opinion", namespace = CaseLawLdml.AKN_NS)
public class Opinion {

  @XmlAttribute(name = "type")
  private String type;

  @XmlAttribute(name = "domainTerm", namespace = CaseLawLdml.RIS_NS)
  private String domainTerm;

  @XmlAttribute(name = "by")
  private String by;

  @XmlValue private String content;
}
