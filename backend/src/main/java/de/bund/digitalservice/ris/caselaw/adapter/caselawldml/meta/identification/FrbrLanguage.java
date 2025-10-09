package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.identification;

import jakarta.xml.bind.annotation.XmlAttribute;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class FrbrLanguage {
  @XmlAttribute private String language;

  public FrbrLanguage(String language) {
    this.language = language;
  }
}
