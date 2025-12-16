package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.analysis;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Periodikum {
  @XmlAttribute(name = "domainTerm")
  private static final String DOMAIN_TERM = "Periodikum";

  @XmlElement(name = "periodikumAbkuerzung", namespace = CaseLawLdml.RIS_NS)
  private PeriodikumAbkuerzung periodikumAbkuerzung;

  @XmlElement(name = "periodikumTyp", namespace = CaseLawLdml.RIS_NS)
  private Typ typ;

  @XmlElement(name = "periodikumTitel", namespace = CaseLawLdml.RIS_NS)
  private Titel titel;

  @XmlElement(name = "periodikumUntertitel", namespace = CaseLawLdml.RIS_NS)
  private Untertitel untertitel;

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class PeriodikumAbkuerzung {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Abkürzung";

    @XmlValue private String value;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Typ {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Art der Fundstelle";

    @XmlValue private String value;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Titel {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Titel";

    @XmlValue private String value;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Untertitel {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Untertitel";

    @XmlValue private String value;
  }
}
