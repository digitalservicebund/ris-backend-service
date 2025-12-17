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
public class Tarifvertraege {
  @XmlAttribute(name = "domainTerm")
  private static final String DOMAIN_TERM = "Tarifverträge";

  @SuppressWarnings("java:S1700")
  @XmlElement(name = "tarifvertrag", namespace = CaseLawLdml.RIS_NS)
  private List<Tarifvertrag> tarifvertraege;

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Tarifvertrag {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Tarifvertrag";

    @XmlElement(name = "bezeichnung", namespace = CaseLawLdml.RIS_NS)
    private TarifvertragName name;

    @XmlElement(name = "tarifnorm", namespace = CaseLawLdml.RIS_NS)
    private Tarifnorm tarifnorm;

    @XmlElement(name = "datum", namespace = CaseLawLdml.RIS_NS)
    private TarifvertragDatum datum;

    @XmlElement(name = "branche", namespace = CaseLawLdml.RIS_NS)
    private TarifvertragBranche branche;

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Builder
    public static class TarifvertragName {
      @XmlAttribute(name = "domainTerm")
      private static final String DOMAIN_TERM = "Bezeichnung des Tarifvertrag";

      @XmlValue private String value;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Builder
    public static class Tarifnorm {
      @XmlAttribute(name = "domainTerm")
      private static final String DOMAIN_TERM = "Tarifnorm";

      @XmlValue private String value;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Builder
    public static class TarifvertragDatum {
      @XmlAttribute(name = "domainTerm")
      private static final String DOMAIN_TERM = "Datum";

      @XmlValue private String value;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Builder
    public static class TarifvertragBranche {
      @XmlAttribute(name = "domainTerm")
      private static final String DOMAIN_TERM = "Branche";

      @XmlValue private String value;
    }
  }
}
