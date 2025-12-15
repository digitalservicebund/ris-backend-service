package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.analysis;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
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
public class Norm {
  @XmlAttribute(name = "domainTerm")
  @Builder.Default
  private String domainTerm = "Norm";

  @XmlElement(name = "normAbkuerzung", namespace = CaseLawLdml.RIS_NS)
  private Abkuerzung abkuerzung;

  @XmlElement(name = "einzelnorm", namespace = CaseLawLdml.RIS_NS)
  private List<Einzelnorm> einzelnormen;

  @XmlElement(name = "normTitel", namespace = CaseLawLdml.RIS_NS)
  private Titel titel;

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Abkuerzung {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Abkürzung";

    @XmlValue private String value;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Einzelnorm {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Einzelnorm";

    @XmlElement(name = "einzelnormBezeichnung", namespace = CaseLawLdml.RIS_NS)
    private Bezeichnung bezeichnung;

    @XmlElement(name = "einzelnormGesetzeskraft", namespace = CaseLawLdml.RIS_NS)
    private Gesetzeskraft gesetzeskraft;

    @XmlElement(name = "einzelnormDatum", namespace = CaseLawLdml.RIS_NS)
    private Fassungsdatum datum;

    @XmlElement(name = "einzelnormJahr", namespace = CaseLawLdml.RIS_NS)
    private Jahr jahr;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Bezeichnung {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Bezeichnung";

    @XmlValue private String value;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Gesetzeskraft {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Gesetzeskraft";

    @XmlElement(name = "gesetzeskraftTyp", namespace = CaseLawLdml.RIS_NS)
    private GesetzeskraftTyp typ;

    @XmlElement(name = "gesetzeskraftGeltungsbereich", namespace = CaseLawLdml.RIS_NS)
    private Geltungsbereich geltungsbereich;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class GesetzeskraftTyp {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Typ der Gesetzeskraft";

    @XmlValue private String value;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Geltungsbereich {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Geltungsbereich";

    @XmlValue private String value;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Fassungsdatum {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Fassungsdatum";

    @XmlValue private String value;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Jahr {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Jahr";

    @XmlValue private String value;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  @XmlType(name = "NormTitel") // Avoid conflict with inner classes with same name
  public static class Titel {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Amtliche Langüberschrift";

    @XmlValue private String value;
  }
}
