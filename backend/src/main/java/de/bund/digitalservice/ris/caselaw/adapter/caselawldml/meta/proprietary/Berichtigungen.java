package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.RandnummernRef;
import jakarta.xml.bind.annotation.XmlAnyElement;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlValue;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Singular;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
public class Berichtigungen {
  @XmlAttribute(name = "domainTerm")
  private static final String DOMAIN_TERM = "Berichtigungen";

  @XmlElement(name = "berichtigung", namespace = CaseLawLdml.RIS_NS)
  private List<Berichtigung> values;

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Berichtigung {
    @Builder.Default
    @XmlAttribute(name = "domainTerm")
    private String domainTerm = "Berichtigung";

    @XmlElement(name = "berichtigungArtDerEintragung", namespace = CaseLawLdml.RIS_NS)
    private ArtDerEintragung artDerEintragung;

    @XmlElement(name = "berichtigungArtDerAenderung", namespace = CaseLawLdml.RIS_NS)
    private ArtDerAenderung artDerAenderung;

    @XmlElement(name = "berichtigungDatumDerAenderung", namespace = CaseLawLdml.RIS_NS)
    private DatumDerAenderung datumDerAenderung;

    @XmlElement(name = "berichtigungRandnummern", namespace = CaseLawLdml.RIS_NS)
    private Randnummern randnummern;

    @XmlElement(name = "berichtigungInhaltDerAenderung", namespace = CaseLawLdml.RIS_NS)
    private InhaltDerAenderung inhaltDerAenderung;

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Builder
    public static class ArtDerEintragung {
      @Builder.Default
      @XmlAttribute(name = "domainTerm")
      private String domainTerm = "Art der Eintragung";

      @XmlValue private String value;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Builder
    public static class ArtDerAenderung {
      @Builder.Default
      @XmlAttribute(name = "domainTerm")
      private String domainTerm = "Art der Änderung";

      @XmlValue private String value;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Builder
    public static class DatumDerAenderung {
      @Builder.Default
      @XmlAttribute(name = "domainTerm")
      private String domainTerm = "Datum der Änderung";

      @XmlValue private String value;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Builder
    public static class Randnummern {
      @Builder.Default
      @XmlAttribute(name = "domainTerm")
      private String domainTerm = "Randnummern";

      @Singular
      @XmlElement(name = "ref", namespace = CaseLawLdml.AKN_NS)
      private List<Randnummer> values;

      @NoArgsConstructor
      public static class Randnummer extends RandnummernRef {
        public Randnummer(String randnummer) {
          super("Randnummer", randnummer);
        }
      }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Builder
    public static class InhaltDerAenderung {
      @Builder.Default
      @XmlAttribute(name = "domainTerm")
      private String domainTerm = "Inhalt der Änderung";

      @XmlAnyElement private List<Object> content;
    }
  }
}
