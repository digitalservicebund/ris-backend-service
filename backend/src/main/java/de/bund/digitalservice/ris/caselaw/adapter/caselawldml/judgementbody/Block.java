package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.judgementbody;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "block", namespace = CaseLawLdml.AKN_NS)
@XmlAccessorType(XmlAccessType.FIELD)
public class Block {
  @XmlAttribute(name = "name")
  private String name;

  @XmlElement(name = "opinion", namespace = CaseLawLdml.AKN_NS)
  private List<Opinion> opinions;
}
