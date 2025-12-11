package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary;

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
public class Streitjahre {
  @XmlAttribute(name = "domainTerm")
  private static final String DOMAIN_TERM = "Streitjahre";

  @SuppressWarnings("java:S1700")
  @XmlElement(name = "streitjahr", namespace = CaseLawLdml.RIS_NS)
  private List<Streitjahr> streitjahre;

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Streitjahr {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Streitjahr";

    @XmlValue private String value;
  }
}
