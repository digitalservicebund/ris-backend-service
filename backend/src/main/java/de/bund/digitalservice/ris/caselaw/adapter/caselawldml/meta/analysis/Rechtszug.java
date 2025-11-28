package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.analysis;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.AktenzeichenListe;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.DokumentTyp;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Gericht;
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
  @AllArgsConstructor
  @Getter
  @SuperBuilder
  public static class CaselawReference {
    @XmlElement(name = "dokumentTyp", namespace = CaseLawLdml.RIS_NS)
    private DokumentTyp dokumentTyp;

    @XmlElement(name = "datum", namespace = CaseLawLdml.RIS_NS)
    private Datum datum;

    @XmlElement(name = "dokumentNummer", namespace = CaseLawLdml.RIS_NS)
    private DokumentNummer dokumentNummer;

    @XmlElement(name = "aktenzeichen", namespace = CaseLawLdml.RIS_NS)
    private AktenzeichenListe.Aktenzeichen aktenzeichen;

    @XmlElement(name = "gericht", namespace = CaseLawLdml.RIS_NS)
    private Gericht gericht;
  }

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

    @XmlElement(name = "vermerk", namespace = CaseLawLdml.RIS_NS)
    private Vermerk vermerk;

    @XmlAttribute(name = "art")
    private ArtDerNachgehendenEntscheidung art;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class Datum {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Entscheidungsdatum";

    @XmlValue private String value;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @Builder
  public static class DokumentNummer {
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
