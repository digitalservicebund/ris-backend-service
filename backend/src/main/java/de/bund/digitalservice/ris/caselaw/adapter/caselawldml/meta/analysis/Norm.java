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
public class Norm {
  @XmlAttribute(name = "domainTerm")
  @Builder.Default
  private String domainTerm = "Norm";

  @XmlElement(name = "abkuerzung", namespace = CaseLawLdml.RIS_NS)
  private Abkuerzung abkuerzung;

  @XmlElement(name = "einzelnorm", namespace = CaseLawLdml.RIS_NS)
  private List<Einzelnorm> einzelnormen;

  @XmlElement(name = "titel", namespace = CaseLawLdml.RIS_NS)
  private String titel;

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

    @XmlElement(name = "bezeichnung", namespace = CaseLawLdml.RIS_NS)
    private Bezeichnung bezeichnung;

    @XmlElement(name = "gesetzeskraft", namespace = CaseLawLdml.RIS_NS)
    private Gesetzeskraft gesetzeskraft;

    @XmlElement(name = "datum", namespace = CaseLawLdml.RIS_NS)
    private Fassungsdatum datum;

    @XmlElement(name = "jahr", namespace = CaseLawLdml.RIS_NS)
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

    @XmlElement(name = "typ", namespace = CaseLawLdml.RIS_NS)
    private TypDerGesetzeskraft typ;

    @XmlElement(name = "geltungsbereich", namespace = CaseLawLdml.RIS_NS)
    private Geltungsbereich geltungsbereich;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class TypDerGesetzeskraft {
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
}
