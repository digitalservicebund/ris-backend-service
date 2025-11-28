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
public class Gegenstandswerte {
  @XmlAttribute(name = "domainTerm")
  private static final String DOMAIN_TERM = "Gegenstandswerte";

  @SuppressWarnings("java:S1700")
  @XmlElement(name = "gegenstandswert", namespace = CaseLawLdml.RIS_NS)
  private List<Gegenstandswert> gegenstandswerte;

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Gegenstandswert {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Gegenstandswert";

    @XmlElement(name = "betrag", namespace = CaseLawLdml.RIS_NS)
    private Betrag betrag;

    @XmlElement(name = "waehrung", namespace = CaseLawLdml.RIS_NS)
    private Waehrung waehrung;

    @XmlElement(name = "verfahren", namespace = CaseLawLdml.RIS_NS)
    private Verfahren verfahren;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Betrag {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Betrag";

    @XmlValue private String value;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Waehrung {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Währung";

    @XmlValue private String value;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Verfahren {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Verfahren";

    @XmlValue private String value;
  }
}
