package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.analysis;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

public class Rechtszug {

  @NoArgsConstructor
  @Getter
  @SuperBuilder
  public static class Vorgehend extends CaselawReference {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Vorgehende Entscheidung";
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @SuperBuilder
  public static class Nachgehend extends CaselawReference {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Nachgehende Entscheidung";

    @XmlAttribute(name = "art")
    private ArtDerNachgehendenEntscheidung art;

    @XmlElement(name = "vermerk", namespace = CaseLawLdml.RIS_NS)
    private Vermerk vermerk;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Entscheidungsdatum {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Entscheidungsdatum";

    @XmlValue private String value;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Mitteilungsdatum {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Mitteilungsdatum";

    @XmlValue private String value;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Dokumentnummer {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Dokumentnummer";

    @XmlValue private String value;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Vermerk {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Vermerk";

    @XmlValue private String value;
  }

  @XmlEnum
  public enum ArtDerNachgehendenEntscheidung {
    @XmlEnumValue("anhängig")
    ANHAENGIG,

    @XmlEnumValue("nachgehend")
    NACHGEHEND
  }
}
