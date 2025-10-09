package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.identification;

import jakarta.xml.bind.annotation.XmlAttribute;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class FrbrAlias {
  @XmlAttribute private String name;
  @XmlAttribute private String value;

  public FrbrAlias(String name, String value) {
    this.name = name;
    this.value = value;
  }
}
