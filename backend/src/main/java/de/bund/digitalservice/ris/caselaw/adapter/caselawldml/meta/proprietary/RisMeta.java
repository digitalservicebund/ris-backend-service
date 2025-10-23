package de.bund.digitalservice.ris.caselaw.adapter.caselawldml.meta.proprietary;

import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.CaseLawLdml;
import de.bund.digitalservice.ris.caselaw.adapter.caselawldml.RelatedDecision;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class RisMeta {
  @XmlElementWrapper(name = "decisionNames", namespace = CaseLawLdml.RIS_NS)
  @XmlElement(name = "decisionName", namespace = CaseLawLdml.RIS_NS)
  private List<String> decisionName;

  @XmlElementWrapper(name = "previousDecisions", namespace = CaseLawLdml.RIS_NS)
  @XmlElement(name = "previousDecision", namespace = CaseLawLdml.RIS_NS)
  private List<RelatedDecision> previousDecision;

  @XmlElementWrapper(name = "ensuingDecisions", namespace = CaseLawLdml.RIS_NS)
  @XmlElement(name = "ensuingDecision", namespace = CaseLawLdml.RIS_NS)
  private List<RelatedDecision> ensuingDecision;

  @XmlElement(name = "dokumentTyp", namespace = CaseLawLdml.RIS_NS)
  private DokumentTyp dokumentTyp;

  @XmlElement(name = "gericht", namespace = CaseLawLdml.RIS_NS)
  private Gericht gericht;

  @XmlElement(name = "fehlerhafteGerichte", namespace = CaseLawLdml.RIS_NS)
  private FehlerhafteGerichte fehlerhafteGerichte;

  @XmlElement(name = "regionen", namespace = CaseLawLdml.RIS_NS)
  private Regionen regionen;

  @XmlElement(name = "dokumentationsstelle", namespace = CaseLawLdml.RIS_NS)
  private Dokumentationsstelle dokumentationsstelle;

  @XmlElement(name = "aktenzeichenListe", namespace = CaseLawLdml.RIS_NS)
  private AktenzeichenListe aktenzeichenListe;

  @XmlElement(name = "abweichendeDaten", namespace = CaseLawLdml.RIS_NS)
  private AbweichendeDaten abweichendeDaten;

  @XmlElement(name = "abweichendeDokumentnummern", namespace = CaseLawLdml.RIS_NS)
  private AbweichendeDokumentnummern abweichendeDokumentnummern;

  @XmlElement(name = "abweichendeEclis", namespace = CaseLawLdml.RIS_NS)
  private AbweichendeEclis abweichendeEclis;

  @XmlElement(name = "spruchkoerper", namespace = CaseLawLdml.RIS_NS)
  private Spruchkoerper spruchkoerper;

  @XmlElement(name = "sachgebiete", namespace = CaseLawLdml.RIS_NS)
  private Sachgebiete sachgebiete;

  @XmlElement(name = "rechtskraft", namespace = CaseLawLdml.RIS_NS)
  private Rechtskraft rechtskraft;

  @XmlElement(name = "vorgaenge", namespace = CaseLawLdml.RIS_NS)
  private Vorgaenge vorgaenge;

  @XmlElement(name = "eingangsarten", namespace = CaseLawLdml.RIS_NS)
  private Eingangsarten eingangsarten;

  @XmlElement(name = "definitionen", namespace = CaseLawLdml.RIS_NS)
  private Definitionen definitionen;

  @XmlElement(name = "fremdsprachigeFassungen", namespace = CaseLawLdml.RIS_NS)
  private FremdsprachigeFassungen fremdsprachigeFassungen;

  @XmlElement(name = "evsf", namespace = CaseLawLdml.RIS_NS)
  private Evsf evsf;
}
