package de.bund.digitalservice.ris.caselaw.domain;

/**
 * Enum representing different source values for decisions. O - Unaufgefordert eingesandtes Original
 * (Unsolicited original submission) A - Angefordertes Original (Requested original submission) Z -
 * Zeitschriftenveröffentlichung (Publication in a journal) E - Ohne Vorlage des Originals E-Mail
 * (Email submission without the original document) L - Ländergerichte, EuG- und EuGH-Entscheidungen
 * über jDV-Verfahren (Decisions from national courts, the EU General Court (EuG), and the Court of
 * Justice of the European Union (EuGH) regarding cross-border legal procedures) S - Sonstige (Other
 * sources)
 */
public enum SourceValue {
  O,
  A,
  Z,
  E,
  L,
  S
}
