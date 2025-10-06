package de.bund.digitalservice.ris.caselaw.adapter.caselawldml;

import jakarta.xml.bind.annotation.XmlAttribute;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class AknKeyword extends BaseLdml {

  @XmlAttribute(name = "dictionary")
  private String dictionary;

  @XmlAttribute(name = "showAs")
  private String showAs;

  @XmlAttribute(name = "value")
  private String value;

  public AknKeyword(String value) {
    this.domainTerm = "Schlagwort";
    this.dictionary = ""; // Is empty as we don't use a "Wertetabelle" for Keywords
    this.showAs = value;
    this.value = value;
  }
}
