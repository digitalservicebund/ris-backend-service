package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
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
public class Spruchkoerper {
  @Builder.Default
  @XmlAttribute(name = "domainTerm")
  private String domainTerm = "Spruchkörper";

  @XmlAttribute(namespace = CaseLawLdml.AKN_NS, name = "refersTo")
  private String refersTo;

  @XmlValue private String value;
}
