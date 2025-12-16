package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.analysis;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.AktenzeichenListe;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Dokumenttyp;
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
  @XmlType(propOrder = {"dokumenttyp", "datum", "dokumentnummer", "aktenzeichen", "gericht"})
  public static class Vorgehend extends CaselawReference {
    @XmlAttribute(name = "domainTerm")
    private static final String DOMAIN_TERM = "Vorgehende Entscheidung";

    @Override
    @XmlElement(name = "vorgehendDokumenttyp", namespace = CaseLawLdml.RIS_NS)
    public Dokumenttyp getDokumenttyp() {
      return super.getDokumenttyp();
    }

    @Override
    @XmlElement(name = "vorgehendDatum", namespace = CaseLawLdml.RIS_NS)
    public Rechtszug.Datum getDatum() {
      return super.getDatum();
    }

    @Override
    @XmlElement(name = "vorgehendDokumentnummer", namespace = CaseLawLdml.RIS_NS)
    public Dokumentnummer getDokumentnummer() {
      return super.getDokumentnummer();
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
        "dokumenttyp",
        "datum",
        "dokumentnummer",
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
    @XmlElement(name = "nachgehendDokumenttyp", namespace = CaseLawLdml.RIS_NS)
    public Dokumenttyp getDokumenttyp() {
      return super.getDokumenttyp();
    }

    @Override
    @XmlElement(name = "nachgehendDatum", namespace = CaseLawLdml.RIS_NS)
    public Rechtszug.Datum getDatum() {
      return super.getDatum();
    }

    @Override
    @XmlElement(name = "nachgehendDokumentnummer", namespace = CaseLawLdml.RIS_NS)
    public Dokumentnummer getDokumentnummer() {
      return super.getDokumentnummer();
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
