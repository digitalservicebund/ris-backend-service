package de.bund.digitalservice.ris.caselaw.adapter.caselawldml;

import jakarta.xml.bind.annotation.XmlAttribute;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class FrbrCountry {
  @XmlAttribute private String value;

  public FrbrCountry(String value) {
    this.value = value;
  }
}
