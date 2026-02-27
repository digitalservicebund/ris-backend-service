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
public class Gebuehren {
  @XmlAttribute(name = "domainTerm")
  private static final String DOMAIN_TERM = "Gebühren";

  @SuppressWarnings("java:S1700")
  @XmlElement(name = "gebuehr", namespace = CaseLawLdml.RIS_NS)
  private List<Gebuehr> gebuehren;

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Gebuehr {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Gebühr";

    @XmlElement(name = "betrag", namespace = CaseLawLdml.RIS_NS)
    private Betrag betrag;

    @XmlElement(name = "waehrung", namespace = CaseLawLdml.RIS_NS)
    private Waehrung waehrung;

    @XmlElement(name = "adressat", namespace = CaseLawLdml.RIS_NS)
    private Adressat adressat;
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
  public static class Adressat {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Adressat";

    @XmlValue private String value;
  }
}
