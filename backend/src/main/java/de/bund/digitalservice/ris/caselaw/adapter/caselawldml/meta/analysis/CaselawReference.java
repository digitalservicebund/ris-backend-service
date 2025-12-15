package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.analysis;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.AktenzeichenListe;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.DokumentTyp;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary.Gericht;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@Getter
@SuperBuilder
public abstract class CaselawReference {
  protected DokumentTyp dokumentTyp;
  protected Rechtszug.Datum datum;
  protected Rechtszug.DokumentNummer dokumentNummer;
  protected AktenzeichenListe.Aktenzeichen aktenzeichen;
  protected Gericht gericht;
}
