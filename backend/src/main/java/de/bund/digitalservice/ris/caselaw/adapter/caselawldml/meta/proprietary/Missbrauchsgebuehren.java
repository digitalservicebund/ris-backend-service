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
public class Missbrauchsgebuehren {
  @XmlAttribute(name = "domainTerm")
  private static final String DOMAIN_TERM = "Missbrauchsgebühren";

  @SuppressWarnings("java:S1700")
  @XmlElement(name = "missbrauchsgebuehr", namespace = CaseLawLdml.RIS_NS)
  private List<Missbrauchsgebuehr> missbrauchsgebuehren;

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Missbrauchsgebuehr {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Missbrauchsgebühr";

    @XmlElement(name = "missbrauchsgebuehrBetrag", namespace = CaseLawLdml.RIS_NS)
    private MissbrauchsgebuehrBetrag missbrauchsgebuehrBetrag;

    @XmlElement(name = "missbrauchsgebuehrWaehrung", namespace = CaseLawLdml.RIS_NS)
    private MissbrauchsgebuehrWaehrung missbrauchsgebuehrWaehrung;

    @XmlElement(name = "missbrauchsgebuehrAdressat", namespace = CaseLawLdml.RIS_NS)
    private MissbrauchsgebuehrAdressat missbrauchsgebuehrAdressat;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class MissbrauchsgebuehrBetrag {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Betrag";

    @XmlValue private String value;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class MissbrauchsgebuehrWaehrung {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Währung";

    @XmlValue private String value;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class MissbrauchsgebuehrAdressat {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Adressat";

    @XmlValue private String value;
  }
}
