package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.identification;

import static de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml.RIS_NS;

import jakarta.xml.bind.annotation.XmlAttribute;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class FrbrLanguage {
  @XmlAttribute(name = "domainTerm", namespace = RIS_NS)
  private String domainTerm;

  @XmlAttribute private String language;

  public FrbrLanguage(String language) {
    this.language = language;
  }
}
