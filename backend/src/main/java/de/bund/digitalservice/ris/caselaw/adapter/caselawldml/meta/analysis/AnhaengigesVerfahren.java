package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.analysis;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.AktenzeichenListe;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.DokumentTyp;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Gericht;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@Getter
@SuperBuilder
public class AnhaengigesVerfahren extends CaselawReference {
  @XmlAttribute(name = "domainTerm")
  private static final String DOMAIN_TERM = "Anhängiges Verfahren";

  @XmlElement(name = "anhaengigesVerfahrenDokumentTyp", namespace = CaseLawLdml.RIS_NS)
  private DokumentTyp dokumentTyp;

  @XmlElement(name = "anhaengigesVerfahrenDatum", namespace = CaseLawLdml.RIS_NS)
  private Rechtszug.Datum datum;

  @XmlElement(name = "anhaengigesVerfahrenDokumentNummer", namespace = CaseLawLdml.RIS_NS)
  private Rechtszug.DokumentNummer dokumentNummer;

  @XmlElement(name = "anhaengigesVerfahrenAktenzeichen", namespace = CaseLawLdml.RIS_NS)
  private AktenzeichenListe.Aktenzeichen aktenzeichen;

  @XmlElement(name = "anhaengigesVerfahrenGericht", namespace = CaseLawLdml.RIS_NS)
  private Gericht gericht;
}
