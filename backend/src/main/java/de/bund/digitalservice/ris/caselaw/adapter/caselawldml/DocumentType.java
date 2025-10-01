package de.bund.digitalservice.ris.caselaw.adapter.caselawldml;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class DocumentType {
  @XmlValue private String value;

  @XmlAttribute(namespace = CaseLawLdml.AKN_NS, name = "eId")
  private String eId;

  public void DocumentType(String value, String eId) {
    this.value = value;
    this.eId = eId;
  }
}
