package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.identification;

import jakarta.xml.bind.annotation.XmlAttribute;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class FrbrDate {
  @XmlAttribute private String eId;
  @XmlAttribute private String date;
  @XmlAttribute private String name;

  public FrbrDate(String date, String name) {
    this.date = date;
    this.name = name;
  }

  public void setEid(String eId) {
    this.eId = eId;
  }
}
