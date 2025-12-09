package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.analysis.Norm;
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
public class Nichtanwendungsgesetze {
  @XmlAttribute(name = "domainTerm")
  private static final String DOMAIN_TERM = "Nichtanwendungsgesetze";

  @SuppressWarnings("java:S1700")
  @XmlElement(name = "norm", namespace = CaseLawLdml.RIS_NS)
  private List<Norm> nichtanwendungsgesetze;
}
