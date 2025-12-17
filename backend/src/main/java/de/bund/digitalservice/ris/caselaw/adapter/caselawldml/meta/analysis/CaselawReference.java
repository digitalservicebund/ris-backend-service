package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.analysis;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.AktenzeichenListe;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Dokumenttyp;
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
  @XmlElement(name = "dokumenttyp", namespace = CaseLawLdml.RIS_NS)
  private Dokumenttyp dokumenttyp;

  @XmlElement(name = "entscheidungsdatum", namespace = CaseLawLdml.RIS_NS)
  private Rechtszug.Entscheidungsdatum entscheidungsdatum;

  @XmlElement(name = "mitteilungsdatum", namespace = CaseLawLdml.RIS_NS)
  private Rechtszug.Mitteilungsdatum mitteilungsdatum;

  @XmlElement(name = "dokumentnummer", namespace = CaseLawLdml.RIS_NS)
  private Rechtszug.Dokumentnummer dokumentnummer;

  @XmlElement(name = "aktenzeichen", namespace = CaseLawLdml.RIS_NS)
  private AktenzeichenListe.Aktenzeichen aktenzeichen;

  @XmlElement(name = "gericht", namespace = CaseLawLdml.RIS_NS)
  private Gericht gericht;
}
