package de.bund.digitalservice.ris.caselaw.adapter.caselawldml;

import jakarta.xml.bind.annotation.XmlAttribute;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class TlcElement {
  @XmlAttribute private String eId;
  @XmlAttribute private String href;
  @XmlAttribute private String showAs;

  public TlcElement(String eId, String href, String showAs) {
    this.eId = eId;
    this.href = href;
    this.showAs = showAs;
  }
}
