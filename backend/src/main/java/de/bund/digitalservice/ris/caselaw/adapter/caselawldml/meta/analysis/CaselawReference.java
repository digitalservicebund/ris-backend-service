package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.analysis;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.AktenzeichenListe;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.DokumentTyp;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Gericht;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@SuperBuilder
public class CaselawReference {
  @XmlElement(name = "dokumentTyp", namespace = CaseLawLdml.RIS_NS)
  private DokumentTyp dokumentTyp;

  @XmlElement(name = "datum", namespace = CaseLawLdml.RIS_NS)
  private Rechtszug.Datum datum;

  @XmlElement(name = "dokumentNummer", namespace = CaseLawLdml.RIS_NS)
  private Rechtszug.DokumentNummer dokumentNummer;

  @XmlElement(name = "aktenzeichen", namespace = CaseLawLdml.RIS_NS)
  private AktenzeichenListe.Aktenzeichen aktenzeichen;

  @XmlElement(name = "gericht", namespace = CaseLawLdml.RIS_NS)
  private Gericht gericht;
}
