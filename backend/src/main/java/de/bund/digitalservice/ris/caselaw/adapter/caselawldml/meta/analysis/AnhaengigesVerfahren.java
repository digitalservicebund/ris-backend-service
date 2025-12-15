package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.analysis;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.AktenzeichenListe;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.DokumentTyp;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Gericht;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@Getter
@SuperBuilder
@XmlType(propOrder = {"dokumentTyp", "datum", "dokumentNummer", "aktenzeichen", "gericht"})
public class AnhaengigesVerfahren extends CaselawReference {
  @XmlAttribute(name = "domainTerm")
  private static final String DOMAIN_TERM = "Anhängiges Verfahren";

  @Override
  @XmlElement(name = "anhaengigesVerfahrenDokumentTyp", namespace = CaseLawLdml.RIS_NS)
  public DokumentTyp getDokumentTyp() {
    return super.getDokumentTyp();
  }

  @Override
  @XmlElement(name = "anhaengigesVerfahrenDatum", namespace = CaseLawLdml.RIS_NS)
  public Rechtszug.Datum getDatum() {
    return super.getDatum();
  }

  @Override
  @XmlElement(name = "anhaengigesVerfahrenDokumentNummer", namespace = CaseLawLdml.RIS_NS)
  public Rechtszug.DokumentNummer getDokumentNummer() {
    return super.getDokumentNummer();
  }

  @Override
  @XmlElement(name = "anhaengigesVerfahrenAktenzeichen", namespace = CaseLawLdml.RIS_NS)
  public AktenzeichenListe.Aktenzeichen getAktenzeichen() {
    return super.getAktenzeichen();
  }

  @Override
  @XmlElement(name = "anhaengigesVerfahrenGericht", namespace = CaseLawLdml.RIS_NS)
  public Gericht getGericht() {
    return super.getGericht();
  }
}
