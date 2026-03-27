package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.analysis;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlValue;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Entscheidungsnamen {
  @XmlAttribute(name = "domainTerm")
  private static final String DOMAIN_TERM = "Entscheidungsnamen";

  @SuppressWarnings("java:S1700")
  @XmlElement(name = "entscheidungsname", namespace = CaseLawLdml.RIS_NS)
  private List<Entscheidungsname> entscheidungsnamen;

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Entscheidungsname {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Entscheidungsname";

    @XmlValue private String value;
  }
}
