package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.RandnummernRef;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.identification.FrbrLanguage;
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
public class HerkunftDerUebersetzungen {
  @XmlAttribute(name = "domainTerm")
  private static final String DOMAIN_TERM = "Herkunft der Übersetzungen";

  @SuppressWarnings("java:S1700")
  @XmlElement(name = "herkunftDerUebersetzung", namespace = CaseLawLdml.RIS_NS)
  private List<HerkunftDerUebersetzung> herkunftDerUebersetzungen;

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class HerkunftDerUebersetzung {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Herkunft der Übersetzung";

    @XmlElement(name = "FRBRlanguage", namespace = CaseLawLdml.AKN_NS)
    private FrbrLanguage frbrLanguage;

    @XmlElement(name = "uebersetzerinnen", namespace = CaseLawLdml.RIS_NS)
    private Uebersetzerinnen uebersetzerinnen;

    @XmlElement(name = "interneVerlinkungen", namespace = CaseLawLdml.RIS_NS)
    private InterneVerlinkungen interneVerlinkungen;

    @XmlElement(name = "externeVerlinkungen", namespace = CaseLawLdml.RIS_NS)
    private ExterneVerlinkungen externeVerlinkungen;

    @XmlElement(name = "art", namespace = CaseLawLdml.RIS_NS)
    private Uebersetzungsart uebersetzungsart;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Uebersetzerinnen {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Übersetzer:innen";

    @XmlElement(name = "uebersetzerin", namespace = CaseLawLdml.RIS_NS)
    @SuppressWarnings("java:S1700")
    private List<Uebersetzerin> uebersetzerinnen;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Uebersetzerin {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Übersetzer:in";

    @XmlValue private String value;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class InterneVerlinkungen {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Interne Verlinkungen";

    @XmlElement(name = "ref", namespace = CaseLawLdml.AKN_NS)
    @SuppressWarnings("java:S1700")
    private List<InterneVerlinkung> interneVerlinkungen;
  }

  @NoArgsConstructor
  public static class InterneVerlinkung extends RandnummernRef {
    public InterneVerlinkung(String randnummer) {
      super("Interne Verlinkung", randnummer);
    }
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class ExterneVerlinkungen {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Externe Verlinkungen";

    @XmlElement(name = "externeVerlinkung", namespace = CaseLawLdml.RIS_NS)
    @SuppressWarnings("java:S1700")
    private List<ExterneVerlinkung> externeVerlinkungen;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class ExterneVerlinkung {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Externe Verlinkung";

    @XmlElement(name = "documentRef", namespace = CaseLawLdml.AKN_NS)
    private DocumentRef documentRef;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Uebersetzungsart {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Übersetzungsart";

    @XmlValue private String value;
  }
}
