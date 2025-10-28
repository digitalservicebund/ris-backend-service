package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Definitionen {
  @Builder.Default
  @XmlAttribute(namespace = CaseLawLdml.RIS_NS, name = "domainTerm")
  private String domainTerm = "Definitionen";

  @SuppressWarnings("java:S1700")
  @XmlElement(name = "definition", namespace = CaseLawLdml.RIS_NS)
  private List<Definition> definitionen;
}
