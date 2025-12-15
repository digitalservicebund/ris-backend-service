package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.analysis;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.AktenzeichenListe;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.DokumentTyp;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Gericht;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;
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
  @XmlType(propOrder = {"dokumentTyp", "datum", "dokumentNummer", "aktenzeichen", "gericht"})
  public static class Vorgehend extends CaselawReference {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Vorgehende Entscheidung";

    @Override
    @XmlElement(name = "vorgehendDokumentTyp", namespace = CaseLawLdml.RIS_NS)
    public DokumentTyp getDokumentTyp() {
      return super.getDokumentTyp();
    }

    @Override
    @XmlElement(name = "vorgehendDatum", namespace = CaseLawLdml.RIS_NS)
    public Rechtszug.Datum getDatum() {
      return super.getDatum();
    }

    @Override
    @XmlElement(name = "vorgehendDokumentNummer", namespace = CaseLawLdml.RIS_NS)
    public Rechtszug.DokumentNummer getDokumentNummer() {
      return super.getDokumentNummer();
    }

    @Override
    @XmlElement(name = "vorgehendAktenzeichen", namespace = CaseLawLdml.RIS_NS)
    public AktenzeichenListe.Aktenzeichen getAktenzeichen() {
      return super.getAktenzeichen();
    }

    @Override
    @XmlElement(name = "vorgehendGericht", namespace = CaseLawLdml.RIS_NS)
    public Gericht getGericht() {
      return super.getGericht();
    }
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @SuperBuilder
  @XmlType(
      propOrder = {
        "art",
        "dokumentTyp",
        "datum",
        "dokumentNummer",
        "aktenzeichen",
        "gericht",
        "vermerk"
      })
  public static class Nachgehend extends CaselawReference {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Nachgehende Entscheidung";

    @XmlAttribute(name = "art")
    private ArtDerNachgehendenEntscheidung art;

    @Override
    @XmlElement(name = "nachgehendDokumentTyp", namespace = CaseLawLdml.RIS_NS)
    public DokumentTyp getDokumentTyp() {
      return super.getDokumentTyp();
    }

    @Override
    @XmlElement(name = "nachgehendDatum", namespace = CaseLawLdml.RIS_NS)
    public Rechtszug.Datum getDatum() {
      return super.getDatum();
    }

    @Override
    @XmlElement(name = "nachgehendDokumentNummer", namespace = CaseLawLdml.RIS_NS)
    public Rechtszug.DokumentNummer getDokumentNummer() {
      return super.getDokumentNummer();
    }

    @Override
    @XmlElement(name = "nachgehendAktenzeichen", namespace = CaseLawLdml.RIS_NS)
    public AktenzeichenListe.Aktenzeichen getAktenzeichen() {
      return super.getAktenzeichen();
    }

    @Override
    @XmlElement(name = "nachgehendGericht", namespace = CaseLawLdml.RIS_NS)
    public Gericht getGericht() {
      return super.getGericht();
    }

    @XmlElement(name = "nachgehendVermerk", namespace = CaseLawLdml.RIS_NS)
    private Vermerk vermerk;
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
