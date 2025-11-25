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
@SuppressWarnings("java:S6539") // Monster class depends on more than 20 classes
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

  @XmlElement(name = "datenDerMuendlichenVerhandlung", namespace = CaseLawLdml.RIS_NS)
  private DatenDerMuendlichenVerhandlung datenDerMuendlichenVerhandlung;

  @XmlElement(name = "abweichendeDaten", namespace = CaseLawLdml.RIS_NS)
  private AbweichendeDaten abweichendeDaten;

  @XmlElement(name = "abweichendeDokumentnummern", namespace = CaseLawLdml.RIS_NS)
  private AbweichendeDokumentnummern abweichendeDokumentnummern;

  @XmlElement(name = "abweichendeEclis", namespace = CaseLawLdml.RIS_NS)
  private AbweichendeEclis abweichendeEclis;

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

  @XmlElement(name = "herkunftDerUebersetzungen", namespace = CaseLawLdml.RIS_NS)
  private HerkunftDerUebersetzungen herkunftDerUebersetzungen;

  @XmlElement(name = "evsf", namespace = CaseLawLdml.RIS_NS)
  private Evsf evsf;

  @XmlElement(name = "rechtsmittelzulassung", namespace = CaseLawLdml.RIS_NS)
  private Rechtsmittelzulassung rechtsmittelzulassung;

  @XmlElement(name = "tarifvertraege", namespace = CaseLawLdml.RIS_NS)
  private Tarifvertraege tarifvertraege;

  @XmlElement(name = "quellen", namespace = CaseLawLdml.RIS_NS)
  private Quellen quellen;

  @XmlElement(name = "streitjahre", namespace = CaseLawLdml.RIS_NS)
  private Streitjahre streitjahre;

  @XmlElement(name = "berufsbilder", namespace = CaseLawLdml.RIS_NS)
  private Berufsbilder berufsbilder;

  @XmlElement(name = "kuendigungsgruende", namespace = CaseLawLdml.RIS_NS)
  private Kuendigungsgruende kuendigungsgruende;

  @XmlElement(name = "kuendigungsarten", namespace = CaseLawLdml.RIS_NS)
  private Kuendigungsarten kuendigungsarten;

  @XmlElement(name = "gesetzgebungsauftrag", namespace = CaseLawLdml.RIS_NS)
  private Gesetzgebungsauftrag gesetzgebungsauftrag;

  @XmlElement(name = "notiz", namespace = CaseLawLdml.RIS_NS)
  private Notiz notiz;
}
