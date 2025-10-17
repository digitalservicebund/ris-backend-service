package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.judgementbody;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.BaseLdml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyElement;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "opinion", namespace = CaseLawLdml.AKN_NS)
public class Opinion extends BaseLdml {

  @XmlAttribute(name = "type")
  private String type;

  @XmlAttribute(name = "by")
  private String by;

  @XmlAnyElement private String content;
}
