package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.analysis;

import jakarta.xml.bind.annotation.XmlAttribute;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@Getter
@SuperBuilder
public class AnhaengigesVerfahren extends CaselawReference {
  @XmlAttribute(name = "domainTerm")
  private static final String DOMAIN_TERM = "Anhängiges Verfahren";
}
